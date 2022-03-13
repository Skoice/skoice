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

import net.clementraynaud.skoice.lang.MinecraftLang;
import org.bukkit.command.CommandSender;

import static net.clementraynaud.skoice.Skoice.getBot;
import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.bot.Bot.getJda;
import static net.clementraynaud.skoice.config.Config.setToken;

public class TokenArgument extends Argument {

    public TokenArgument(CommandSender sender, String arg) {
        super(sender, arg, true, true);
    }

    @Override
    public void run() {
        if (!canExecuteCommand())
            return;
        if (arg.isEmpty()) {
            sender.sendMessage(MinecraftLang.NO_TOKEN.toString());
            return;
        }
        if (arg.length() != 59 || !arg.matches("[a-zA-Z0-9_.]+")) {
            sender.sendMessage(MinecraftLang.INVALID_TOKEN.toString());
            return;
        }
        setToken(arg);
        getPlugin().setTokenBoolean(true);
        if (getJda() == null) {
            getBot().connectBot(false, sender);
        } else {
            sender.sendMessage(MinecraftLang.BOT_ALREADY_CONNECTED.toString());
        }
    }
}
