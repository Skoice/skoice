/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025, 2026 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.velocity;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.clementraynaud.skoice.velocity.minecraft.VelocityBasePlayer;
import net.clementraynaud.skoice.velocity.network.SkoicePacketListener;
import org.slf4j.Logger;

import java.nio.file.Path;

public class SkoicePluginVelocity {

    @Inject
    private Logger logger;
    @Inject
    private ProxyServer proxy;
    @DataDirectory
    @Inject
    private Path dataDirectory;

    private SkoiceVelocity skoice;
    private SkoicePacketListener packetListener;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.skoice = new SkoiceVelocity(this);
        this.skoice.start();

        this.packetListener = new SkoicePacketListener(this.skoice, this.proxy);
        PacketEvents.getAPI().getEventManager().registerListeners(this.packetListener);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.skoice.shutdown();
    }

    @Subscribe
    public void onDisconnectEvent(DisconnectEvent event) {
        this.skoice.getListenerManager().onPlayerQuit(new VelocityBasePlayer(event.getPlayer()));
        this.skoice.removePlayerInfo(event.getPlayer().getUniqueId());
        this.packetListener.removePlayer(event.getPlayer().getUniqueId());
    }

    @Subscribe
    public void onServerPostConnect(ServerPostConnectEvent event) {
        if (event.getPreviousServer() != null) {
            this.skoice.getListenerManager().onPlayerQuit(new VelocityBasePlayer(event.getPlayer())).thenAccept(aVoid -> {
                this.skoice.removePlayerInfo(event.getPlayer().getUniqueId());
                this.skoice.getListenerManager().onPlayerJoin(new VelocityBasePlayer(event.getPlayer()), false);
            });
        } else {
            this.skoice.getListenerManager().onPlayerJoin(new VelocityBasePlayer(event.getPlayer()), true);
        }
    }

    public Path getDataDirectory() {
        return this.dataDirectory;
    }

    public Logger getLogger() {
        return this.logger;
    }

    public ProxyServer getProxy() {
        return this.proxy;
    }
}
