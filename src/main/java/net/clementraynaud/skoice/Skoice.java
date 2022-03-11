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
import net.clementraynaud.skoice.commands.interaction.Response;
import net.clementraynaud.skoice.commands.menus.Menu;
import net.clementraynaud.skoice.config.OutdatedConfig;
import net.clementraynaud.skoice.listeners.VoiceChannelDeleteListener;
import net.clementraynaud.skoice.listeners.guild.GuildVoiceJoinListener;
import net.clementraynaud.skoice.listeners.guild.GuildVoiceLeaveListener;
import net.clementraynaud.skoice.listeners.guild.GuildVoiceMoveListener;
import net.clementraynaud.skoice.listeners.player.DirtyPlayerListeners;
import net.clementraynaud.skoice.listeners.player.PlayerJoinListener;
import net.clementraynaud.skoice.listeners.player.PlayerQuitListener;
import net.clementraynaud.skoice.lang.LoggerLang;
import net.clementraynaud.skoice.networks.Network;
import net.clementraynaud.skoice.scheduler.UpdateNetworks;
import net.clementraynaud.skoice.util.UpdateUtil;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static net.clementraynaud.skoice.bot.Bot.getJda;
import static net.clementraynaud.skoice.config.Config.*;

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
        getLogger().info(LoggerLang.PLUGIN_ENABLED_INFO.toString());
        getConfig().options().copyDefaults(true);
        saveConfig();
        new OutdatedConfig().update();
        isTokenSet = getConfig().contains(TOKEN_FIELD);
        setBot(new Bot());
        plugin.getCommand("skoice").setExecutor(new SkoiceCommand());
        checkVersion();
    }

    public void checkVersion() {
        new UpdateUtil(this, 82861).getVersion(version -> {
            if (!this.getDescription().getVersion().equals(version)) {
                getLogger().warning(String.format(LoggerLang.OUTDATED_VERSION_WARNING.toString(),
                        this.getDescription().getVersion(), version));
            }
        });
    }

    public void updateConfigurationStatus(boolean startup) {
        boolean wasBotReady = isBotReady;
        if (!getConfig().contains(TOKEN_FIELD)) {
            isTokenSet = false;
            isBotReady = false;
            getLogger().warning(LoggerLang.NO_TOKEN_WARNING.toString());
        } else if (getJda() == null) {
            isBotReady = false;
        } else if (!isGuildUnique()) {
            isBotReady = false;
            getLogger().warning(LoggerLang.MULTIPLE_GUILDS_WARNING.toString());
        } else if (!getConfig().contains(LOBBY_ID_FIELD)) {
            isBotReady = false;
            getLogger().warning(LoggerLang.NO_LOBBY_ID_WARNING.toString());
        } else if (!getConfig().contains(HORIZONTAL_RADIUS_FIELD)
                || !getConfig().contains(VERTICAL_RADIUS_FIELD)) {
            isBotReady = false;
            getLogger().warning(LoggerLang.NO_RADIUS_WARNING.toString());
        } else {
            isBotReady = true;
        }
        updateActivity();
        updateListeners(startup, wasBotReady);
    }

    private void updateActivity() {
        if (getJda() != null) {
            Activity activity = getJda().getPresence().getActivity();
            if (isBotReady && !Objects.equals(activity, Activity.listening("link")))
                getJda().getPresence().setActivity(Activity.listening("/link"));
            else if (!isBotReady && !Objects.equals(activity, Activity.listening("configure")))
                getJda().getPresence().setActivity(Activity.listening("/configure"));
        }
    }

    private void updateListeners(boolean startup, boolean wasBotReady) {
        if (startup) {
            if (isBotReady) {
                Bukkit.getPluginManager().registerEvents(new DirtyPlayerListeners(), plugin);
                Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), plugin);
                getJda().addEventListener(new GuildVoiceJoinListener(), new GuildVoiceLeaveListener(), new GuildVoiceMoveListener(), new VoiceChannelDeleteListener());
            } else {
                Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
                if (getJda() != null)
                    Menu.MODE.refreshAdditionalFields();
            }
        } else if (!wasBotReady && isBotReady) {
            HandlerList.unregisterAll(new PlayerJoinListener());
            Bukkit.getPluginManager().registerEvents(new DirtyPlayerListeners(), plugin);
            Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(), plugin);
            getJda().addEventListener(new GuildVoiceJoinListener(), new GuildVoiceLeaveListener(), new GuildVoiceMoveListener(), new VoiceChannelDeleteListener());
            Menu.MODE.refreshAdditionalFields();
            getLogger().info(LoggerLang.CONFIGURATION_COMPLETE_INFO.toString());
        } else if (wasBotReady && !isBotReady) {
            new Response().deleteMessage();
            HandlerList.unregisterAll(new DirtyPlayerListeners());
            HandlerList.unregisterAll(new PlayerQuitListener());
            Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), plugin);
            if (getJda() != null) {
                getJda().removeEventListener(new GuildVoiceJoinListener(), new GuildVoiceLeaveListener(), new GuildVoiceMoveListener(), new VoiceChannelDeleteListener());
                Menu.MODE.refreshAdditionalFields();
            }
        }
    }

    @Override
    public void onDisable() {
        if (getJda() != null) {
            for (Pair<String, CompletableFuture<Void>> value : UpdateNetworks.awaitingMoves.values()) {
                value.getRight().cancel(true);
            }
            for (Network network : Network.networks) {
                network.getChannel().delete().queue();
                network.clear();
            }
            Network.networks.clear();
            getJda().shutdown();
        }
        getLogger().info(LoggerLang.PLUGIN_DISABLED_INFO.toString());
    }
}
