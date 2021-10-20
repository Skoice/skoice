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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.regex.Pattern;

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
        } else if(playerData.getString("distance").equals("")) {
            getLogger().severe(ChatColor.RED + "No distance config found, make sure you add the 'distance' from data https://github.com/carlodrift/skoice/blob/dev/src/main/resources/data.yml");
        }else {

            // Check if newest version

            //Check/get scoreboard

            //Check Version
            checkVersion();

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

    public void checkVersion(){
        try{
            Skoice nsk = this;
            String skoiceFileVersion = nsk.getDescription().getVersion();

            HttpsURLConnection httpsURLConnection = (HttpsURLConnection)new URL("https://plimbocraft.com/skoice-two/php/newestVersion.php").openConnection();
            httpsURLConnection.setRequestMethod("GET");

            JsonObject jsonObject = new JsonParser().parse(new JsonReader((Reader)new InputStreamReader(httpsURLConnection.getInputStream()))).getAsJsonObject();
            String spigotVersion =  jsonObject.get("current_version").getAsString();

            int nw = isUpdateAvailable(spigotVersion, skoiceFileVersion);
            if(nw>0){
                getLogger().warning((Object)ChatColor.RED + "You are using an outdated version!");
                getLogger().warning("Latest version: " + (Object)ChatColor.GREEN + spigotVersion + (Object)ChatColor.YELLOW + ". You are on version: " + (Object)ChatColor.RED + skoiceFileVersion + (Object)ChatColor.YELLOW + ".");
                getLogger().warning("Update here: " + (Object)ChatColor.AQUA + "http://home.plimbocraft.com/skoice-two/php/latest.php");
            }else if (nw < 0){
                getLogger().warning((Object)ChatColor.RED + "You are using an unreleased version!");
                getLogger().warning("Latest version: " + (Object)ChatColor.GREEN + spigotVersion + (Object)ChatColor.YELLOW + ". You are on version: " + (Object)ChatColor.RED + skoiceFileVersion + (Object)ChatColor.YELLOW + ".");
            }
        }catch (IOException e){
            //getLogger().severe("Unable to check for updates. Error: " + e.getMessage());
            try{
                Skoice nsk = this;
                String skoiceFileVersion = nsk.getDescription().getVersion();

                HttpURLConnection httpURLConnection = (HttpURLConnection)new URL("http://home.plimbocraft.com/skoice-two/php/newestVersion.php").openConnection();
                httpURLConnection.setRequestMethod("GET");

                JsonObject jsonObject = new JsonParser().parse(new JsonReader((Reader)new InputStreamReader(httpURLConnection.getInputStream()))).getAsJsonObject();
                String spigotVersion =  jsonObject.get("current_version").getAsString();

                int nw = isUpdateAvailable(spigotVersion, skoiceFileVersion);
                if(nw>0){
                    getLogger().warning((Object)ChatColor.RED + "You are using an outdated version!");
                    getLogger().warning("Latest version: " + (Object)ChatColor.GREEN + spigotVersion + (Object)ChatColor.YELLOW + ". You are on version: " + (Object)ChatColor.RED + skoiceFileVersion + (Object)ChatColor.YELLOW + ".");
                    getLogger().warning("Update here: " + (Object)ChatColor.AQUA + "http://home.plimbocraft.com/skoice-two/php/latest.php");
                }else if (nw < 0){
                    getLogger().warning((Object)ChatColor.RED + "You are using an unreleased version!");
                    getLogger().warning("Latest version: " + (Object)ChatColor.GREEN + spigotVersion + (Object)ChatColor.YELLOW + ". You are on version: " + (Object)ChatColor.RED + skoiceFileVersion + (Object)ChatColor.YELLOW + ".");
                }
            }catch (IOException ioException){
                getLogger().severe("Unable to check for updates. Error: " + ioException.getMessage());
            }
        }
    }

    // Will add autoUpdater

    public int isUpdateAvailable(String string, String string2) {
        int[] arrn;
        if (string == null || string2 == null) {
            return 0;
        }
        int[] arrn2 = Arrays.stream(string.replaceAll("[^0-9.]", "").split(Pattern.quote("."))).mapToInt(Integer::parseInt).toArray();
        if (arrn2.length > (arrn = Arrays.stream(string2.replaceAll("[^0-9.]", "").split(Pattern.quote("."))).mapToInt(Integer::parseInt).toArray()).length) {
            arrn = Arrays.copyOf(arrn, arrn2.length);
            getLogger().info(arrn.toString());
        } else if (arrn.length > arrn2.length) {
            arrn2 = Arrays.copyOf(arrn2, arrn.length);
            getLogger().info(arrn2.toString());
        }
        int n = 0;
        for (int i = 0; i < arrn2.length; ++i) {
            if (arrn2[i] > arrn[i]) {
                n = 1;
                break;
            }
            if (arrn[i] <= arrn2[i]) continue;
            n = -1;
            break;
        }
        getLogger().info("HI: "+n);
        return n;
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
        getLogger().info("Plugin disabled!");
    }

}
