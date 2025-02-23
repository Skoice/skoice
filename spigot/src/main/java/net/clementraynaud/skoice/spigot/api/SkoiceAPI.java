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

package net.clementraynaud.skoice.spigot.api;

import net.clementraynaud.skoice.common.bot.BotStatus;
import net.clementraynaud.skoice.spigot.SkoiceSpigot;
import net.clementraynaud.skoice.spigot.api.events.player.PlayerProximityConnectEvent;
import net.clementraynaud.skoice.spigot.api.events.player.PlayerProximityDisconnectEvent;
import net.clementraynaud.skoice.spigot.api.events.system.SystemInterruptionEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SkoiceAPI implements Listener {

    private final SkoiceSpigot plugin;
    private final Set<UUID> proximityConnectedPlayers = new HashSet<>();

    public SkoiceAPI(SkoiceSpigot plugin) {
        this.plugin = plugin;
    }

    public Map<String, String> getLinkedAccounts() {
        return Collections.unmodifiableMap(this.plugin.getLinksYamlFile().getLinks());
    }

    public boolean linkUser(UUID minecraftId, String discordId) {
        if (this.plugin.getLinksYamlFile().getLinks().containsKey(minecraftId.toString()) || this.plugin.getLinksYamlFile().getLinks().containsValue(discordId)) {
            return false;
        }
        this.plugin.getLinksYamlFile().linkUserDirectly(minecraftId.toString(), discordId);
        return true;
    }

    public boolean unlinkUser(UUID minecraftId) {
        if (this.plugin.getLinksYamlFile().getLinks().get(minecraftId.toString()) == null) {
            return false;
        }
        this.plugin.getLinksYamlFile().unlinkUserDirectly(minecraftId.toString());
        return true;
    }

    public boolean isLinked(UUID minecraftId) {
        return this.getLinkedAccounts().containsKey(minecraftId.toString());
    }

    public boolean isLinked(String discordId) {
        return this.getLinkedAccounts().containsValue(discordId);
    }

    public boolean isProximityConnected(UUID minecraftId) {
        return this.proximityConnectedPlayers.contains(minecraftId);
    }

    public Set<UUID> getProximityConnectedPlayers() {
        return Collections.unmodifiableSet(this.proximityConnectedPlayers);
    }

    public boolean isSystemReady() {
        return this.plugin.getBot().getStatus() == BotStatus.READY;
    }

    @EventHandler
    private void onPlayerLeft(PlayerQuitEvent event) {
        if (this.isLinked(event.getPlayer().getUniqueId()) && this.isProximityConnected(event.getPlayer().getUniqueId())) {
            this.plugin.getScheduler().runTask(() -> {
                PlayerProximityDisconnectEvent newEvent = new PlayerProximityDisconnectEvent(event.getPlayer().getUniqueId().toString());
                this.plugin.getPlugin().getServer().getPluginManager().callEvent(newEvent);
            });
        }
    }

    @EventHandler
    private void onPlayerProximityDisconnect(PlayerProximityDisconnectEvent event) {
        this.proximityConnectedPlayers.remove(event.getMinecraftId());
    }

    @EventHandler
    private void onPlayerProximityConnect(PlayerProximityConnectEvent event) {
        this.proximityConnectedPlayers.add(event.getMinecraftId());
    }

    @EventHandler
    private void onSystemInterruption(SystemInterruptionEvent event) {
        this.proximityConnectedPlayers.clear();
    }
}