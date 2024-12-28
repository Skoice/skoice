package net.clementraynaud.skoice.spigot.system;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.spigot.SkoiceSpigot;
import net.clementraynaud.skoice.common.system.ListenerManager;
import net.clementraynaud.skoice.spigot.handlers.SpigotPlayerJoinHandler;
import net.clementraynaud.skoice.common.handlers.player.PlayerJoinHandler;
import net.clementraynaud.skoice.spigot.jda.SpigotGuildVoiceUpdateListener;
import net.clementraynaud.skoice.spigot.listeners.PlayerJoinListener;
import net.clementraynaud.skoice.spigot.listeners.PlayerQuitListener;
import net.clementraynaud.skoice.spigot.listeners.ServerCommandListener;
import net.clementraynaud.skoice.common.listeners.guild.voice.GuildVoiceUpdateListener;
import org.bukkit.event.HandlerList;

public class SpigotListenerManager extends ListenerManager {

    private final PlayerQuitListener playerQuitListener;
    private final SkoiceSpigot plugin;

    public SpigotListenerManager(SkoiceSpigot plugin) {
        super(plugin);
        this.plugin = plugin;
        this.playerQuitListener = new PlayerQuitListener(this);
    }

    @Override
    public void registerPermanentMinecraftListeners() {
        this.plugin.getPlugin().getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this.plugin.getPlugin());
        this.plugin.getPlugin().getServer().getPluginManager().registerEvents(new ServerCommandListener(this), this.plugin.getPlugin());
    }

    @Override
    public void registerMinecraftListeners() {
        this.plugin.getPlugin().getServer().getPluginManager().registerEvents(this.playerQuitListener, this.plugin.getPlugin());
    }

    @Override
    public void unregisterMinecraftListeners() {
        HandlerList.unregisterAll(this.playerQuitListener);
    }

    @Override
    protected GuildVoiceUpdateListener createGuildVoiceUpdate() {
        return new SpigotGuildVoiceUpdateListener(this.plugin);
    }

    @Override
    protected PlayerJoinHandler createPlayerJoinHandler(Skoice skoice) {
        return new SpigotPlayerJoinHandler((SkoiceSpigot) skoice);
    }

    public SkoiceSpigot getPlugin() {
        return this.plugin;
    }
}
