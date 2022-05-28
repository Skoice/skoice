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
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.config.OutdatedConfig;
import net.clementraynaud.skoice.lang.LangInfo;
import net.clementraynaud.skoice.lang.Lang;
import net.clementraynaud.skoice.listeners.channel.voice.network.VoiceChannelDeleteListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceJoinListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceLeaveListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceMoveListener;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerJoinListener;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerMoveListener;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerQuitListener;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerTeleportListener;
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.menus.MenuType;
import net.clementraynaud.skoice.menus.ConfigurationMenu;
import net.clementraynaud.skoice.system.EligiblePlayers;
import net.clementraynaud.skoice.tasks.InterruptSystemTask;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

public class Skoice extends JavaPlugin {

    private static final int SERVICE_ID = 11380;
    private static final int RESSOURCE_ID = 82861;

    private Lang lang;
    private Config config;
    private Bot bot;
    private ConfigurationMenu configurationMenu;
    private EligiblePlayers eligiblePlayers;

    private Updater updater;

    @Override
    public void onEnable() {
        new Metrics(this, Skoice.SERVICE_ID);
        this.config = new Config(this);
        this.config.init();
        this.lang = new Lang();
        this.lang.load(LangInfo.valueOf(this.config.getFile().getString(ConfigField.LANG.get())));
        this.getLogger().info(this.lang.getMessage("logger.info.plugin-enabled"));
        new OutdatedConfig(this).update();
        this.eligiblePlayers = new EligiblePlayers();
        this.bot = new Bot(this);
        this.bot.connect();
        if (this.bot.getJda() != null) {
            this.configurationMenu = new ConfigurationMenu(this);
            this.bot.setup(true, null);
        } else {
            this.updateStatus(true);
        }
        new SkoiceCommand(this).init();
        this.updater = new Updater(this, Skoice.RESSOURCE_ID);
        this.updater.checkVersion();
    }

    @Override
    public void onDisable() {
        if (this.bot.getJda() != null) {
            new InterruptSystemTask(this.config).run();
            this.bot.getJda().shutdown();
        }
        this.getLogger().info(this.lang.getMessage("logger.info.plugin-disabled"));
    }

    public void updateStatus(boolean startup) {
        boolean wasBotReady = this.bot.isReady();
        this.bot.setReady(false);
        if (!this.config.getFile().contains(ConfigField.TOKEN.get())) {
            this.getLogger().warning(this.lang.getMessage("logger.warning.no-token"));
        } else if (this.bot.getJda() != null) {
            if (this.bot.getJda().getGuilds().isEmpty()) {
                this.getLogger().warning(this.lang.getMessage("logger.warning.no-guild",
                        this.bot.getJda().getSelfUser().getApplicationId()));
            } else if (this.bot.isOnMultipleGuilds()) {
                this.getLogger().warning(this.lang.getMessage("logger.warning.multiple-guilds"));
            } else if (!this.bot.getJda().getGuilds().get(0).getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
                this.getLogger().severe(this.lang.getMessage("logger.error.missing-permission",
                        this.bot.getJda().getSelfUser().getApplicationId()));
            } else if (!this.config.getFile().contains(ConfigField.LOBBY_ID.get())) {
                this.getLogger().warning(this.lang.getMessage("logger.warning.no-lobby-id"));
            } else if (!this.config.getFile().contains(ConfigField.HORIZONTAL_RADIUS.get())
                    || !this.config.getFile().contains(ConfigField.VERTICAL_RADIUS.get())) {
                this.getLogger().warning(this.lang.getMessage("logger.warning.no-radius"));
            } else {
                this.bot.setReady(true);
            }
            this.bot.updateActivity();
        }
        this.updateListeners(startup, wasBotReady);
    }

    private void updateListeners(boolean startup, boolean wasBotReady) {
        if (startup) {
            if (this.bot.isReady()) {
                this.registerEligiblePlayerListeners();
                this.bot.registerListeners();
            } else {
                Bukkit.getPluginManager().registerEvents(new net.clementraynaud.skoice.listeners.player.PlayerJoinListener(this), this);
            }
        } else if (!wasBotReady && this.bot.isReady()) {
            HandlerList.unregisterAll(new net.clementraynaud.skoice.listeners.player.PlayerJoinListener(this));
            this.registerEligiblePlayerListeners();
            this.bot.registerListeners();
            this.getLogger().info(this.lang.getMessage("logger.info.configuration-complete"));
            Message message = this.configurationMenu.retrieveMessage();
            if (message != null && message.getInteraction() != null) {
                message.getInteraction().getUser().openPrivateChannel().complete()
                        .sendMessage(new Menu(this, "empty-configuration",
                                Collections.singleton(this.bot.getFields().get("configuration-complete")),
                                MenuType.SUCCESS)
                                .toMessage())
                        .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
            }
        } else if (wasBotReady && !this.bot.isReady()) {
            this.configurationMenu.deleteMessage();
            this.unregisterEligiblePlayerListeners();
            Bukkit.getPluginManager().registerEvents(new net.clementraynaud.skoice.listeners.player.PlayerJoinListener(this), this);
            if (this.bot.getJda() != null) {
                this.bot.unregisterListeners();
            }
            new InterruptSystemTask(this.config).run();
        }
    }

    private void registerEligiblePlayerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(this.eligiblePlayers), this);
        Bukkit.getPluginManager().registerEvents(new PlayerTeleportListener(this.eligiblePlayers), this);
    }

    private void unregisterEligiblePlayerListeners() {
        HandlerList.unregisterAll(new PlayerJoinListener(this));
        HandlerList.unregisterAll(new PlayerQuitListener(this));
        HandlerList.unregisterAll(new PlayerMoveListener(this.eligiblePlayers));
        HandlerList.unregisterAll(new PlayerTeleportListener(this.eligiblePlayers));
    }

    public Lang getLang() {
        return this.lang;
    }

    public Config readConfig() {
        return this.config;
    }

    public Bot getBot() {
        return this.bot;
    }

    public ConfigurationMenu getConfigurationMenu() {
        return this.configurationMenu;
    }

    public EligiblePlayers getEligiblePlayers() {
        return this.eligiblePlayers;
    }

    public Updater getUpdater() {
        return this.updater;
    }
}
