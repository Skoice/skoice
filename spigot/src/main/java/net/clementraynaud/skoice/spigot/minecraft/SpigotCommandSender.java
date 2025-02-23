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

import net.clementraynaud.skoice.common.model.minecraft.SkoiceCommandSender;
import net.clementraynaud.skoice.spigot.SkoiceSpigot;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

public class SpigotCommandSender extends SkoiceCommandSender {

    private final CommandSender sender;

    public SpigotCommandSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public void sendMessage(Component message) {
        SkoiceSpigot.adventure().sender(this.sender).sendMessage(message);
    }

    @Override
    public boolean hasPermission(String permission) {
        return this.sender.hasPermission(permission);
    }
}
