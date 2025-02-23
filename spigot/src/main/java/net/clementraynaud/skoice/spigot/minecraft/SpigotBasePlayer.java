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

import net.clementraynaud.skoice.common.model.minecraft.BasePlayer;
import net.clementraynaud.skoice.spigot.SkoicePluginSpigot;
import net.clementraynaud.skoice.spigot.SkoiceSpigot;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class SpigotBasePlayer extends BasePlayer {

    private final Player player;

    public SpigotBasePlayer(Player player) {
        super(player.getUniqueId());
        this.player = player;
    }

    @Override
    public void sendActionBar(Component message) {
        SkoiceSpigot.adventure().player(this.player).sendActionBar(message);
    }

    @Override
    public void sendMessage(Component message) {
        if (!SkoicePluginSpigot.isProxyMode()) {
            SkoiceSpigot.adventure().player(this.player).sendMessage(message);
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.player.hasPermission(permission);
    }
}
