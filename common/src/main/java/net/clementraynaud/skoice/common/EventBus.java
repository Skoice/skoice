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

    public <T extends SkoiceEvent> EventHandler<T> subscribe(Class<T> eventClass, Consumer<T> handler) {
        EventListener<T> listener = new EventListener<>(handler);

        this.listeners.computeIfAbsent(eventClass, k -> ConcurrentHashMap.newKeySet()).add(listener);

        return new EventHandler<>(this, eventClass, listener);
    }

    @SuppressWarnings("unused")
    public <T extends SkoiceEvent> EventHandler<T> subscribeCallback(Class<T> eventClass, EventCallback<T> callback) {
        return this.subscribe(eventClass, callback::handle);
    }

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

    @SuppressWarnings("unused")
    public <T extends SkoiceEvent> boolean hasListeners(Class<T> eventClass) {
        Set<EventListener<?>> eventListeners = this.listeners.get(eventClass);
        return eventListeners != null && !eventListeners.isEmpty();
    }

    public void shutdown() {
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
