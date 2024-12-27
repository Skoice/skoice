package net.clementraynaud.skoice.platforms.spigot.listeners;

import net.clementraynaud.skoice.platforms.spigot.minecraft.SpigotBasePlayer;
import net.clementraynaud.skoice.platforms.spigot.system.SpigotListenerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final SpigotListenerManager listenerManager;

    public PlayerQuitListener(SpigotListenerManager listenerManager) {
        this.listenerManager = listenerManager;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.listenerManager.getPlayerQuitHandler().onPlayerQuit(new SpigotBasePlayer(event.getPlayer()));
    }
}
