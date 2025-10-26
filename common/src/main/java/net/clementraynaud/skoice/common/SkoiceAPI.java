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

/**
 * The main Skoice API interface.
 * <p>
 * This class provides methods for interacting with Skoice's core functionality,
 * including account linking, proximity chat status, and system state queries.
 *
 * <p>Access the API instance through {@link Skoice#api()}.
 *
 * <p><b>Example usage:</b>
 * <pre>{@code
 * SkoiceAPI api = Skoice.api();
 * if (api != null && api.isSystemReady()) {
 *     UUID playerUuid = player.getUniqueId();
 *
 *     // Check if player is connected to proximity chat
 *     if (api.isProximityConnected(playerUuid)) {
 *         // Player is in proximity chat
 *     }
 *
 *     // Check if player has linked their Discord account
 *     if (api.isLinked(playerUuid)) {
 *         // Player has linked their account
 *     }
 * }
 * }</pre>
 *
 * @see Skoice#api()
 */
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

    /**
     * Gets all linked Minecraft-Discord account pairs.
     * <p>
     * The returned map is immutable and maps Minecraft UUIDs (as strings) to Discord user IDs.
     *
     * @return an immutable map of Minecraft UUID strings to Discord IDs
     */
    public Map<String, String> getLinkedAccounts() {
        return Collections.unmodifiableMap(this.plugin.getLinksYamlFile().getLinks());
    }

    /**
     * Links a Minecraft account to a Discord account.
     * <p>
     * This method will fail if either the Minecraft UUID is already linked to another
     * Discord account, or if the Discord ID is already linked to another Minecraft account.
     *
     * <p>When successful, this will not trigger an {@link net.clementraynaud.skoice.common.api.events.account.AccountLinkEvent}.
     *
     * @param minecraftId the Minecraft player's UUID
     * @param discordId   the Discord user's ID
     * @return true if the link was created successfully, false if either account is already linked
     */
    @SuppressWarnings("unused")
    public boolean linkUser(UUID minecraftId, String discordId) {
        if (this.plugin.getLinksYamlFile().getLinks().containsKey(minecraftId.toString()) || this.plugin.getLinksYamlFile().getLinks().containsValue(discordId)) {
            return false;
        }
        this.plugin.getLinksYamlFile().linkUserDirectly(minecraftId.toString(), discordId);
        return true;
    }

    /**
     * Unlinks a Minecraft account from its associated Discord account.
     * <p>
     * This method will fail if the Minecraft UUID is not currently linked.
     *
     * <p>When successful, this will not trigger an {@link net.clementraynaud.skoice.common.api.events.account.AccountUnlinkEvent}.
     *
     * @param minecraftId the Minecraft player's UUID
     * @return true if the account was unlinked successfully, false if the account was not linked
     */
    @SuppressWarnings("unused")
    public boolean unlinkUser(UUID minecraftId) {
        if (this.plugin.getLinksYamlFile().getLinks().get(minecraftId.toString()) == null) {
            return false;
        }
        this.plugin.getLinksYamlFile().unlinkUserDirectly(minecraftId.toString());
        return true;
    }

    /**
     * Checks if a Minecraft player has linked their Discord account.
     *
     * @param minecraftId the Minecraft player's UUID
     * @return true if the player has a linked Discord account, false otherwise
     */
    public boolean isLinked(UUID minecraftId) {
        return this.getLinkedAccounts().containsKey(minecraftId.toString());
    }

    /**
     * Checks if a Discord user has linked their Minecraft account.
     *
     * @param discordId the Discord user's ID
     * @return true if the Discord user has a linked Minecraft account, false otherwise
     */
    @SuppressWarnings("unused")
    public boolean isLinked(String discordId) {
        return this.getLinkedAccounts().containsValue(discordId);
    }

    /**
     * Checks if a player is currently connected to proximity voice chat.
     * <p>
     * A player is considered proximity connected when they are linked and connected
     * to either the main voice channel or a proximity channel.
     *
     * @param minecraftId the Minecraft player's UUID
     * @return true if the player is connected to proximity chat, false otherwise
     */
    public boolean isProximityConnected(UUID minecraftId) {
        return this.proximityConnectedPlayers.contains(minecraftId);
    }

    /**
     * Gets all players currently connected to proximity voice chat.
     * <p>
     * The returned set is immutable and contains the UUIDs of all players connected
     * to proximity chat.
     *
     * @return an immutable set of player UUIDs connected to proximity chat
     */
    @SuppressWarnings("unused")
    public Set<UUID> getProximityConnectedPlayers() {
        return Collections.unmodifiableSet(this.proximityConnectedPlayers);
    }

    /**
     * Checks if the proximity voice chat system is actively running.
     * <p>
     * The system is ready when the Discord bot is connected and proximity voice chat
     * is operational. This is useful for features that should only activate when
     * proximity chat is available, such as enforcing voice chat requirements.
     *
     * <p>Listen to {@link net.clementraynaud.skoice.common.api.events.system.SystemReadyEvent}
     * and {@link net.clementraynaud.skoice.common.api.events.system.SystemInterruptionEvent}
     * to be notified when the system state changes.
     *
     * @return true if proximity voice chat is actively running, false otherwise
     */
    @SuppressWarnings("unused")
    public boolean isSystemReady() {
        return this.plugin.getBot().getStatus() == BotStatus.READY;
    }
}