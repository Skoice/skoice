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

package net.clementraynaud.skoice.common.api.events.player;

import net.clementraynaud.skoice.common.api.events.SkoiceEventLink;

/**
 * Called when a player connects to proximity chat.
 * <p>
 * After this event, {@link net.clementraynaud.skoice.common.SkoiceAPI#isProximityConnected}
 * will return true for this player.
 *
 * <p><b>Example usage:</b>
 * <pre>{@code
 * eventBus.subscribe(PlayerProximityConnectEvent.class, event -> {
 *     UUID minecraftId = event.getMinecraftId();
 *     String discordId = event.getDiscordId();
 *     // Player connected to proximity chat
 * });
 * }</pre>
 *
 * @see net.clementraynaud.skoice.common.SkoiceAPI#isProximityConnected
 * @see PlayerProximityDisconnectEvent
 */
public class PlayerProximityConnectEvent extends SkoiceEventLink {

    public PlayerProximityConnectEvent(String minecraftId, String discordId) {
        super(minecraftId, discordId);
    }
}