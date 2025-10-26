/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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
import net.clementraynaud.skoice.common.storage.ProxyYamlFile;
import net.clementraynaud.skoice.common.storage.TempYamlFile;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.clementraynaud.skoice.common.storage.config.ConfigYamlFile;
import net.clementraynaud.skoice.common.storage.config.OutdatedConfig;
import net.clementraynaud.skoice.common.system.ListenerManager;
import net.clementraynaud.skoice.common.tasks.UpdateNetworksTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

public abstract class Skoice {

    private static AnalyticManager analyticManager;
    private static SkoiceAPI api;
    private static EventBus eventBus;
    private final SkoiceLogger logger;
    private final SkoiceTaskScheduler scheduler;
    private MinecraftLang lang;
    private ConfigYamlFile configYamlFile;
    private LinksYamlFile linksYamlFile;
    private TempYamlFile tempYamlFile;
    private LoginNotificationYamlFile loginNotificationYamlFile;
    private ProxyYamlFile proxyYamlFile;
    private ListenerManager listenerManager;
    private Bot bot;
    private UpdateNetworksTask updateNetworksTask;

    protected Skoice(SkoiceLogger logger, SkoiceTaskScheduler scheduler) {
        this.logger = logger;
        this.scheduler = scheduler;
    }

    public static AnalyticManager analyticManager() {
        return Skoice.analyticManager;
    }

    /**
     * Gets the Skoice API instance.
     * <p>
     * This is the main entry point for interacting with Skoice's developer API.
     * The API provides methods for checking player states, managing account links,
     * and querying system status.
     *
     * <p><b>Example usage:</b>
     * <pre>{@code
     * SkoiceAPI api = Skoice.api();
     * if (api != null && api.isSystemReady()) {
     *     boolean isConnected = api.isProximityConnected(playerUuid);
     * }
     * }</pre>
     *
     * @return the Skoice API instance, or null if not initialized
     * @see SkoiceAPI
     */
    public static SkoiceAPI api() {
        return Skoice.api;
    }

    /**
     * Gets the event bus instance.
     * <p>
     * The event bus allows you to subscribe to various Skoice events such as
     * player proximity connections, account linking, and system state changes.
     *
     * <p><b>Example usage:</b>
     * <pre>{@code
     * EventBus eventBus = Skoice.eventBus();
     * if (eventBus != null) {
     *     eventBus.subscribe(PlayerProximityConnectEvent.class, event -> {
     *         // Handle player connecting to proximity chat
     *     });
     * }
     * }</pre>
     *
     * @return the event bus instance, or null if not initialized
     * @see EventBus
     */
    public static EventBus eventBus() {
        return Skoice.eventBus;
    }

    public void start() {
        this.saveDefaultConfig();
        this.configYamlFile = new ConfigYamlFile(this);
        this.configYamlFile.load();
        this.configYamlFile.saveDefaultValues();
        this.lang = new MinecraftLang();
        this.lang.load(LangInfo.valueOf(this.configYamlFile.getString(ConfigField.LANG.toString())));
        this.logger.info(this.lang.getMessage("logger.info.plugin-enabled"));
        this.linksYamlFile = this.createLinksYamlFile();
        this.linksYamlFile.load();
        new OutdatedConfig(this).update();
        this.tempYamlFile = new TempYamlFile(this);
        this.tempYamlFile.load();
        this.loginNotificationYamlFile = new LoginNotificationYamlFile(this);
        this.loginNotificationYamlFile.load();
        this.proxyYamlFile = this.createProxyYamlFile();
        if (this.proxyYamlFile != null) {
            this.proxyYamlFile.load();
            this.proxyYamlFile.saveDefaultValues();
        }
        Skoice.eventBus = new EventBus();
        Skoice.api = new SkoiceAPI(this);
        Skoice.analyticManager = this.createAnalyticManager();
        Skoice.analyticManager.initialize();
        this.listenerManager.registerPermanentMinecraftListeners();
        this.runBot();
        this.updateNetworksTask = new UpdateNetworksTask(this);
        this.setSkoiceCommand().init();
        Updater updater = new Updater(this, this.getPluginFilePath());
        updater.runUpdaterTaskTimer();
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

    protected ProxyYamlFile createProxyYamlFile() {
        return null;
    }

    public abstract SkoiceCommand setSkoiceCommand();

    private void saveDefaultConfig() {
        File configFile = new File(this.getDataFolder(), "config.yml");
        if (configFile.exists()) {
            this.saveResource("config.yml", false);
        }
    }

    public void shutdown() {
        this.bot.shutdown();
        if (Skoice.eventBus != null) {
            Skoice.eventBus.shutdown();
        }
        this.logger.info(this.lang.getMessage("logger.info.plugin-disabled"));
    }

    private InputStream getResource(String filename) {
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

    private void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = this.getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + this.getDataFolder());
        }

        File outFile = new File(this.getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(this.getDataFolder(), resourcePath.substring(0, Math.max(lastIndex, 0)));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = Files.newOutputStream(outFile.toPath());
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            }
        } catch (IOException ex) {
            this.logger.severe("Could not save " + outFile.getName() + " to " + outFile);
        }
    }

    public void log(Level level, String path) {
        this.getLogger().log(level, this.getLang().getConsoleMessage(path));
    }

    public SkoiceLogger getLogger() {
        return this.logger;
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

    public ProxyYamlFile getProxyYamlFile() {
        return this.proxyYamlFile;
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

    public abstract File getUpdateFolderFile();

    public abstract String getPluginFilePath();

    public abstract boolean areHooksAvailable();
}
