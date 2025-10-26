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

/**
 * Core event types and base classes for the Skoice event system.
 * <p>
 * This package contains the base interfaces and abstract classes that all Skoice
 * events extend from. Concrete event implementations are organized into subpackages
 * by category.
 *
 * <h2>Event Categories</h2>
 * <ul>
 *   <li>{@link net.clementraynaud.skoice.common.api.events.player} - Player proximity chat events</li>
 *   <li>{@link net.clementraynaud.skoice.common.api.events.account} - Account linking events</li>
 *   <li>{@link net.clementraynaud.skoice.common.api.events.system} - System state events</li>
 * </ul>
 *
 * @see net.clementraynaud.skoice.common.EventBus
 * @see net.clementraynaud.skoice.common.api.events.SkoiceEvent
 */
package net.clementraynaud.skoice.common.api.events;
