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
 * Base class for all Skoice events involving both a Minecraft player and a Discord user.
 * <p>
 * This abstract class extends {@link SkoiceEventMinecraft} and adds Discord user information
 * for events that involve a link between Minecraft and Discord accounts.
 *
 * @see SkoiceEventMinecraft
 */
public abstract class SkoiceEventLink extends SkoiceEventMinecraft {

    private final String discordId;

    protected SkoiceEventLink(String minecraftId, String discordId) {
        super(minecraftId);
        this.discordId = discordId;
    }

    /**
     * Gets the Discord user's ID.
     *
     * @return the Discord user ID
     */
    public String getDiscordId() {
        return this.discordId;
    }
}