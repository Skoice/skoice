/*
 * Copyright 2020, 2021, 2022 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.commands.arguments;

import net.clementraynaud.skoice.lang.Minecraft;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Stream;

public enum Argument {
    CONFIGURE {
        @Override
        public void execute(CommandSender sender, String arg) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Minecraft.ILLEGAL_EXECUTOR.toString());
                return;
            }
            if (!sender.isOp()) {
                sender.sendMessage(Minecraft.MISSING_PERMISSION.toString());
                return;
            }
            new ConfigureArgument().execute(sender);
        }},

    TOKEN {
        @Override
        public void execute(CommandSender sender, String arg) {
            if (!sender.isOp()) {
                sender.sendMessage(Minecraft.MISSING_PERMISSION.toString());
                return;
            }
            new TokenArgument().execute(sender, arg);
        }},

    LINK {
        @Override
        public void execute(CommandSender sender, String arg) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Minecraft.ILLEGAL_EXECUTOR.toString());
                return;
            }
            new LinkArgument().execute(sender, arg);
        }},

    UNLINK {
        @Override
        public void execute(CommandSender sender, String arg) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Minecraft.ILLEGAL_EXECUTOR.toString());
                return;
            }
            new UnlinkArgument().execute(sender);
        }};

    //public static String[] getList() {
    //    return Stream.of(Argument.values()).map(Enum::name).map(String::toLowerCase).toArray(String[]::new);
    //}

    public static Argument getArgument(String argument) {
        return Stream.of(Argument.values()).filter(value -> value.toString().equalsIgnoreCase(argument)).findFirst().orElse(null);
    }

    public abstract void execute(CommandSender sender, String arg);
}
