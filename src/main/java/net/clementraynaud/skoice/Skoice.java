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

package net.clementraynaud.skoice;

import net.clementraynaud.skoice.bot.Connection;
import net.clementraynaud.skoice.configuration.minecraft.IncorrectConfigurationAlert;
import net.clementraynaud.skoice.configuration.minecraft.Instructions;
import net.clementraynaud.skoice.configuration.minecraft.TokenRetrieval;
import net.clementraynaud.skoice.link.Link;
import net.clementraynaud.skoice.link.Unlink;
import net.clementraynaud.skoice.configuration.discord.MessageManagement;
import net.clementraynaud.skoice.system.ChannelManagement;
import net.clementraynaud.skoice.system.MarkPlayersDirty;
import net.clementraynaud.skoice.system.Network;
import net.clementraynaud.skoice.util.Lang;
import net.clementraynaud.skoice.util.UpdateChecker;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public class Skoice extends JavaPlugin {

    private static Skoice plugin;
    private static Connection bot;
    private final FileConfiguration configFile = getConfig();
    private boolean isTokenSet = configFile.contains("token");
    private boolean isBotReady = false;
    private boolean isGuildUnique;

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
        saveDefaultConfig();
        registerDefaultValues();
        setPlugin(this);
        getLogger().info(Lang.Console.PLUGIN_ENABLED_INFO.print());
        setBot(new Connection());
        plugin.getCommand("configure").setExecutor(new Instructions());
        plugin.getCommand("token").setExecutor(new TokenRetrieval());
        plugin.getCommand("link").setExecutor(new Link());
        plugin.getCommand("unlink").setExecutor(new Unlink());
        checkVersion();
    }

    private void registerDefaultValues() {
        if (!configFile.contains("action-bar-alert")) {
            configFile.set("action-bar-alert", true);
            saveConfig();
        }
        if (!configFile.contains("channel-visibility")) {
            configFile.set("channel-visibility", false);
            saveConfig();
        }
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
        boolean wasBotReady = isBotReady;
        if (!configFile.contains("token")) {
            isTokenSet = false;
            isBotReady = false;
            getLogger().warning(Lang.Console.NO_TOKEN_WARNING.print());
        } else if (Connection.getJda() == null) {
            isBotReady = false;
        } else if (!configFile.contains("language")) {
            isBotReady = false;
            getLogger().warning(Lang.Console.NO_LANGUAGE_WARNING.print());
        } else if (!isGuildUnique()) {
            isBotReady = false;
            getLogger().warning(Lang.Console.MULTIPLE_GUILDS_WARNING.print());
        } else if (!configFile.contains("lobby-id")) {
            isBotReady = false;
            getLogger().warning(Lang.Console.NO_LOBBY_ID_WARNING.print());
        } else if (!configFile.contains("radius.horizontal")
                || !configFile.contains("radius.vertical")) {
            isBotReady = false;
            getLogger().warning(Lang.Console.NO_DISTANCES_WARNING.print());
        } else {
            isBotReady = true;
        }
        updateListeners(startup, wasBotReady);
    }

    private void updateListeners(boolean startup, boolean wasBotReady) {
        if (startup) {
            if (isBotReady) {
                Bukkit.getPluginManager().registerEvents(new MarkPlayersDirty(), plugin);
                Bukkit.getPluginManager().registerEvents(new ChannelManagement(), plugin);
                Connection.getJda().addEventListener(new ChannelManagement(), new MarkPlayersDirty());
                Connection.getJda().getPresence().setActivity(Activity.listening("/link"));
                getLogger().info("STARTUP IG");
            } else {
                Bukkit.getPluginManager().registerEvents(new IncorrectConfigurationAlert(), plugin);
                if (Connection.getJda() != null)
                    Connection.getJda().getPresence().setActivity(Activity.listening("/configure"));
                getLogger().info("STARTUP CONFIG");
            }
        } else if (!wasBotReady && isBotReady) {
            HandlerList.unregisterAll(new IncorrectConfigurationAlert());
            Bukkit.getPluginManager().registerEvents(new MarkPlayersDirty(), plugin);
            Bukkit.getPluginManager().registerEvents(new ChannelManagement(), plugin);
            Connection.getJda().addEventListener(new ChannelManagement(), new MarkPlayersDirty());
            Connection.getJda().getPresence().setActivity(Activity.listening("/link"));
            getLogger().info(Lang.Console.CONFIGURATION_COMPLETE_INFO.print());
            getLogger().info("IG");
        } else if (wasBotReady && !isBotReady) {
            MessageManagement.deleteConfigurationMessage();
            HandlerList.unregisterAll(new MarkPlayersDirty());
            HandlerList.unregisterAll(new ChannelManagement());
            Bukkit.getPluginManager().registerEvents(new IncorrectConfigurationAlert(), plugin);
            if (Connection.getJda() != null) {
                Connection.getJda().removeEventListener(new ChannelManagement(), new MarkPlayersDirty());
                Connection.getJda().getPresence().setActivity(Activity.listening("/configure"));
            }
            getLogger().info("CONFIG");
        }
    }

    @Override
    public void onDisable() {
        if (Connection.getJda() != null) {
            for (Pair<String, CompletableFuture<Void>> value : ChannelManagement.awaitingMoves.values()) {
                value.getRight().cancel(true);
            }
            for (Network network : ChannelManagement.networks) {
                for (Member member : network.getChannel().getMembers()) {
                    member.mute(false).queue();
                }
                network.getChannel().delete().queue();
                network.clear();
            }
            ChannelManagement.networks.clear();
            try {
                Connection.getJda().shutdown();
            } catch (NoClassDefFoundError ignored) {
            }
            getLogger().info(Lang.Console.PLUGIN_DISABLED_INFO.print());
        }
    }
}
