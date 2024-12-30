package net.clementraynaud.skoice.spigot;

import net.clementraynaud.skoice.common.model.JsonModel;
import net.clementraynaud.skoice.common.model.minecraft.PlayerInfo;
import net.clementraynaud.skoice.common.model.minecraft.ProxyInfo;
import net.clementraynaud.skoice.common.model.minecraft.SkoiceGameMode;
import net.clementraynaud.skoice.common.model.minecraft.SkoiceLocation;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.clementraynaud.skoice.spigot.api.events.system.SystemReadyEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkoicePluginSpigot extends JavaPlugin implements PluginMessageListener, Listener {

    private static final String CHANNEL = "skoice:main";
    private static boolean PROXY_MODE = false;
    private final Map<UUID, String> latestMessagesSent = new ConcurrentHashMap<>();
    private SkoiceSpigot skoice;

    public static boolean isProxyMode() {
        return SkoicePluginSpigot.PROXY_MODE;
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getMessenger().registerIncomingPluginChannel(this, SkoicePluginSpigot.CHANNEL, this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, SkoicePluginSpigot.CHANNEL);

        this.skoice = new SkoiceSpigot(this);
        this.skoice.start();
    }

    public File getFile() {
        return super.getFile();
    }

    @Override
    public void onDisable() {
        this.skoice.shutdown();
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!SkoicePluginSpigot.CHANNEL.equals(channel) || SkoicePluginSpigot.PROXY_MODE) {
            return;
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(message);
        DataInputStream in = new DataInputStream(bais);
        String json;
        try {
            json = in.readUTF();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (JsonModel.fromJson(json, ProxyInfo.class) != null) {
            this.enableProxyMode();
        }
    }

    @EventHandler
    public void onSystemReady(SystemReadyEvent event) {
        if (SkoicePluginSpigot.PROXY_MODE) {
            this.disableStandaloneSkoice();
        }
    }

    private void enableProxyMode() {
        if (SkoicePluginSpigot.PROXY_MODE) {
            return;
        }
        SkoicePluginSpigot.PROXY_MODE = true;
        this.runProxyTask();
        this.disableStandaloneSkoice();
        System.out.println("Proxy mode enabled");
    }

    private void disableStandaloneSkoice() {
        this.skoice.getConfigYamlFile().remove(ConfigField.TOKEN.toString());
        this.skoice.shutdown();
    }

    private void runProxyTask() {
        this.getServer().getScheduler().runTaskTimer(this, () -> {
            this.getServer().getOnlinePlayers().parallelStream().forEach(player -> {
                Scoreboard scoreboard = player.getScoreboard();
                Team playerTeam = scoreboard.getEntryTeam(player.getName());
                PlayerInfo info = new PlayerInfo(player.getUniqueId(),
                        player.isDead(),
                        SkoiceGameMode.valueOf(player.getGameMode().toString()),
                        player.getWorld().getName(),
                        new SkoiceLocation(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()),
                        playerTeam == null ? null : playerTeam.getName(),
                        this.getDescription().getVersion());
                String json = JsonModel.toJson(info);

                if (!json.equals(this.latestMessagesSent.get(player.getUniqueId()))) {
                    this.latestMessagesSent.put(player.getUniqueId(), json);
                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(b);
                    try {
                        out.writeUTF(json);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    player.sendPluginMessage(this, SkoicePluginSpigot.CHANNEL, b.toByteArray());
                }
            });
        }, 0L, 20L);
    }
}
