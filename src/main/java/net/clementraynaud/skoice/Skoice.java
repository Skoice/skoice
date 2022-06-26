/*
 * Copyright 2020, 2021, 2022 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice;

import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.commands.skoice.SkoiceCommand;
import net.clementraynaud.skoice.config.Configuration;
import net.clementraynaud.skoice.config.ConfigurationField;
import net.clementraynaud.skoice.config.OutdatedConfiguration;
import net.clementraynaud.skoice.lang.LangInfo;
import net.clementraynaud.skoice.lang.Lang;
import net.clementraynaud.skoice.menus.ConfigurationMenu;
import net.clementraynaud.skoice.storage.LinksFileStorage;
import net.clementraynaud.skoice.storage.TempFileStorage;
import net.clementraynaud.skoice.system.ListenerManager;
import net.clementraynaud.skoice.tasks.InterruptSystemTask;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class Skoice extends JavaPlugin {

    private static final int SERVICE_ID = 11380;
    private static final int RESSOURCE_ID = 82861;

    private Lang lang;
    private Configuration configuration;
    private LinksFileStorage linksFileStorage;
    private TempFileStorage tempFileStorage;
    private ListenerManager listenerManager;
    private Bot bot;
    private ConfigurationMenu configurationMenu;

    private Updater updater;

    @Override
    public void onEnable() {
        new Metrics(this, Skoice.SERVICE_ID);
        this.saveDefaultConfig();
        this.configuration = new Configuration(this);
        this.configuration.init();
        this.lang = new Lang();
        this.lang.load(LangInfo.valueOf(this.configuration.getFile().getString(ConfigurationField.LANG.toString())));
        this.getLogger().info(this.lang.getMessage("logger.info.plugin-enabled"));
        this.linksFileStorage = new LinksFileStorage(this);
        this.linksFileStorage.load();
        new OutdatedConfiguration(this).update();
        this.tempFileStorage = new TempFileStorage(this);
        this.tempFileStorage.load();
        this.listenerManager = new ListenerManager(this);
        this.bot = new Bot(this);
        this.bot.connect();
        this.configurationMenu = new ConfigurationMenu(this);
        if (this.bot.getJDA() != null) {
            this.bot.setup();
        } else {
            this.listenerManager.update();
        }
        new SkoiceCommand(this).init();
        this.updater = new Updater(this, Skoice.RESSOURCE_ID);
        this.updater.checkVersion();
    }

    @Override
    public void onDisable() {
        if (this.bot.getJDA() != null) {
            new InterruptSystemTask(this.configuration).run();
            this.bot.getJDA().shutdown();
        }
        this.getLogger().info(this.lang.getMessage("logger.info.plugin-disabled"));
    }

    public Lang getLang() {
        return this.lang;
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public LinksFileStorage getLinksFileStorage() {
        return this.linksFileStorage;
    }

    public TempFileStorage getTempFileStorage() {
        return this.tempFileStorage;
    }

    public ListenerManager getListenerManager() {
        return this.listenerManager;
    }

    public Bot getBot() {
        return this.bot;
    }

    public ConfigurationMenu getConfigurationMenu() {
        return this.configurationMenu;
    }

    public Updater getUpdater() {
        return this.updater;
    }
}
