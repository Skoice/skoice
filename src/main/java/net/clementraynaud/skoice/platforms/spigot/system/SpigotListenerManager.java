package net.clementraynaud.skoice.platforms.spigot.system;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.handlers.player.PlayerJoinHandler;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceUpdateListener;
import net.clementraynaud.skoice.platforms.spigot.SkoiceSpigot;
import net.clementraynaud.skoice.platforms.spigot.handlers.SpigotPlayerJoinHandler;
import net.clementraynaud.skoice.platforms.spigot.jda.SpigotGuildVoiceUpdateListener;
import net.clementraynaud.skoice.platforms.spigot.listeners.PlayerJoinListener;
import net.clementraynaud.skoice.platforms.spigot.listeners.PlayerQuitListener;
import net.clementraynaud.skoice.platforms.spigot.listeners.ServerCommandListener;
import net.clementraynaud.skoice.system.ListenerManager;
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
