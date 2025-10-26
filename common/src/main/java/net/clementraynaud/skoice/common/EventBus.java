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

import net.clementraynaud.skoice.common.api.EventCallback;
import net.clementraynaud.skoice.common.api.events.SkoiceEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * The event bus for subscribing to and handling Skoice events.
 * <p>
 * The event bus provides a simple publish-subscribe pattern for listening to various
 * Skoice events. All event handlers are executed asynchronously on a dedicated event thread.
 *
 * <p>Access the event bus instance through {@link Skoice#eventBus()}.
 *
 * <p><b>Example usage:</b>
 * <pre>{@code
 * EventBus eventBus = Skoice.eventBus();
 *
 * // Subscribe to player connecting to proximity chat
 * EventHandler<PlayerProximityConnectEvent> handler = eventBus.subscribe(
 *     PlayerProximityConnectEvent.class,
 *     event -> {
 *         UUID playerId = event.getMinecraftId();
 *         String discordId = event.getDiscordId();
 *         // Handle the event
 *     }
 * );
 *
 * // Later, unsubscribe if needed
 * handler.unsubscribe();
 * }</pre>
 *
 * @see Skoice#eventBus()
 * @see EventHandler
 */
public class EventBus {

    private final Map<Class<? extends SkoiceEvent>, Set<EventListener<?>>> listeners = new ConcurrentHashMap<>();
    private final ExecutorService executorService;

    EventBus() {
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Skoice-Events");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Subscribes to a specific event type with a handler function.
     * <p>
     * The handler will be invoked asynchronously whenever an event of the specified type is fired.
     * Multiple handlers can be subscribed to the same event type.
     *
     * <p><b>Example:</b>
     * <pre>{@code
     * eventBus.subscribe(PlayerProximityConnectEvent.class, event -> {
     *     System.out.println("Player " + event.getMinecraftId() + " connected!");
     * });
     * }</pre>
     *
     * @param eventClass the class of the event to subscribe to
     * @param handler    the consumer that will handle the event
     * @param <T>        the event type
     * @return an {@link EventHandler} that can be used to unsubscribe later
     */
    public <T extends SkoiceEvent> EventHandler<T> subscribe(Class<T> eventClass, Consumer<T> handler) {
        EventListener<T> listener = new EventListener<>(handler);

        this.listeners.computeIfAbsent(eventClass, k -> ConcurrentHashMap.newKeySet()).add(listener);

        return new EventHandler<>(this, eventClass, listener);
    }

    /**
     * Subscribes to a specific event type with an {@link EventCallback}.
     * <p>
     * This is a convenience method for subscribing with an {@link EventCallback} instead of a
     * {@link Consumer}. Functionally equivalent to {@link #subscribe(Class, Consumer)}.
     *
     * @param eventClass the class of the event to subscribe to
     * @param callback   the callback that will handle the event
     * @param <T>        the event type
     * @return an {@link EventHandler} that can be used to unsubscribe later
     * @see #subscribe(Class, Consumer)
     */
    @SuppressWarnings("unused")
    public <T extends SkoiceEvent> EventHandler<T> subscribeCallback(Class<T> eventClass, EventCallback<T> callback) {
        return this.subscribe(eventClass, callback::handle);
    }

    /**
     * Fires an event asynchronously to all registered listeners.
     * <p>
     * This method is primarily for internal use by Skoice. All registered handlers
     * for the event type will be invoked asynchronously on the event thread.
     *
     * <p>If any handler throws an exception, it will be caught and printed, but will
     * not prevent other handlers from executing.
     *
     * @param event the event to fire
     * @param <T>   the event type
     */
    public <T extends SkoiceEvent> void fireAsync(T event) {
        Class<?> eventClass = event.getClass();
        Set<EventListener<?>> eventListeners = this.listeners.get(eventClass);

        if (eventListeners == null || eventListeners.isEmpty()) {
            return;
        }

        this.executorService.execute(() -> {
            for (EventListener<?> listener : eventListeners) {
                try {
                    @SuppressWarnings("unchecked")
                    EventListener<T> typedListener = (EventListener<T>) listener;
                    typedListener.handle(event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    <T extends SkoiceEvent> void unsubscribe(Class<T> eventClass, EventListener<T> listener) {
        Set<EventListener<?>> eventListeners = this.listeners.get(eventClass);
        if (eventListeners != null) {
            eventListeners.remove(listener);

            if (eventListeners.isEmpty()) {
                this.listeners.remove(eventClass);
            }
        }
    }

    /**
     * Checks if any listeners are subscribed to a specific event type.
     *
     * @param eventClass the event class to check
     * @param <T>        the event type
     * @return true if at least one listener is subscribed to this event type, false otherwise
     */
    @SuppressWarnings("unused")
    public <T extends SkoiceEvent> boolean hasListeners(Class<T> eventClass) {
        Set<EventListener<?>> eventListeners = this.listeners.get(eventClass);
        return eventListeners != null && !eventListeners.isEmpty();
    }

    void shutdown() {
        this.listeners.clear();
        this.executorService.shutdown();
        try {
            if (!this.executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                this.executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            this.executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
