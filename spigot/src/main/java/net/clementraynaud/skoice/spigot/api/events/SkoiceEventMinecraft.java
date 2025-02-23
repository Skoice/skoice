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

package net.clementraynaud.skoice.spigot.api.events;

import java.util.UUID;

public class SkoiceEventMinecraft extends SkoiceEvent {

    private UUID minecraftId;

    public SkoiceEventMinecraft(String minecraftId) {
        super();
        try {
            this.minecraftId = UUID.fromString(minecraftId);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public UUID getMinecraftId() {
        return this.minecraftId;
    }
}