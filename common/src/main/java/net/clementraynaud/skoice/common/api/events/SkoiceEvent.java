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

package net.clementraynaud.skoice.common.api.events;

/**
 * Base marker interface for all Skoice events.
 * <p>
 * All events fired by Skoice implement this interface. You can subscribe to
 * any event type through the {@link net.clementraynaud.skoice.common.EventBus}.
 *
 * <h2>Event Types</h2>
 * <p>Skoice provides several categories of events:
 * <ul>
 *   <li><b>Player Events:</b> Events related to player proximity chat connections
 *       <ul>
 *         <li>{@link net.clementraynaud.skoice.common.api.events.player.PlayerProximityConnectEvent}</li>
 *         <li>{@link net.clementraynaud.skoice.common.api.events.player.PlayerProximityDisconnectEvent}</li>
 *       </ul>
 *   </li>
 *   <li><b>Account Events:</b> Events related to Discord-Minecraft account linking
 *       <ul>
 *         <li>{@link net.clementraynaud.skoice.common.api.events.account.AccountLinkEvent}</li>
 *         <li>{@link net.clementraynaud.skoice.common.api.events.account.AccountUnlinkEvent}</li>
 *       </ul>
 *   </li>
 *   <li><b>System Events:</b> Events related to Skoice system state
 *       <ul>
 *         <li>{@link net.clementraynaud.skoice.common.api.events.system.SystemReadyEvent}</li>
 *         <li>{@link net.clementraynaud.skoice.common.api.events.system.SystemInterruptionEvent}</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * @see net.clementraynaud.skoice.common.EventBus
 */
public interface SkoiceEvent {

}
