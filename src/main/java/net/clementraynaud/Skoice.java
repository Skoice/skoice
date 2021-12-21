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
import net.clementraynaud.main.Main;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class Skoice extends JavaPlugin {

    public FileConfiguration playerData;
    public File data;

    public Main getVoiceModule() {
        return main;
    }

    private Main main;

    boolean botReady = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        new Metrics(this, 11380);
        getConfig().options().copyDefaults();
        createConfig();
        getLogger().info("Plugin enabled!");
        if (playerData.getString("token").equals("") || playerData.getString("token") == null) {
            getLogger().severe(ChatColor.RED + "No bot token, edit the data.yml to add token");
        } else if (playerData.getString("mainVoiceChannelID").equals("") || playerData.getString("mainVoiceChannelID") == null) {
            getLogger().severe(ChatColor.RED + "No bot mainVoiceChannelID, edit the data.yml to add mainVoiceChannelID");
        } else if (playerData.getString("categoryID").equals("") || playerData.getString("categoryID") == null) {
            getLogger().severe(ChatColor.RED + "No bot categoryID, edit the data.yml to add categoryID");
        } else if (playerData.getString("distance") == null) {
            getLogger().severe(ChatColor.RED + "No distance config found, make sure you add the 'distance' from data https://github.com/carlodrift/skoice/blob/dev/src/main/resources/data.yml");
        } else if (playerData.getString("checkVersion") == null) {
            getLogger().severe(ChatColor.RED + "No checkVersion config found, make sure you add the 'checkVersion' from data https://github.com/carlodrift/skoice/blob/dev/src/main/resources/data.yml");
        } else {
            //Check Version
            if (playerData.getBoolean("checkVersion.atStartup")) {
                getLogger().info(ChatColor.YELLOW + "Checking Version Now!");
                checkVersion();
            }
            botReady = true;
            main = new Main(this);
        }
        if (!botReady) {
            onDisable();
        }
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
        try {
            Skoice nsk = this;
            String skoiceFileVersion = "v" + nsk.getDescription().getVersion();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL("https://raw.githubusercontent.com/carlodrift/skoice/main/version").openStream()));
            String spigotVersion = bufferedReader.readLine();
            if (!skoiceFileVersion.equals(spigotVersion)) {
                getLogger().warning((Object) ChatColor.RED + "You are using an outdated version!");
                getLogger().warning("Latest version: " + (Object) ChatColor.GREEN + spigotVersion + (Object) ChatColor.YELLOW + ". You are on version: " + ChatColor.RED + skoiceFileVersion + (Object) ChatColor.YELLOW + ".");
                getLogger().warning("Update here: " + (Object) ChatColor.AQUA + "https://www.spigotmc.org/resources/skoice-proximity-voice-chat.82861/");
            }
            //getLogger().info(ChatColor.GREEN+"Looks like your using the latest version!");

        } catch (IOException e) {
            //getLogger().severe("Unable to check for updates. Error: " + e.getMessage());
//                checkVersion();
        }

    }

    // Will add autoUpdater

    public static int isUpdateAvailable(String string, String string2) {
        int[] arrn;
        if (string == null || string2 == null) {
            return 0;
        }
        int[] arrn2 = Arrays.stream(string.replaceAll("[^0-9.]", "").split(Pattern.quote("."))).mapToInt(Integer::parseInt).toArray();
        if (arrn2.length > (arrn = Arrays.stream(string2.replaceAll("[^0-9.]", "").split(Pattern.quote("."))).mapToInt(Integer::parseInt).toArray()).length) {
            arrn = Arrays.copyOf(arrn, arrn2.length);
        } else if (arrn.length > arrn2.length) {
            arrn2 = Arrays.copyOf(arrn2, arrn.length);
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
        return n;
    }


    @Override
    public void onDisable() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("Skoice - Shutdown").build();
        final ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);
        try {
            executor.invokeAll(Collections.singletonList(() -> {
                main.shutdown();
                if (main.jda != null)
                    main.jda.getEventManager().getRegisteredListeners().forEach(listener -> main.jda.getEventManager().unregister(listener));
                if (main.jda != null) {
                    CompletableFuture<Void> shutdownTask = new CompletableFuture<>();
                    main.jda.addEventListener(new ListenerAdapter() {
                        @Override
                        public void onShutdown(@NotNull ShutdownEvent event) {
                            shutdownTask.complete(null);
                        }
                    });
                    main.jda.shutdownNow();
                    main.jda = null;
                    try {
                        shutdownTask.get(5, TimeUnit.SECONDS);
                    } catch (TimeoutException e) {
                        getLogger().warning("JDA took too long to shut down, skipping");
                    }
                }

                return null;
            }), 15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
        executor.shutdownNow();
        getLogger().info("Plugin disabled!");
    }

}
