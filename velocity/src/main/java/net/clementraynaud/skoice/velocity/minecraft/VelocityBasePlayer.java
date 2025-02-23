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

package net.clementraynaud.skoice.velocity.minecraft;

import com.velocitypowered.api.proxy.Player;
import net.clementraynaud.skoice.common.model.minecraft.BasePlayer;
import net.kyori.adventure.text.Component;

public class VelocityBasePlayer extends BasePlayer {

    private final Player player;

    public VelocityBasePlayer(Player player) {
        super(player.getUniqueId());
        this.player = player;
    }

    @Override
    public void sendMessage(Component message) {
        this.player.sendMessage(message);
    }

    @Override
    public void sendActionBar(Component message) {
        this.player.sendActionBar(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.player.hasPermission(permission);
    }
}
