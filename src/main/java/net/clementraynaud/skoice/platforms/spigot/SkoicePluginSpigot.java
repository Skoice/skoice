package net.clementraynaud.skoice.platforms.spigot;

import net.clementraynaud.skoice.model.JsonModel;
import net.clementraynaud.skoice.model.minecraft.PlayerInfo;
import net.clementraynaud.skoice.model.minecraft.SkoiceGameMode;
import net.clementraynaud.skoice.model.minecraft.SkoiceLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkoicePluginSpigot extends JavaPlugin implements PluginMessageListener, Listener {

    private static final String CHANNEL = "skoice:main";
    private final Map<UUID, String> latestMessagesSent = new ConcurrentHashMap<>();
    private SkoiceSpigot skoice;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getMessenger().registerIncomingPluginChannel(this, SkoicePluginSpigot.CHANNEL, this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, SkoicePluginSpigot.CHANNEL);

        this.skoice = new SkoiceSpigot(this);
        this.skoice.onEnable();

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

    @Override
    public void onDisable() {
        this.skoice.onDisable();
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
    }
}
