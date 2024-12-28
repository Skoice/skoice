/*
 * Copyright 2020, 2021, 2022, 2023, 2024 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
 *
 * This file is part of Skoice.
 *
 * Skoice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skoice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skoice.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.clementraynaud.skoice.common;

import net.clementraynaud.skoice.common.analytics.AnalyticManager;
import net.clementraynaud.skoice.common.bot.Bot;
import net.clementraynaud.skoice.common.commands.skoice.SkoiceCommand;
import net.clementraynaud.skoice.common.lang.LangInfo;
import net.clementraynaud.skoice.common.lang.MinecraftLang;
import net.clementraynaud.skoice.common.model.logger.SkoiceLogger;
import net.clementraynaud.skoice.common.model.minecraft.BasePlayer;
import net.clementraynaud.skoice.common.model.minecraft.FullPlayer;
import net.clementraynaud.skoice.common.model.scheduler.SkoiceTaskScheduler;
import net.clementraynaud.skoice.common.storage.LinksYamlFile;
import net.clementraynaud.skoice.common.storage.LoginNotificationYamlFile;
import net.clementraynaud.skoice.common.storage.TempYamlFile;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.clementraynaud.skoice.common.storage.config.ConfigYamlFile;
import net.clementraynaud.skoice.common.storage.config.OutdatedConfig;
import net.clementraynaud.skoice.common.system.ListenerManager;
import net.clementraynaud.skoice.common.tasks.UpdateNetworksTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

public abstract class Skoice {

    private static AnalyticManager analyticManager;
    private final SkoiceLogger logger;
    private final SkoiceTaskScheduler scheduler;
    private MinecraftLang lang;
    private ConfigYamlFile configYamlFile;
    private LinksYamlFile linksYamlFile;
    private TempYamlFile tempYamlFile;
    private LoginNotificationYamlFile loginNotificationYamlFile;
    private ListenerManager listenerManager;
    private Bot bot;
    private UpdateNetworksTask updateNetworksTask;

    public Skoice(SkoiceLogger logger, SkoiceTaskScheduler scheduler) {
        this.logger = logger;
        this.scheduler = scheduler;
    }

    public static AnalyticManager analyticManager() {
        return Skoice.analyticManager;
    }

    public SkoiceLogger getLogger() {
        return this.logger;
    }

    public void onEnable() {
        this.saveDefaultConfig();
        this.configYamlFile = new ConfigYamlFile(this);
        this.configYamlFile.load();
        this.configYamlFile.saveDefaultValues();
        this.lang = new MinecraftLang();
        this.lang.load(LangInfo.valueOf(this.configYamlFile.getString(ConfigField.LANG.toString())));
        this.log(Level.INFO, "logger.info.plugin-enabled");
        this.linksYamlFile = this.createLinksYamlFile();
        this.linksYamlFile.load();
        new OutdatedConfig(this).update();
        this.tempYamlFile = new TempYamlFile(this);
        this.tempYamlFile.load();
        this.loginNotificationYamlFile = new LoginNotificationYamlFile(this);
        this.loginNotificationYamlFile.load();
        Skoice.analyticManager = this.createAnalyticManager();
        Skoice.analyticManager.initialize();
        this.listenerManager.registerPermanentMinecraftListeners();
        this.runBot();
        this.updateNetworksTask = new UpdateNetworksTask(this);
        this.setSkoiceCommand().init();
    }

    protected AnalyticManager createAnalyticManager() {
        return new AnalyticManager(this);
    }

    private void runBot() {
        this.bot = this.createBot();
        this.bot.connect();
    }

    protected Bot createBot() {
        return new Bot(this);
    }

    protected LinksYamlFile createLinksYamlFile() {
        return new LinksYamlFile(this);
    }

    public abstract SkoiceCommand setSkoiceCommand();

    private void saveDefaultConfig() {
        File configFile = new File(this.getDataFolder(), "config.yml");
        if (configFile.exists()) {
            this.saveResource("config.yml", false);
        }
    }

    public InputStream getResource(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        try {
            ClassLoader classLoader = this.getClass().getClassLoader();
            URL url = classLoader.getResource(filename);

            if (url == null) {
                return null;
            }

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }

    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || "".equals(resourcePath)) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = this.getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + this.getDataFolder());
        }

        File outFile = new File(this.getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(this.getDataFolder(), resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } else {
                this.logger.warning("Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
            }
        } catch (IOException ex) {
            this.logger.severe("Could not save " + outFile.getName() + " to " + outFile);
        }
    }

    public void onDisable() {
        this.bot.shutdown();
        this.log(Level.INFO, "logger.info.plugin-disabled");
    }

    public void log(Level level, String path) {
        this.getLogger().log(level, this.getLang().getConsoleMessage(path));
    }

    public MinecraftLang getLang() {
        return this.lang;
    }

    public ConfigYamlFile getConfigYamlFile() {
        return this.configYamlFile;
    }

    public LinksYamlFile getLinksYamlFile() {
        return this.linksYamlFile;
    }

    public TempYamlFile getTempYamlFile() {
        return this.tempYamlFile;
    }

    public LoginNotificationYamlFile getLoginNotificationYamlFile() {
        return this.loginNotificationYamlFile;
    }

    public ListenerManager getListenerManager() {
        return this.listenerManager;
    }

    public void setListenerManager(ListenerManager listenerManager) {
        this.listenerManager = listenerManager;
    }

    public Bot getBot() {
        return this.bot;
    }

    public UpdateNetworksTask getUpdateNetworksTask() {
        return this.updateNetworksTask;
    }

    public abstract boolean isEnabled();

    public SkoiceTaskScheduler getScheduler() {
        return this.scheduler;
    }

    public abstract File getDataFolder();

    public abstract BasePlayer getPlayer(UUID uuid);

    public abstract Collection<FullPlayer> getOnlinePlayers();

    public abstract Collection<String> getWorlds();

    public abstract FullPlayer getFullPlayer(BasePlayer player);

    public abstract String getVersion();
}
