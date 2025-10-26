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

package net.clementraynaud.skoice.common.api;

import net.clementraynaud.skoice.common.api.events.SkoiceEvent;

/**
 * A functional interface for handling Skoice events.
 * <p>
 * This interface can be used as an alternative to {@link java.util.function.Consumer}
 * when subscribing to events via {@link net.clementraynaud.skoice.common.EventBus#subscribeCallback(Class, EventCallback)}.
 *
 * <p><b>Example usage:</b>
 * <pre>{@code
 * EventCallback<PlayerProximityConnectEvent> callback = event -> {
 *     UUID playerId = event.getMinecraftId();
 *     String discordId = event.getDiscordId();
 *     // Handle the event
 * };
 *
 * eventBus.subscribeCallback(PlayerProximityConnectEvent.class, callback);
 * }</pre>
 *
 * @param <T> the type of event this callback handles
 * @see net.clementraynaud.skoice.common.EventBus#subscribeCallback(Class, EventCallback)
 */
@FunctionalInterface
public interface EventCallback<T extends SkoiceEvent> {

    /**
     * Handles the given event.
     *
     * @param event the event to handle
     */
    void handle(T event);
}