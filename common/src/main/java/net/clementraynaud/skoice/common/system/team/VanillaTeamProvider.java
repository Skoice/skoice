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
 * The built-in team provider, backed by Minecraft's native team system: the
 * scoreboard on Spigot and team packets on Velocity. Both are exposed through
 * {@link FullPlayer#getTeam()}.
 */
public class VanillaTeamProvider implements TeamProvider {

    public static final String ID = "vanilla";

    @Override
    public String getId() {
        return VanillaTeamProvider.ID;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getTeam(FullPlayer player) {
        return player.getTeam();
    }
}
