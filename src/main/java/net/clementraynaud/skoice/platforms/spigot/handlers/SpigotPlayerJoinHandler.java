package net.clementraynaud.skoice.platforms.spigot.handlers;

import net.clementraynaud.skoice.handlers.player.PlayerJoinHandler;
import net.clementraynaud.skoice.platforms.spigot.SkoiceSpigot;
import net.clementraynaud.skoice.platforms.spigot.api.events.player.PlayerProximityConnectEvent;

public class SpigotPlayerJoinHandler extends PlayerJoinHandler {

    private final SkoiceSpigot plugin;

    public SpigotPlayerJoinHandler(SkoiceSpigot plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected void callPlayerProximityConnectEvent(String minecraftId, String memberId) {
        this.plugin.getPlugin().getServer().getScheduler().runTask(this.plugin.getPlugin(), () -> {
            PlayerProximityConnectEvent event = new PlayerProximityConnectEvent(minecraftId, memberId);
            this.plugin.getPlugin().getServer().getPluginManager().callEvent(event);
        });
    }
}
