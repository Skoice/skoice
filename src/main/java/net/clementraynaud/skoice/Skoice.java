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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private Bugsnag bugsnag;

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

        this.getSharedConfigFields().forEach(field ->
                metrics.addCustomChart(new SimplePie(field.toCamelCase(), () ->
                        this.configYamlFile.getString(field.toString())
                ))
        );

        this.getSharedIntConfigFields().forEach(field ->
                metrics.addCustomChart(ChartUtil.createDrilldownPie(field.toCamelCase(),
                        this.configYamlFile.getInt(field.toString()), 0, 10, 11)
                )
        );

        int linkedUsers = this.linksYamlFile.getLinks().size();
        metrics.addCustomChart(ChartUtil.createDrilldownPie("linkedUsers", linkedUsers, 0, 10, 11));

        metrics.addCustomChart(new SimplePie("botStatus", () -> this.bot.getStatus().toString()));
    }

    private void setupBugsnag() {
        if (Skoice.BUGSNAG_KEY.isEmpty()) {
            return;
        }
        this.bugsnag = new Bugsnag(Skoice.BUGSNAG_KEY);
        this.bugsnag.setAppVersion(this.getDescription().getVersion());

        this.bugsnag.addCallback(report -> {
            StackTraceElement[] trace = report.getException().getStackTrace();
            boolean reportError = false;
            for (StackTraceElement element : trace) {
                if (element.getClassName().startsWith("net.clementraynaud.skoice")) {
                    reportError = true;
                    break;
                }
            }
            if (!reportError) {
                report.cancel();
                return;
            }

            report.setUserId(this.configYamlFile.getString(ConfigField.SERVER_ID.toString()));
            report.addToTab("server", "version", this.getServer().getVersion());
            report.addToTab("server", "bukkitVersion", this.getServer().getBukkitVersion());

            this.getSharedConfigFields().forEach(field ->
                    report.addToTab("app", field.toCamelCase(), this.configYamlFile.getString(field.toString()))
            );

            this.getSharedIntConfigFields().forEach(field ->
                    report.addToTab("app", field.toCamelCase(), this.configYamlFile.getInt(field.toString()))
            );

            report.addToTab("app", ConfigField.LANG.toCamelCase(), LangInfo.valueOf(this.configYamlFile.getString(ConfigField.LANG.toString())).getFullName());

            int linkedUsers = this.linksYamlFile.getLinks().size();
            report.addToTab("app", "linkedUsers", linkedUsers);
            report.addToTab("app", "botStatus", this.bot.getStatus().toString());
        });

        this.bugsnag.startSession();
    }

    private Set<ConfigField> getSharedConfigFields() {
        return Stream.of(
                ConfigField.LANG,
                ConfigField.LOGIN_NOTIFICATION,
                ConfigField.CONNECTING_ALERT,
                ConfigField.DISCONNECTING_ALERT,
                ConfigField.TOOLTIPS,
                ConfigField.PLAYERS_ON_DEATH_SCREEN_INCLUDED,
                ConfigField.SPECTATORS_INCLUDED,
                ConfigField.CHANNEL_VISIBILITY
        ).collect(Collectors.toSet());
    }

    private Set<ConfigField> getSharedIntConfigFields() {
        Set<ConfigField> fields = new HashSet<>();
        if (this.configYamlFile.contains(ConfigField.HORIZONTAL_RADIUS.toString())) {
            fields.add(ConfigField.HORIZONTAL_RADIUS);
        }
        if (this.configYamlFile.contains(ConfigField.VERTICAL_RADIUS.toString())) {
            fields.add(ConfigField.VERTICAL_RADIUS);
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

    public Bugsnag getBugsnag() {
        return this.bugsnag;
    }
}
