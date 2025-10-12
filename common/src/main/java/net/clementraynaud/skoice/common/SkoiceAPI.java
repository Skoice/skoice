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

package net.clementraynaud.skoice.common;

import net.clementraynaud.skoice.common.api.events.player.PlayerProximityConnectEvent;
import net.clementraynaud.skoice.common.api.events.player.PlayerProximityDisconnectEvent;
import net.clementraynaud.skoice.common.api.events.system.SystemInterruptionEvent;
import net.clementraynaud.skoice.common.bot.BotStatus;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SkoiceAPI {

    private final Skoice plugin;
    private final Set<UUID> proximityConnectedPlayers = ConcurrentHashMap.newKeySet();

    SkoiceAPI(Skoice plugin) {
        this.plugin = plugin;

        Skoice.eventBus().subscribe(
                PlayerProximityDisconnectEvent.class,
                event -> {
                    this.proximityConnectedPlayers.remove(event.getMinecraftId());
                }
        );
        Skoice.eventBus().subscribe(
                PlayerProximityConnectEvent.class,
                event -> {
                    this.proximityConnectedPlayers.add(event.getMinecraftId());
                }
        );
        Skoice.eventBus().subscribe(
                SystemInterruptionEvent.class,
                event -> {
                    this.proximityConnectedPlayers.clear();
                }
        );
    }

    public Map<String, String> getLinkedAccounts() {
        return Collections.unmodifiableMap(this.plugin.getLinksYamlFile().getLinks());
    }

    @SuppressWarnings("unused")
    public boolean linkUser(UUID minecraftId, String discordId) {
        if (this.plugin.getLinksYamlFile().getLinks().containsKey(minecraftId.toString()) || this.plugin.getLinksYamlFile().getLinks().containsValue(discordId)) {
            return false;
        }
        this.plugin.getLinksYamlFile().linkUserDirectly(minecraftId.toString(), discordId);
        return true;
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public boolean isLinked(String discordId) {
        return this.getLinkedAccounts().containsValue(discordId);
    }

    public boolean isProximityConnected(UUID minecraftId) {
        return this.proximityConnectedPlayers.contains(minecraftId);
    }

    @SuppressWarnings("unused")
    public Set<UUID> getProximityConnectedPlayers() {
        return Collections.unmodifiableSet(this.proximityConnectedPlayers);
    }

    @SuppressWarnings("unused")
    public boolean isSystemReady() {
        return this.plugin.getBot().getStatus() == BotStatus.READY;
    }
}