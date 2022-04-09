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
import net.clementraynaud.skoice.commands.SkoiceCommand;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.clementraynaud.skoice.menus.Response;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerMoveListener;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerTeleportListener;
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.config.OutdatedConfig;
import net.clementraynaud.skoice.listeners.channel.voice.network.VoiceChannelDeleteListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceJoinListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceLeaveListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceMoveListener;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerJoinListener;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerQuitListener;
import net.clementraynaud.skoice.lang.LoggerLang;
import net.clementraynaud.skoice.tasks.InterruptSystemTask;
import net.clementraynaud.skoice.util.UpdateUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.util.Objects;

public class Skoice extends JavaPlugin {

    private static Skoice plugin;
    private static Bot bot;
    private boolean isTokenSet;
    private boolean isBotReady;
    private boolean isGuildUnique;

    public static Skoice getPlugin() {
        return Skoice.plugin;
    }

    public static void setPlugin(Skoice plugin) {
        Skoice.plugin = plugin;
    }

    public static Bot getBot() {
        return Skoice.bot;
    }

    public static void setBot(Bot bot) {
        Skoice.bot = bot;
    }

    public boolean isTokenSet() {
        return this.isTokenSet;
    }

    public void setTokenBoolean(boolean isTokenSet) {
        this.isTokenSet = isTokenSet;
    }

    public boolean isBotReady() {
        return this.isBotReady;
    }

    public boolean isGuildUnique() {
        return this.isGuildUnique;
    }

    public void setGuildUnique(boolean guildUnique) {
        this.isGuildUnique = guildUnique;
    }

    @Override
    public void onEnable() {
        new Metrics(this, 11380);
        Skoice.setPlugin(this);
        this.getLogger().info(LoggerLang.PLUGIN_ENABLED_INFO.toString());
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
        new OutdatedConfig().update();
        this.isTokenSet = this.getConfig().contains(Config.TOKEN_FIELD);
        Skoice.setBot(new Bot());
        SkoiceCommand skoiceCommand = new SkoiceCommand();
        this.getCommand("skoice").setExecutor(skoiceCommand);
        this.getCommand("skoice").setTabCompleter(skoiceCommand);
        this.checkVersion();
    }

    public void checkVersion() {
        new UpdateUtil(this, 82861).getVersion(version -> {
            if (!this.getDescription().getVersion().equals(version)) {
                this.getLogger().warning(String.format(LoggerLang.OUTDATED_VERSION_WARNING.toString(),
                        this.getDescription().getVersion(), version));
            }
        });
    }

    public void updateConfigurationStatus(boolean startup) {
        boolean wasBotReady = this.isBotReady;
        if (!this.getConfig().contains(Config.TOKEN_FIELD)) {
            this.isTokenSet = false;
            this.isBotReady = false;
            this.getLogger().warning(LoggerLang.NO_TOKEN_WARNING.toString());
        } else if (Bot.getJda() == null) {
            this.isBotReady = false;
        } else if (!this.isGuildUnique()) {
            this.isBotReady = false;
            this.getLogger().warning(LoggerLang.MULTIPLE_GUILDS_WARNING.toString());
        } else if (!this.getConfig().contains(Config.LOBBY_ID_FIELD)) {
            this.isBotReady = false;
            this.getLogger().warning(LoggerLang.NO_LOBBY_ID_WARNING.toString());
        } else if (!this.getConfig().contains(Config.HORIZONTAL_RADIUS_FIELD)
                || !this.getConfig().contains(Config.VERTICAL_RADIUS_FIELD)) {
            this.isBotReady = false;
            this.getLogger().warning(LoggerLang.NO_RADIUS_WARNING.toString());
        } else {
            this.isBotReady = true;
        }
        this.updateActivity();
        this.updateListeners(startup, wasBotReady);
    }

    private void updateActivity() {
        if (Bot.getJda() != null) {
            Activity activity = Bot.getJda().getPresence().getActivity();
            if (this.isBotReady && !Objects.equals(activity, Activity.listening("/link"))) {
                Bot.getJda().getPresence().setActivity(Activity.listening("/link"));
            } else if (!this.isBotReady && !Objects.equals(activity, Activity.listening("/configure"))) {
                Bot.getJda().getPresence().setActivity(Activity.listening("/configure"));
            }
        }
    }

    private void updateListeners(boolean startup, boolean wasBotReady) {
        if (startup) {
            if (this.isBotReady) {
                this.registerEligiblePlayerListeners();
                Bot.getJda().addEventListener(new GuildVoiceJoinListener(), new GuildVoiceLeaveListener(), new GuildVoiceMoveListener(), new VoiceChannelDeleteListener());
            } else {
                Bukkit.getPluginManager().registerEvents(new net.clementraynaud.skoice.listeners.player.PlayerJoinListener(), Skoice.plugin);
                if (Bot.getJda() != null) {
                    Menu.MODE.refreshFields();
                }
            }
        } else if (!wasBotReady && this.isBotReady) {
            HandlerList.unregisterAll(new net.clementraynaud.skoice.listeners.player.PlayerJoinListener());
            this.registerEligiblePlayerListeners();
            Bot.getJda().addEventListener(new GuildVoiceJoinListener(), new GuildVoiceLeaveListener(), new GuildVoiceMoveListener(), new VoiceChannelDeleteListener());
            Menu.MODE.refreshFields();
            this.getLogger().info(LoggerLang.CONFIGURATION_COMPLETE_INFO.toString());
            Message configurationMessage = new Response().getConfigurationMessage();
            if (configurationMessage != null) {
                configurationMessage.getInteraction().getUser().openPrivateChannel().complete()
                        .sendMessageEmbeds(new EmbedBuilder().setTitle(MenuEmoji.GEAR + DiscordLang.CONFIGURATION_EMBED_TITLE.toString())
                                .addField(MenuEmoji.HEAVY_CHECK_MARK + DiscordLang.CONFIGURATION_COMPLETE_FIELD_TITLE.toString(),
                                        DiscordLang.CONFIGURATION_COMPLETE_FIELD_DESCRIPTION.toString(), false)
                                .setColor(Color.GREEN).build())
                        .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
            }
        } else if (wasBotReady && !this.isBotReady) {
            new Response().deleteMessage();
            this.unregisterEligiblePlayerListeners();
            Bukkit.getPluginManager().registerEvents(new net.clementraynaud.skoice.listeners.player.PlayerJoinListener(), Skoice.plugin);
            if (Bot.getJda() != null) {
                Bot.getJda().removeEventListener(new GuildVoiceJoinListener(), new GuildVoiceLeaveListener(), new GuildVoiceMoveListener(), new VoiceChannelDeleteListener());
                Menu.MODE.refreshFields();
            }
            new InterruptSystemTask().run();
        }
    }

    private void registerEligiblePlayerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), Skoice.plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), Skoice.plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(), Skoice.plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerTeleportListener(), Skoice.plugin);
    }

    private void unregisterEligiblePlayerListeners() {
        HandlerList.unregisterAll(new PlayerJoinListener());
        HandlerList.unregisterAll(new PlayerQuitListener());
        HandlerList.unregisterAll(new PlayerMoveListener());
        HandlerList.unregisterAll(new PlayerTeleportListener());
    }

    @Override
    public void onDisable() {
        if (Bot.getJda() != null) {
            new InterruptSystemTask().run();
            Bot.getJda().shutdown();
        }
        this.getLogger().info(LoggerLang.PLUGIN_DISABLED_INFO.toString());
    }
}
