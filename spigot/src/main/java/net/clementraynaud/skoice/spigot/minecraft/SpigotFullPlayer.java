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

package net.clementraynaud.skoice.spigot.minecraft;

import net.clementraynaud.skoice.common.model.minecraft.FullPlayer;
import net.clementraynaud.skoice.common.model.minecraft.PlayerInfo;
import net.clementraynaud.skoice.common.model.minecraft.SkoiceGameMode;
import net.clementraynaud.skoice.common.model.minecraft.SkoiceLocation;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class SpigotFullPlayer extends FullPlayer {

    private final Player playerInfo;

    public SpigotFullPlayer(Player player) {
        super(new SpigotBasePlayer(player), null);
        this.playerInfo = player;
    }

    @Override
    public boolean isDead() {
        return this.playerInfo.isDead();
    }

    @Override
    public SkoiceGameMode getGameMode() {
        return SkoiceGameMode.valueOf(this.playerInfo.getGameMode().toString());
    }

    @Override
    public String getWorld() {
        return this.playerInfo.getWorld().getName();
    }

    @Override
    public SkoiceLocation getLocation() {
        return new SkoiceLocation(this.playerInfo.getLocation().getX(), this.playerInfo.getLocation().getY(), this.playerInfo.getLocation().getZ());
    }

    @Override
    public void setPlayerInfo(PlayerInfo playerInfo) {
        throw new UnsupportedOperationException("Cannot set player info for SpigotFullPlayer");
    }

    @Override
    public String getTeam() {
        Scoreboard scoreboard = this.playerInfo.getScoreboard();
        Team playerTeam = scoreboard.getEntryTeam(this.playerInfo.getName());
        return playerTeam == null ? null : playerTeam.getName();
    }
}
