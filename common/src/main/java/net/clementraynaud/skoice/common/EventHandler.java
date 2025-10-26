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

import net.clementraynaud.skoice.common.api.events.SkoiceEvent;

/**
 * Represents a subscription to a specific event type.
 * <p>
 * This class is returned when subscribing to an event via {@link EventBus#subscribe(Class, java.util.function.Consumer)}.
 * It allows you to unsubscribe from the event and check if the subscription is still active.
 *
 * <p><b>Example usage:</b>
 * <pre>{@code
 * EventHandler<PlayerProximityConnectEvent> handler = eventBus.subscribe(
 *     PlayerProximityConnectEvent.class,
 *     event -> {
 *         // Handle event
 *     }
 * );
 *
 * // Later, when you want to stop listening:
 * handler.unsubscribe();
 * }</pre>
 *
 * @param <T> the event type this handler is subscribed to
 * @see EventBus#subscribe(Class, java.util.function.Consumer)
 */
public class EventHandler<T extends SkoiceEvent> {

    private final EventBus eventBus;
    private final Class<T> eventClass;
    private final EventListener<T> listener;
    private volatile boolean active = true;

    EventHandler(EventBus eventBus, Class<T> eventClass, EventListener<T> listener) {
        this.eventBus = eventBus;
        this.eventClass = eventClass;
        this.listener = listener;
    }

    /**
     * Unsubscribes this handler from the event bus.
     * <p>
     * After calling this method, the handler will no longer receive events.
     * This method is idempotent - calling it multiple times has no additional effect.
     */
    public void unsubscribe() {
        if (this.active) {
            this.eventBus.unsubscribe(this.eventClass, this.listener);
            this.active = false;
        }
    }

    /**
     * Checks if this handler is still actively subscribed.
     *
     * @return true if this handler is active and will receive events, false if it has been unsubscribed
     */
    public boolean isActive() {
        return this.active;
    }
}