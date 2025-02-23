/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
 *
 * This file is part of Skoice.
 *
 * Skoice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skoice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skoice.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.clementraynaud.skoice.velocity.system;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.system.ListenerManager;
import net.clementraynaud.skoice.velocity.minecraft.VelocityBasePlayer;

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

