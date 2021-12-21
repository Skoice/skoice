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
import net.clementraynaud.configuration.discord.MessageManagement;
import net.clementraynaud.configuration.minecraft.IncorrectConfigurationAlert;
import net.clementraynaud.configuration.minecraft.Instructions;
import net.clementraynaud.configuration.minecraft.TokenRetrieval;
import net.clementraynaud.link.Link;
import net.clementraynaud.link.Unlink;
import net.clementraynaud.system.ChannelManagement;
import net.clementraynaud.system.MarkPlayersDirty;
import net.clementraynaud.system.Network;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.*;

import static net.clementraynaud.Bot.*;
import static net.clementraynaud.configuration.discord.MessageManagement.*;
import static net.clementraynaud.system.ChannelManagement.networks;
import static net.clementraynaud.util.DataGetters.getLobby;

public class Skoice extends JavaPlugin {

    private static Skoice plugin;
    private FileConfiguration playerData;
    private File data;
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

    public FileConfiguration getPlayerData() {
        return playerData;
    }

    public File getData() {
        return data;
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
        getConfig().options().copyDefaults();
        createConfig();
        getLogger().info(ChatColor.YELLOW + "Plugin enabled!");
        if (playerData.getBoolean("checkVersion.atStartup")) {
            getLogger().info(ChatColor.YELLOW + "Checking version now.");
            checkVersion();
        }
        setPlugin(this);
        updateConfigurationStatus(true);
        setBot(new Bot());
        plugin.getCommand("configure").setExecutor(new Instructions());
        plugin.getCommand("token").setExecutor(new TokenRetrieval());
        plugin.getCommand("link").setExecutor(new Link());
        plugin.getCommand("unlink").setExecutor(new Unlink());
    }

    private void createConfig() {
        data = new File(getDataFolder() + File.separator + "data.yml");
        if (!data.exists()) {
            getLogger().info(ChatColor.LIGHT_PURPLE + "Creating file data.yml");
            this.saveResource("data.yml", false);
        }
        playerData = new YamlConfiguration();
        try {
            playerData.load(data);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
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
            getLogger().warning("An error occurred while checking the version:\n" + Arrays.toString(e.getStackTrace()) + "\nGoing to continue anyway.");
        }
    }

    public void updateConfigurationStatus(boolean startup) {
        boolean oldBotConfigured = isBotConfigured;
        if (playerData.getString("token").equals("")) {
            getLogger().warning(ChatColor.RED + "No bot token detected, join your Minecraft server to set up Skoice.");
            isTokenSet = false;
            isBotConfigured = false;
        } else if (playerData.getString("lobbyID").equals("")) {
            getLogger().warning(ChatColor.RED + "No main voice channel detected, type \"/configure\" on your Discord server to set up Skoice.");
            isBotConfigured = false;
        } else if (playerData.getString("distance.verticalStrength").equals("") || playerData.getString("distance.horizontalStrength").equals("")) {
            getLogger().warning(ChatColor.RED + "Maximum distances not set, type \"/configure\" on your Discord server to set up Skoice.");
            isBotConfigured = false;
        } else {
            isBotConfigured = true;
        }
        updateListeners(startup, oldBotConfigured);
    }

    private void updateListeners(boolean startup, boolean oldBotConfigured) {
        if (startup) {
            if (isBotConfigured) {
                Bukkit.getPluginManager().registerEvents(new MarkPlayersDirty(), plugin);
                Bukkit.getPluginManager().registerEvents(new ChannelManagement(), plugin);
            } else {
                Bukkit.getPluginManager().registerEvents(new IncorrectConfigurationAlert(), plugin);
            }
        } else if (!oldBotConfigured && isBotConfigured) {
            HandlerList.unregisterAll(new IncorrectConfigurationAlert());
            Bukkit.getPluginManager().registerEvents(new MarkPlayersDirty(), plugin);
            Bukkit.getPluginManager().registerEvents(new ChannelManagement(), plugin);
            if (getJda() != null) {
                getJda().addEventListener(new ChannelManagement(), new MarkPlayersDirty());
                getJda().getPresence().setActivity(Activity.listening("/link"));
            }
        } else if (oldBotConfigured && !isBotConfigured) {
            deleteConfigurationMessage();
            Bukkit.getPluginManager().registerEvents(new IncorrectConfigurationAlert(), plugin);
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
        final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("Skoice - Shutdown").build();
        final ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);
        try {
            executor.invokeAll(Collections.singletonList(() -> {
                shutdown();
                if (getJda() != null)
                    getJda().getEventManager().getRegisteredListeners().forEach(listener -> getJda().getEventManager().unregister(listener));
                if (getJda() != null) {
                    CompletableFuture<Void> shutdownTask = new CompletableFuture<>();
                    getJda().addEventListener(new ListenerAdapter() {
                        @Override
                        public void onShutdown(@NotNull ShutdownEvent event) {
                            shutdownTask.complete(null);
                        }
                    });
                    getJda().shutdownNow();
                    setJda(null);
                    try {
                        shutdownTask.get(5, TimeUnit.SECONDS);
                    } catch (TimeoutException e) {
                        getLogger().warning("JDA took too long to shut down, skipping.");
                    }
                }
                return null;
            }), 15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
        executor.shutdownNow();
        getLogger().info(ChatColor.YELLOW + "Plugin disabled!");
    }

    private void shutdown() {
        for (Pair<String, CompletableFuture<Void>> value : ChannelManagement.awaitingMoves.values()) {
            value.getRight().cancel(true);
        }
        for (Network network : networks) {
            for (Member member : network.getChannel().getMembers()) {
                member.mute(false).queue();
                member.getGuild().moveVoiceMember(member, getLobby()).queue();
            }
            network.getChannel().delete().queue();
            network.clear();
        }
        networks.clear();
    }
}
