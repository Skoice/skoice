// Copyright 2020, 2021 Clément "carlodrift" Raynaud, rowisabeast

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
// along with Foobar.  If not, see <https://www.gnu.org/licenses/>.


package net.clementraynaud;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class Skoice extends JavaPlugin {

    public FileConfiguration playerData;
    public File data;
    private Bot bot;

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
        } else if(playerData.getString("guildID").equals("")){
            getLogger().severe(ChatColor.RED + "No bot guildID, edit the data.yml to add guildID");
        } else if(playerData.getString("mainVoiceChannelID").equals("")){
            getLogger().severe(ChatColor.RED + "No bot mainVoiceChannelID, edit the data.yml to add mainVoiceChannelID");
        } else {

            // Check if newest version

            //Check/get scoreboard


            botReady = true;
            bot = new Bot(this);
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
        // Plugin shutdown logic
        if(botReady){
            getLogger().info("Moving all Discord Members to MainVoiceChannel, And Deleting All Skoice Created Voice Channels");
            VoiceChannel MainVC = bot.jda.getVoiceChannelById(playerData.getString("mainVoiceChannelID"));
            for(VoiceChannel createdVC : bot.createdVoiceChannels){
                for(Member meminvc : createdVC.getMembers()){
                    try {
                        getLogger().info("Moving "+meminvc.getNickname()+" to "+MainVC.getName());
                        bot.jda.getGuilds().get(0).moveVoiceMember(meminvc, MainVC).complete(true);
                    } catch (RateLimitedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    getLogger().info("Deleting Voice Channel '"+createdVC.getName()+"'");
                    createdVC.delete().complete(true);
                } catch (RateLimitedException e) {
                    e.printStackTrace();
                }
            }
            bot.jda.shutdownNow();
        }else{
            getLogger().severe(ChatColor.RED + "Discord Bot didn't start correctly, The error should be above. Please Contact us if you believe this is a bug");
        }

        getLogger().info("Plugin disabled!");
    }
}
