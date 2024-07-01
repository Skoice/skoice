/*
 * Copyright 2020, 2021, 2022, 2023, 2024 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SkoiceEvent extends Event {

    protected static final HandlerList HANDLERS = new HandlerList();

    private final String minecraftId;

    public SkoiceEvent(String minecraftId) {
        this.minecraftId = minecraftId;
    }

    public static HandlerList getHandlerList() {
        return SkoiceEvent.HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return SkoiceEvent.HANDLERS;
    }

    public String getMinecraftId() {
        return this.minecraftId;
    }
}
