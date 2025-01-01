package net.clementraynaud.skoice.spigot.handlers;

import net.clementraynaud.skoice.common.handlers.player.PlayerJoinHandler;
import net.clementraynaud.skoice.spigot.SkoiceSpigot;
import net.clementraynaud.skoice.spigot.api.events.player.PlayerProximityConnectEvent;

public class SpigotPlayerJoinHandler extends PlayerJoinHandler {

    private final SkoiceSpigot plugin;

    public SpigotPlayerJoinHandler(SkoiceSpigot plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected void callPlayerProximityConnectEvent(String minecraftId, String memberId) {
        this.plugin.getScheduler().runTask(() -> {
            PlayerProximityConnectEvent event = new PlayerProximityConnectEvent(minecraftId, memberId);
            this.plugin.getPlugin().getServer().getPluginManager().callEvent(event);
        });
    }
}
