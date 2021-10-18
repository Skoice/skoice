// Copyright 2020, 2021 Clément "carlodrift" Raynaud, rowisabeast
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
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.*;

public class Skoice extends JavaPlugin {

    public FileConfiguration playerData;
    public File data;

    public VoiceModule getVoiceModule() {
        return bot;
    }

    private VoiceModule bot;

    boolean botReady = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        new Metrics(this, 11380);
        getConfig().options().copyDefaults();
        createConfig();
        getLogger().info("Plugin enabled!");
        if(playerData.getString("token").equals("")){
            getLogger().severe(ChatColor.RED + "No bot token, edit the data.yml to add token");
        } else if(playerData.getString("mainVoiceChannelID").equals("")){
            getLogger().severe(ChatColor.RED + "No bot mainVoiceChannelID, edit the data.yml to add mainVoiceChannelID");
        } else {

            // Check if newest version

            //Check/get scoreboard


            botReady = true;
//            bot = new Bot(this);
            bot = new VoiceModule(this);
            // getServer().getPluginManager().registerEvents();
        }
        if(!botReady){
            onDisable();
        }
    }

    private void createConfig(){
        data = new File(getDataFolder() + File.separator + "data.yml");
        if(!data.exists()){
            getLogger().info(ChatColor.LIGHT_PURPLE + "Creating file data.yml");
            this.saveResource("data.yml", false);
        }
        playerData = new YamlConfiguration();
        try {
            playerData.load(data);
        } catch (IOException | InvalidConfigurationException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("Skoice - Shutdown").build();
        final ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);
        try {
            executor.invokeAll(Collections.singletonList(() -> {
                bot.shutdown();
                if (bot.jda != null) bot.jda.getEventManager().getRegisteredListeners().forEach(listener -> bot.jda.getEventManager().unregister(listener));
                if (bot.jda != null) {
                    CompletableFuture<Void> shutdownTask = new CompletableFuture<>();
                    bot.jda.addEventListener(new ListenerAdapter() {
                        @Override
                        public void onShutdown(@NotNull ShutdownEvent event) {
                            shutdownTask.complete(null);
                        }
                    });
                    bot.jda.shutdownNow();
                    bot.jda = null;
                    try {
                        shutdownTask.get(5, TimeUnit.SECONDS);
                    } catch (TimeoutException e) {
                        getLogger().warning("JDA took too long to shut down, skipping");
                    }
                }

                return null;
            }), 15, TimeUnit.SECONDS);
        }catch(InterruptedException e){}
        executor.shutdownNow();
    }
        // Plugin shutdown logic
//        if(botReady){
//            getLogger().info("Moving all Discord Members to MainVoiceChannel, And Deleting All Skoice Created Voice Channels");
//            VoiceChannel MainVC = bot.jda.getVoiceChannelById(playerData.getString("mainVoiceChannelID"));
//            for(VoiceChannel createdVC : bot.createdVoiceChannels){
//                for(Member meminvc : createdVC.getMembers()){
//                    try {
//                        getLogger().info("Moving "+meminvc.getNickname()+" to "+MainVC.getName());
//                        bot.jda.getGuilds().get(0).moveVoiceMember(meminvc, MainVC).complete(true);
//                    } catch (RateLimitedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                try {
//                    getLogger().info("Deleting Voice Channel '"+createdVC.getName()+"'");
//                    createdVC.delete().complete(true);
//                } catch (RateLimitedException e) {
//                    e.printStackTrace();
//                }
//            }
//            bot.jda.shutdownNow();
//        }else{
//            getLogger().severe(ChatColor.RED + "Discord Bot didn't start correctly, The error should be above. Please Contact us if you believe this is a bug");
//        }

//        getLogger().info("Plugin disabled!");
    }
