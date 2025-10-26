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

package net.clementraynaud.skoice.common.api.events.system;

import net.clementraynaud.skoice.common.api.events.SkoiceEvent;

/**
 * Called when the proximity voice chat system becomes ready.
 * <p>
 * This event is fired when proximity voice chat starts running. After this event,
 * {@link net.clementraynaud.skoice.common.SkoiceAPI#isSystemReady} will return true.
 *
 * <p>This event is useful for performing tasks that should only occur when proximity
 * chat is active, such as enforcing voice chat requirements.
 *
 * <p><b>Example usage:</b>
 * <pre>{@code
 * eventBus.subscribe(SystemReadyEvent.class, event -> {
 *     // Proximity chat is now running
 * });
 * }</pre>
 *
 * @see net.clementraynaud.skoice.common.SkoiceAPI#isSystemReady
 * @see SystemInterruptionEvent
 */
public class SystemReadyEvent implements SkoiceEvent {

}