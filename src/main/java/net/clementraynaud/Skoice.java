// Copyright 2020, 2021 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
// Copyright 2016, 2017, 2018, 2019, 2020, 2021 Austin "Scarsz" Shapiro

// This file is part of Skoice.

// Skoice is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Skoice is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Skoice.  If not, see <https://www.gnu.org/licenses/>.


package net.clementraynaud;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.clementraynaud.configuration.minecraft.IncorrectConfigurationAlert;
import net.clementraynaud.configuration.minecraft.Instructions;
import net.clementraynaud.configuration.minecraft.TokenRetrieval;
import net.clementraynaud.link.Link;
import net.clementraynaud.link.Unlink;
import net.clementraynaud.system.ChannelManagement;
import net.clementraynaud.system.MarkPlayersDirty;
import net.clementraynaud.system.Network;
import net.clementraynaud.util.Lang;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.*;

import static net.clementraynaud.Bot.*;
import static net.clementraynaud.configuration.discord.MessageManagement.*;
import static net.clementraynaud.system.ChannelManagement.networks;
import static net.clementraynaud.util.DataGetters.*;

public class Skoice extends JavaPlugin {

    private static Skoice plugin;
    private final FileConfiguration configFile = getConfig();
    private static Bot bot;
    private boolean isTokenSet = true;
    private boolean isBotConfigured = false;

    public static void setPlugin(Skoice plugin) {
        Skoice.plugin = plugin;
    }

    public static void setBot(Bot bot) {
        Skoice.bot = bot;
    }

    public static Skoice getPlugin() {
        return plugin;
    }

    public FileConfiguration getConfigFile() {
        return configFile;
    }

    public static Bot getBot() {
        return bot;
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
        }
        saveConfig();
        getLogger().info(ChatColor.YELLOW + "Plugin enabled!");
        getLogger().info(ChatColor.YELLOW + "Checking version now.");
        checkVersion();
        setPlugin(this);
        updateConfigurationStatus(true);
        setBot(new Bot());
        plugin.getCommand("configure").setExecutor(new Instructions());
        plugin.getCommand("token").setExecutor(new TokenRetrieval());
        plugin.getCommand("link").setExecutor(new Link());
        plugin.getCommand("unlink").setExecutor(new Unlink());
    }

    public void checkVersion() {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/carlodrift/skoice/main/version").openStream()))) {
            String runningVersion = "v" + this.getDescription().getVersion();
            String latestVersion = bufferedReader.readLine();
            if (!runningVersion.equals(latestVersion)) {
                getLogger().warning(ChatColor.RED + "You are using an outdated version!");
                getLogger().warning("Latest version: " + ChatColor.GREEN + latestVersion + ChatColor.YELLOW + ". You are on version: " + ChatColor.RED + runningVersion + ChatColor.YELLOW + ".");
                getLogger().warning("Update here: " + ChatColor.AQUA + "https://www.spigotmc.org/resources/skoice-proximity-voice-chat.82861/");
            } else {
                getLogger().info(ChatColor.GREEN + "You are using the latest version!");
            }
        } catch (IOException e) {
            getLogger().warning("An error occurred while checking the version.");
        }
    }

    public void updateConfigurationStatus(boolean startup) {
        boolean oldBotConfigured = isBotConfigured;
        if (configFile.getString("token") == null) {
            getLogger().warning(Lang.ConsoleMessage.NO_TOKEN.print());
            isTokenSet = false;
            isBotConfigured = false;
        } else if (getLanguage() == null) {
            getLogger().warning(Lang.ConsoleMessage.NO_LANGUAGE.print());
            isBotConfigured = false;
        } else if (configFile.getString("lobby-id") == null) {
            getLogger().warning(Lang.ConsoleMessage.NO_LOBBY_ID.print());
            isBotConfigured = false;
        } else if (getVerticalRadius() == 0
                || getHorizontalRadius() == 0) {
            getLogger().warning(Lang.ConsoleMessage.NO_DISTANCES.print());
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
        } else if (!oldBotConfigured && isBotConfigured && getJda() != null) {
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
        getJda().shutdown();
        getLogger().info(ChatColor.YELLOW + "Plugin disabled!");
    }

}
