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
        this.addMinecraftListener(super.getPlayerJoinHandler());
    }

    @Override
    public void registerMinecraftListeners() {
        this.addMinecraftListener(super.getPlayerQuitHandler());
    }

    @Override
    public void unregisterMinecraftListeners() {
        this.removeMinecraftListener(super.getPlayerQuitHandler());
    }

    public void addMinecraftListener(Object listener) {
        this.listeners.add(listener);
    }

    public void removeMinecraftListener(Object listener) {
        this.listeners.remove(listener);
    }

    public CompletionStage<Void> onPlayerQuit(VelocityBasePlayer velocityBasePlayer) {
        if (this.listeners.contains(this.getPlayerQuitHandler())) {
            return this.getPlayerQuitHandler().onPlayerQuit(velocityBasePlayer);
        }
        return CompletableFuture.completedFuture(null);
    }

    public void onPlayerJoin(VelocityBasePlayer velocityBasePlayer, boolean chatAlert) {
        if (this.listeners.contains(this.getPlayerJoinHandler())) {
            this.getPlayerJoinHandler().onPlayerJoin(velocityBasePlayer, chatAlert);
        }
    }
}

