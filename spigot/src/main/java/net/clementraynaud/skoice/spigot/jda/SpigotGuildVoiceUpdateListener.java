package net.clementraynaud.skoice.spigot.jda;

import net.clementraynaud.skoice.common.listeners.guild.voice.GuildVoiceUpdateListener;
import net.clementraynaud.skoice.spigot.SkoiceSpigot;
import net.clementraynaud.skoice.spigot.api.events.player.PlayerProximityDisconnectEvent;

public class SpigotGuildVoiceUpdateListener extends GuildVoiceUpdateListener {

    private final SkoiceSpigot plugin;

    public SpigotGuildVoiceUpdateListener(SkoiceSpigot plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected void callPlayerProximityDisconnectEvent(String minecraftId) {
        this.plugin.getPlugin().getServer().getScheduler().runTask(this.plugin.getPlugin(), () -> {
            PlayerProximityDisconnectEvent event = new PlayerProximityDisconnectEvent(minecraftId);
            this.plugin.getPlugin().getServer().getPluginManager().callEvent(event);
        });
    }
}
