package net.clementraynaud.skoice.platforms.velocity.system;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.platforms.velocity.minecraft.VelocityBasePlayer;
import net.clementraynaud.skoice.system.ListenerManager;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

public class VelocityListenerManager extends ListenerManager {

    private final Set<Object> listeners = ConcurrentHashMap.newKeySet();

    public VelocityListenerManager(Skoice plugin) {
        super(plugin);
    }

    @Override
    public void registerPermanentMinecraftListeners() {
        this.addMinecraftListener(super.getPlayerJoinListener());
    }

    @Override
    public void registerMinecraftListeners() {
        this.addMinecraftListener(super.getPlayerQuitListener());
    }

    @Override
    public void unregisterMinecraftListeners() {
        this.removeMinecraftListener(super.getPlayerQuitListener());
    }

    public void addMinecraftListener(Object listener) {
        this.listeners.add(listener);
    }

    public void removeMinecraftListener(Object listener) {
        this.listeners.remove(listener);
    }

    public CompletionStage<Void> onPlayerQuit(VelocityBasePlayer velocityBasePlayer) {
        if (this.listeners.contains(this.getPlayerQuitListener())) {
            return this.getPlayerQuitListener().onPlayerQuit(velocityBasePlayer);
        }
        return CompletableFuture.completedFuture(null);
    }

    public void onPlayerJoin(VelocityBasePlayer velocityBasePlayer, boolean chatAlert) {
        if (this.listeners.contains(this.getPlayerJoinListener())) {
            this.getPlayerJoinListener().onPlayerJoin(velocityBasePlayer, chatAlert);
        }
    }
}

