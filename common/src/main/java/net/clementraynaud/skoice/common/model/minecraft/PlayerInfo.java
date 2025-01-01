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

package net.clementraynaud.skoice.common.model.minecraft;

import net.clementraynaud.skoice.common.model.JsonModel;

import java.util.UUID;

public class PlayerInfo extends JsonModel {

    private final UUID id;
    private final boolean dead;
    private final SkoiceGameMode gameMode;
    private final SkoiceLocation location;
    private final String team;
    private final String pluginVersion;
    private String world;

    public PlayerInfo(UUID id, boolean dead, SkoiceGameMode gameMode, String world, SkoiceLocation location, String team, String pluginVersion) {
        this.id = id;
        this.dead = dead;
        this.gameMode = gameMode;
        this.world = world;
        this.location = location;
        this.team = team;
        this.pluginVersion = pluginVersion;
    }

    public UUID getId() {
        return this.id;
    }

    public boolean isDead() {
        return this.dead;
    }

    public String getPluginVersion() {
        return this.pluginVersion;
    }

    public SkoiceGameMode getGameMode() {
        return this.gameMode;
    }

    public String getWorld() {
        return this.world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public SkoiceLocation getLocation() {
        return this.location;
    }

    public String getTeam() {
        return this.team;
    }

    @Override
    public String toString() {
        return "PlayerInfo{" +
                "id=" + this.id +
                ", dead=" + this.dead +
                ", gameMode=" + this.gameMode +
                ", world='" + this.world + '\'' +
                ", location=" + this.location +
                ", team='" + this.team + '\'' +
                ", pluginVersion='" + this.pluginVersion + '\'' +
                '}';
    }
}
