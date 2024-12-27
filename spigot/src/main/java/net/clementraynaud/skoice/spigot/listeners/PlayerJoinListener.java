package net.clementraynaud.skoice.spigot.listeners;

import net.clementraynaud.skoice.spigot.minecraft.SpigotBasePlayer;
import net.clementraynaud.skoice.spigot.system.SpigotListenerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final SpigotListenerManager listenerManager;

    public PlayerJoinListener(SpigotListenerManager listenerManager) {
        this.listenerManager = listenerManager;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.listenerManager.getPlayerJoinHandler().onPlayerJoin(new SpigotBasePlayer(event.getPlayer()), true);
    }
}
