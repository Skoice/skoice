/*
 * Copyright 2020, 2021 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
 * Copyright 2016, 2017, 2018, 2019, 2020, 2021 Austin "Scarsz" Shapiro
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

package net.clementraynaud;

import net.clementraynaud.bot.Connection;
import net.clementraynaud.configuration.minecraft.IncorrectConfigurationAlert;
import net.clementraynaud.configuration.minecraft.Instructions;
import net.clementraynaud.configuration.minecraft.TokenRetrieval;
import net.clementraynaud.link.Link;
import net.clementraynaud.link.Unlink;
import net.clementraynaud.system.ChannelManagement;
import net.clementraynaud.system.MarkPlayersDirty;
import net.clementraynaud.system.Network;
import net.clementraynaud.util.Lang;
import net.clementraynaud.util.UpdateChecker;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

import static net.clementraynaud.bot.Connection.getJda;
import static net.clementraynaud.configuration.discord.MessageManagement.deleteConfigurationMessage;
import static net.clementraynaud.system.ChannelManagement.networks;

public class Skoice extends JavaPlugin {

    private static Skoice plugin;
    private static Connection bot;
    private final FileConfiguration configFile = getConfig();
    private boolean isTokenSet = true;
    private boolean isBotConfigured = false;

    public static Skoice getPlugin() {
        return plugin;
    }

    public static void setPlugin(Skoice plugin) {
        Skoice.plugin = plugin;
    }

    public static Connection getBot() {
        return bot;
    }

    public static void setBot(Connection bot) {
        Skoice.bot = bot;
    }

    public FileConfiguration getConfigFile() {
        return configFile;
    }

    public boolean isTokenSet() {
        return isTokenSet;
    }

    public boolean isBotConfigured() {
        return isBotConfigured;
    }

    @Override
    public void onEnable() {
        new Metrics(this, 11380);
        saveDefaultConfig();
        if (!configFile.contains("action-bar-alert")) {
            configFile.set("action-bar-alert", true);
            saveConfig();
        }
        if (!configFile.contains("channel-visibility")) {
            configFile.set("channel-visibility", false);
            saveConfig();
        }
        getLogger().info(ChatColor.YELLOW + "Plugin enabled!");
        setPlugin(this);
        updateConfigurationStatus(true);
        setBot(new Connection());
        plugin.getCommand("configure").setExecutor(new Instructions());
        plugin.getCommand("token").setExecutor(new TokenRetrieval());
        plugin.getCommand("link").setExecutor(new Link());
        plugin.getCommand("unlink").setExecutor(new Unlink());
        checkVersion();
    }

    public void checkVersion() {
        new UpdateChecker(this, 82861).getVersion(version -> {
            if (!this.getDescription().getVersion().equals(version)) {
                getLogger().warning(Lang.Console.OUTDATED_VERSION_WARNING.print()
                        .replace("{runningVersion}", this.getDescription().getVersion())
                        .replace("{latestVersion}", version));
            }
        });
    }

    public void updateConfigurationStatus(boolean startup) {
        boolean oldBotConfigured = isBotConfigured;
        if (!configFile.contains("token")) {
            getLogger().warning(Lang.Console.NO_TOKEN_WARNING.print());
            isTokenSet = false;
            isBotConfigured = false;
        } else if (!configFile.contains("language")) {
            getLogger().warning(Lang.Console.NO_LANGUAGE_WARNING.print());
            isBotConfigured = false;
        } else if (!configFile.contains("lobby-id")) {
            getLogger().warning(Lang.Console.NO_LOBBY_ID_WARNING.print());
            isBotConfigured = false;
        } else if (!configFile.contains("radius.horizontal")
                || !configFile.contains("radius.vertical")) {
            getLogger().warning(Lang.Console.NO_DISTANCES_WARNING.print());
            isBotConfigured = false;
        } else {
            isBotConfigured = true;
        }
        updateListeners(startup, oldBotConfigured);
    }

    private void updateListeners(boolean startup, boolean oldBotConfigured) {
        if (startup) {
            Bukkit.getPluginManager().registerEvents(new IncorrectConfigurationAlert(), plugin);
            if (isBotConfigured) {
                Bukkit.getPluginManager().registerEvents(new MarkPlayersDirty(), plugin);
                Bukkit.getPluginManager().registerEvents(new ChannelManagement(), plugin);
            }
        } else if (!oldBotConfigured && isBotConfigured && getJda() != null && getBot().isGuildUnique()) {
            Bukkit.getPluginManager().registerEvents(new MarkPlayersDirty(), plugin);
            Bukkit.getPluginManager().registerEvents(new ChannelManagement(), plugin);
            getJda().addEventListener(new ChannelManagement(), new MarkPlayersDirty());
            getJda().getPresence().setActivity(Activity.listening("/link"));
        } else if (oldBotConfigured && !isBotConfigured) {
            deleteConfigurationMessage();
            HandlerList.unregisterAll(new MarkPlayersDirty());
            HandlerList.unregisterAll(new ChannelManagement());
            if (getJda() != null) {
                getJda().removeEventListener(new ChannelManagement(), new MarkPlayersDirty());
                getJda().getPresence().setActivity(Activity.listening("/configure"));
            }
        }
    }

    @Override
    public void onDisable() {
        if (getJda() != null) {
            for (Pair<String, CompletableFuture<Void>> value : ChannelManagement.awaitingMoves.values()) {
                value.getRight().cancel(true);
            }
            for (Network network : networks) {
                for (Member member : network.getChannel().getMembers()) {
                    member.mute(false).queue();
                }
                network.getChannel().delete().queue();
                network.clear();
            }
            networks.clear();
            try {
                getJda().shutdown();
            } catch (NoClassDefFoundError ignored) {
            }
            getLogger().info(Lang.Console.PLUGIN_DISABLED_INFO.print());
        }
    }
}
