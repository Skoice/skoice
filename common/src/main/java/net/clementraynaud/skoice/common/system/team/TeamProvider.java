/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025, 2026 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.common.system.team;

import net.clementraynaud.skoice.common.model.minecraft.FullPlayer;

/**
 * A source of team membership used by Skoice's team-communication and
 * separated-teams features. Implementations map a player to a stable team
 * identifier so that two players sharing the same identifier are considered
 * teammates.
 * <p>
 * Providers are treated as low-priority integrations: they must never throw
 * into Skoice's hot paths. Implementations should return {@code null} rather
 * than propagating exceptions.
 */
public interface TeamProvider {

    /**
     * @return the config identifier of this provider (e.g. {@code "vanilla"}).
     */
    String getId();

    /**
     * @return whether the backing team system is present and usable.
     */
    boolean isAvailable();

    /**
     * @param player the player to resolve
     * @return a stable team identifier for the player, or {@code null} if the
     * player is in no team or the team cannot be resolved.
     */
    String getTeam(FullPlayer player);
}
