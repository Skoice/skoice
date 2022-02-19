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
import net.clementraynaud.skoice.commands.interaction.MessageManagement;
import net.clementraynaud.skoice.events.VoiceChannelDeleteEvent;
import net.clementraynaud.skoice.events.guild.GuildVoiceJoinEvent;
import net.clementraynaud.skoice.events.guild.GuildVoiceLeaveEvent;
import net.clementraynaud.skoice.events.guild.GuildVoiceMoveEvent;
import net.clementraynaud.skoice.events.player.PlayerJoinEvent;
import net.clementraynaud.skoice.events.player.PlayerQuitEvent;
import net.clementraynaud.skoice.lang.Logger;
import net.clementraynaud.skoice.commands.SkoiceCommand;
import net.clementraynaud.skoice.scheduler.UpdateNetworks;
import net.clementraynaud.skoice.events.player.DirtyPlayerEvents;
import net.clementraynaud.skoice.networks.NetworkManager;
import net.clementraynaud.skoice.config.OutdatedConfig;
import net.clementraynaud.skoice.util.UpdateUtil;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

import static net.clementraynaud.skoice.bot.Bot.getJda;

public class Skoice extends JavaPlugin {

    private static Skoice plugin;
    private static Bot bot;
    private boolean isTokenSet;
    private boolean isBotReady;
    private boolean isGuildUnique;

    public static Skoice getPlugin() {
        return plugin;
    }

    public static void setPlugin(Skoice plugin) {
        Skoice.plugin = plugin;
    }

    public static Bot getBot() {
        return bot;
    }

    public static void setBot(Bot bot) {
        Skoice.bot = bot;
    }

    public boolean isTokenSet() {
        return isTokenSet;
    }

    public void setTokenBoolean(boolean isTokenSet) {
        this.isTokenSet = isTokenSet;
    }

    public boolean isBotReady() {
        return isBotReady;
    }

    public boolean isGuildUnique() {
        return isGuildUnique;
    }

    public void setGuildUnique(boolean guildUnique) {
        isGuildUnique = guildUnique;
    }

    @Override
    public void onEnable() {
        new Metrics(this, 11380);
        setPlugin(this);
        getLogger().info(Logger.PLUGIN_ENABLED_INFO.toString());
        getConfig().options().copyDefaults(true);
        saveConfig();
        new OutdatedConfig().update();
        isTokenSet = getConfig().contains("token");
        setBot(new Bot());
        plugin.getCommand("skoice").setExecutor(new SkoiceCommand());
        checkVersion();
    }

    public void checkVersion() {
        new UpdateUtil(this, 82861).getVersion(version -> {
            if (!this.getDescription().getVersion().equals(version)) {
                getLogger().warning(Logger.OUTDATED_VERSION_WARNING.toString()
                        .replace("{runningVersion}", this.getDescription().getVersion())
                        .replace("{latestVersion}", version));
            }
        });
    }

    public void updateConfigurationStatus(boolean startup) {
        boolean wasBotReady = isBotReady;
        if (!getConfig().contains("token")) {
            isTokenSet = false;
            isBotReady = false;
            getLogger().warning(Logger.NO_TOKEN_WARNING.toString());
        } else if (getJda() == null) {
            isBotReady = false;
        } else if (!getConfig().contains("lang")) {
            isBotReady = false;
            getLogger().warning(Logger.NO_LANGUAGE_WARNING.toString());
        } else if (!isGuildUnique()) {
            isBotReady = false;
            getLogger().warning(Logger.MULTIPLE_GUILDS_WARNING.toString());
        } else if (!getConfig().contains("lobby-id")) {
            isBotReady = false;
            getLogger().warning(Logger.NO_LOBBY_ID_WARNING.toString());
        } else if (!getConfig().contains("radius.horizontal")
                || !getConfig().contains("radius.vertical")) {
            isBotReady = false;
            getLogger().warning(Logger.NO_RADIUS_WARNING.toString());
        } else {
            isBotReady = true;
        }
        updateListeners(startup, wasBotReady);
    }

    private void updateListeners(boolean startup, boolean wasBotReady) {
        if (startup) {
            if (isBotReady) {
                Bukkit.getPluginManager().registerEvents(new DirtyPlayerEvents(), plugin);
                Bukkit.getPluginManager().registerEvents(new PlayerQuitEvent(), plugin);
                getJda().addEventListener(new GuildVoiceJoinEvent(), new GuildVoiceLeaveEvent(), new GuildVoiceMoveEvent(), new VoiceChannelDeleteEvent());
                getJda().getPresence().setActivity(Activity.listening("/link"));
            } else {
                Bukkit.getPluginManager().registerEvents(new PlayerJoinEvent(), plugin);
                if (getJda() != null)
                    getJda().getPresence().setActivity(Activity.listening("/configure"));
            }
        } else if (!wasBotReady && isBotReady) {
            HandlerList.unregisterAll(new PlayerJoinEvent());
            Bukkit.getPluginManager().registerEvents(new DirtyPlayerEvents(), plugin);
            Bukkit.getPluginManager().registerEvents(new PlayerQuitEvent(), plugin);
            getJda().addEventListener(new GuildVoiceJoinEvent(), new GuildVoiceLeaveEvent(), new GuildVoiceMoveEvent(), new VoiceChannelDeleteEvent());
            getJda().getPresence().setActivity(Activity.listening("/link"));
            getLogger().info(Logger.CONFIGURATION_COMPLETE_INFO.toString());
        } else if (wasBotReady && !isBotReady) {
            MessageManagement.deleteConfigurationMessage();
            HandlerList.unregisterAll(new DirtyPlayerEvents());
            HandlerList.unregisterAll(new PlayerQuitEvent());
            Bukkit.getPluginManager().registerEvents(new PlayerJoinEvent(), plugin);
            if (getJda() != null) {
                getJda().removeEventListener(new GuildVoiceJoinEvent(), new GuildVoiceLeaveEvent(), new GuildVoiceMoveEvent(), new VoiceChannelDeleteEvent());
                getJda().getPresence().setActivity(Activity.listening("/configure"));
            }
        }
    }

    @Override
    public void onDisable() {
        if (getJda() != null) {
            for (Pair<String, CompletableFuture<Void>> value : UpdateNetworks.awaitingMoves.values()) {
                value.getRight().cancel(true);
            }
            for (NetworkManager network : NetworkManager.networks) {
                for (Member member : network.getChannel().getMembers()) {
                    member.mute(false).queue();
                }
                network.getChannel().delete().queue();
                network.clear();
            }
            NetworkManager.networks.clear();
            try {
                getJda().shutdown();
            } catch (NoClassDefFoundError ignored) {
            }
            getLogger().info(Logger.PLUGIN_DISABLED_INFO.toString());
        }
    }
}
