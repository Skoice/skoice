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

import net.kyori.adventure.text.Component;

public class FullPlayer extends BasePlayer {

    private final BasePlayer basePlayer;
    private PlayerInfo playerInfo;

    public FullPlayer(BasePlayer basePlayer, PlayerInfo playerInfo) {
        super(basePlayer.getUniqueId());
        this.basePlayer = basePlayer;
        this.playerInfo = playerInfo;
    }

    public boolean isDead() {
        return this.playerInfo.isDead();
    }

    public SkoiceGameMode getGameMode() {
        return this.playerInfo.getGameMode();
    }

    public String getWorld() {
        return this.playerInfo.getWorld();
    }

    public SkoiceLocation getLocation() {
        return this.playerInfo.getLocation();
    }

    public String getTeam() {
        return this.playerInfo.getTeam();
    }

    public void setPlayerInfo(PlayerInfo playerInfo) {
        this.playerInfo = playerInfo;
    }

    @Override
    public void sendActionBar(Component message) {
        this.basePlayer.sendActionBar(message);
    }

    @Override
    public void sendMessage(Component message) {
        this.basePlayer.sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.basePlayer.hasPermission(permission);
    }

    @Override
    public String toString() {
        return "FullPlayer{" +
                "basePlayer=" + this.basePlayer +
                ", playerInfo=" + this.playerInfo +
                ", id=" + this.id +
                '}';
    }
}
