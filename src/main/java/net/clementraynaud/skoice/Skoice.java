/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

import com.bugsnag.Bugsnag;
import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.bot.BotCommands;
import net.clementraynaud.skoice.commands.skoice.SkoiceCommand;
import net.clementraynaud.skoice.lang.Lang;
import net.clementraynaud.skoice.lang.LangInfo;
import net.clementraynaud.skoice.menus.ConfigurationMenu;
import net.clementraynaud.skoice.storage.LinksYamlFile;
import net.clementraynaud.skoice.storage.LoginNotificationYamlFile;
import net.clementraynaud.skoice.storage.TempYamlFile;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.clementraynaud.skoice.storage.config.ConfigYamlFile;
import net.clementraynaud.skoice.storage.config.OutdatedConfig;
import net.clementraynaud.skoice.system.ListenerManager;
import net.clementraynaud.skoice.tasks.InterruptSystemTask;
import net.clementraynaud.skoice.util.ChartUtil;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class Skoice extends JavaPlugin {

    private static final int SERVICE_ID = 11380;
    private static final String BUGSNAG_KEY = "";

    private Lang lang;
    private ConfigYamlFile configYamlFile;
    private LinksYamlFile linksYamlFile;
    private TempYamlFile tempYamlFile;
    private LoginNotificationYamlFile loginNotificationYamlFile;
    private ListenerManager listenerManager;
    private Bot bot;
    private BotCommands botCommands;
    private ConfigurationMenu configurationMenu;
    private BukkitAudiences adventure;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.configYamlFile = new ConfigYamlFile(this);
        this.configYamlFile.load();
        this.configYamlFile.saveDefaultValues();
        this.lang = new Lang();
        this.lang.load(LangInfo.valueOf(this.configYamlFile.getString(ConfigField.LANG.toString())));
        this.getLogger().info(this.lang.getMessage("logger.info.plugin-enabled"));
        this.linksYamlFile = new LinksYamlFile(this);
        this.linksYamlFile.load();
        new OutdatedConfig(this).update();
        this.tempYamlFile = new TempYamlFile(this);
        this.tempYamlFile.load();
        this.loginNotificationYamlFile = new LoginNotificationYamlFile(this);
        this.loginNotificationYamlFile.load();
        this.listenerManager = new ListenerManager(this);
        this.bot = new Bot(this);
        this.bot.connect();
        this.configurationMenu = new ConfigurationMenu(this);
        this.botCommands = new BotCommands(this);
        this.adventure = BukkitAudiences.create(this);
        if (this.bot.getJDA() != null) {
            this.bot.setup();
        } else {
            this.listenerManager.update();
        }
        new SkoiceCommand(this).init();
        this.addCustomCharts();
        this.setupBugsnag();
        new Updater(this, this.getFile().getAbsolutePath());
    }

    public BukkitAudiences adventure() {
        return this.adventure;
    }

    @Override
    public void onDisable() {
        if (this.bot.getJDA() != null) {
            new InterruptSystemTask(this).run();
            this.bot.getJDA().shutdown();
        }
        this.getLogger().info(this.lang.getMessage("logger.info.plugin-disabled"));
        if (this.adventure != null) {
            this.adventure.close();
        }
    }

    private void addCustomCharts() {
        Metrics metrics = new Metrics(this, Skoice.SERVICE_ID);
        Map<String, ConfigField> configFields = getSharedConfigFields();
        configFields.forEach((name, field) -> metrics.addCustomChart(new SimplePie(name, () ->
                this.configYamlFile.getString(field.toString())
        )));

        Map<String, Integer> intConfigFields = getSharedIntConfigFields();
        intConfigFields.forEach((name, value) -> metrics.addCustomChart(ChartUtil.createDrilldownPie(name, value, 0, 10, 11)));

        int linkedUsers = this.linksYamlFile.getLinks().size();
        metrics.addCustomChart(ChartUtil.createDrilldownPie("linkedUsers", linkedUsers, 0, 10, 11));

        metrics.addCustomChart(new SimplePie("botStatus", () -> this.bot.getStatus().toString()));
    }

    private void setupBugsnag() {
        if (Skoice.BUGSNAG_KEY.isEmpty()) {
            return;
        }
        Bugsnag bugsnag = new Bugsnag(Skoice.BUGSNAG_KEY);
        bugsnag.setAppVersion(this.getDescription().getVersion());

        bugsnag.addCallback(report -> {
            report.setUserId(this.configYamlFile.getString(ConfigField.SERVER_ID.toString()));
            report.addToTab("server", "version", this.getServer().getVersion());
            report.addToTab("server", "bukkitVersion", this.getServer().getBukkitVersion());

            Map<String, ConfigField> sharedConfigFields = getSharedConfigFields();
            sharedConfigFields.forEach((name, field) -> report.addToTab("app", name, this.configYamlFile.getString(field.toString())));

            Map<String, Integer> sharedIntConfigFields = getSharedIntConfigFields();
            sharedIntConfigFields.forEach((name, value) -> report.addToTab("app", name, value));

            int linkedUsers = this.linksYamlFile.getLinks().size();
            report.addToTab("app", "linkedUsers", linkedUsers);

            report.addToTab("app", "botStatus", this.bot.getStatus().toString());
        });
    }

    private Map<String, ConfigField> getSharedConfigFields() {
        Map<String, ConfigField> fields = new HashMap<>();
        fields.put("lang", ConfigField.LANG);
        fields.put("loginNotification", ConfigField.LOGIN_NOTIFICATION);
        fields.put("actionBarAlert", ConfigField.ACTION_BAR_ALERT);
        fields.put("tooltips", ConfigField.TOOLTIPS);
        fields.put("corpsesIncluded", ConfigField.CORPSES_INCLUDED);
        fields.put("spectatorsIncluded", ConfigField.SPECTATORS_INCLUDED);
        fields.put("channelVisibility", ConfigField.CHANNEL_VISIBILITY);
        return fields;
    }

    private Map<String, Integer> getSharedIntConfigFields() {
        Map<String, Integer> fields = new HashMap<>();
        if (this.configYamlFile.contains(ConfigField.HORIZONTAL_RADIUS.toString())) {
            fields.put("horizontalRadius", this.configYamlFile.getInt(ConfigField.HORIZONTAL_RADIUS.toString()));
        }
        if (this.configYamlFile.contains(ConfigField.VERTICAL_RADIUS.toString())) {
            fields.put("verticalRadius", this.configYamlFile.getInt(ConfigField.VERTICAL_RADIUS.toString()));
        }
        return fields;
    }

    public Lang getLang() {
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

    public Bot getBot() {
        return this.bot;
    }

    public BotCommands getBotCommands() {
        return this.botCommands;
    }

    public ConfigurationMenu getConfigurationMenu() {
        return this.configurationMenu;
    }
}
