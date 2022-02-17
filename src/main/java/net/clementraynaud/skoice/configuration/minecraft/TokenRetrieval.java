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

package net.clementraynaud.skoice.configuration.minecraft;

import net.clementraynaud.skoice.lang.Minecraft;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Base64;

import static net.clementraynaud.skoice.Skoice.getBot;
import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.bot.Connection.getJda;

public class TokenRetrieval implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!sender.isOp()) {
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(Minecraft.NO_TOKEN.toString());
            return true;
        }
        if (args[0].length() != 59 || !args[0].matches("[a-zA-Z0-9_.]+")) {
            sender.sendMessage(Minecraft.INVALID_TOKEN.toString());
            return true;
        }
        byte[] tokenBytes = args[0].getBytes();
        for (int i = 0; i < tokenBytes.length; i++) {
            tokenBytes[i]++;
        }
        String base64Token = Base64.getEncoder().encodeToString(tokenBytes);
        getPlugin().getConfigFile().set("token", base64Token);
        getPlugin().saveConfig();
        getPlugin().setTokenBoolean(true);
        if (getJda() == null) {
            getBot().connectBot(false, sender);
        } else {
            sender.sendMessage(Minecraft.BOT_ALREADY_CONNECTED.toString());
        }
        return true;
    }
}
