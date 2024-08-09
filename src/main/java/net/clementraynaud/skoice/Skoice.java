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

package net.clementraynaud.skoice;

import net.clementraynaud.skoice.api.SkoiceAPI;
import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.commands.skoice.SkoiceCommand;
import net.clementraynaud.skoice.hooks.discordsrv.DiscordSRVHook;
import net.clementraynaud.skoice.hooks.essentialsx.EssentialsXHook;
import net.clementraynaud.skoice.lang.LangInfo;
import net.clementraynaud.skoice.lang.MinecraftLang;
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
import org.bukkit.GameMode;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Skoice extends JavaPlugin {

    private static final String OUTDATED_MINECRAFT_SERVER_ERROR = "Skoice only supports Minecraft 1.8 or later. Please update your Minecraft server to use the proximity voice chat.";
    private static final int SERVICE_ID = 11380;
    private static SkoiceAPI api;
    private MinecraftLang lang;
    private ConfigYamlFile configYamlFile;
    private LinksYamlFile linksYamlFile;
    private TempYamlFile tempYamlFile;
    private LoginNotificationYamlFile loginNotificationYamlFile;
    private ListenerManager listenerManager;
    private Bot bot;
    private BukkitAudiences adventure;
    private DiscordSRVHook discordSRVHook;
    private EssentialsXHook essentialsXHook;
    private Updater updater;

    public static SkoiceAPI api() {
        return Skoice.api;
    }

    @Override
    public void onEnable() {
        if (!this.isMinecraftServerCompatible()) {
            this.getLogger().severe(Skoice.OUTDATED_MINECRAFT_SERVER_ERROR);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.saveDefaultConfig();
        this.configYamlFile = new ConfigYamlFile(this);
        this.configYamlFile.load();
        this.configYamlFile.saveDefaultValues();
        this.lang = new MinecraftLang();
        this.lang.load(LangInfo.valueOf(this.configYamlFile.getString(ConfigField.LANG.toString())));
        this.getLogger().info(this.lang.getMessage("logger.info.plugin-enabled"));
        this.linksYamlFile = new LinksYamlFile(this);
        this.linksYamlFile.load();
        new OutdatedConfig(this).update();
        this.tempYamlFile = new TempYamlFile(this);
        this.tempYamlFile.load();
        this.loginNotificationYamlFile = new LoginNotificationYamlFile(this);
        this.loginNotificationYamlFile.load();
        Skoice.api = new SkoiceAPI(this);
        this.listenerManager = new ListenerManager(this);
        this.listenerManager.registerPermanentMinecraftListeners();
        this.bot = new Bot(this);
        this.bot.connect();
        this.adventure = BukkitAudiences.create(this);
        new SkoiceCommand(this).init();
        this.discordSRVHook = new DiscordSRVHook(this);
        this.discordSRVHook.initialize();
        this.essentialsXHook = new EssentialsXHook(this);
        this.essentialsXHook.initialize();
        this.addCustomCharts();
        this.updater = new Updater(this, this.getFile().getAbsolutePath());
        this.updater.runUpdaterTaskTimer();
    }

    public BukkitAudiences adventure() {
        return this.adventure;
    }

    @Override
    public void onDisable() {
        if (!this.isMinecraftServerCompatible()) {
            return;
        }
        if (this.bot.getJDA() != null) {
            new InterruptSystemTask(this).run();
            this.bot.getJDA().shutdown();
            try {
                if (!this.bot.getJDA().awaitShutdown(Duration.ofSeconds(5))) {
                    this.bot.getJDA().shutdownNow();
                    this.bot.getJDA().awaitShutdown();
                }
            } catch (InterruptedException ignored) {
            }
        }
        this.getLogger().info(this.lang.getMessage("logger.info.plugin-disabled"));
        if (this.adventure != null) {
            this.adventure.close();
        }
        if (this.discordSRVHook != null) {
            this.discordSRVHook.close();
        }
    }

    private boolean isMinecraftServerCompatible() {
        try {
            GameMode.SPECTATOR.toString();
        } catch (NoSuchFieldError exception) {
            return false;
        }
        return true;
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

    public Bot getBot() {
        return this.bot;
    }

    public Updater getUpdater() {
        return this.updater;
    }

    public DiscordSRVHook getDiscordSRVHook() {
        return this.discordSRVHook;
    }

    public EssentialsXHook getEssentialsXHook() {
        return this.essentialsXHook;
    }
}
