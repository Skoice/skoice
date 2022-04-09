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

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.lang.MinecraftLang;
import org.bukkit.command.CommandSender;

public class TokenArgument extends Argument {

    public TokenArgument(CommandSender sender, String arg) {
        super(sender, arg, true, true);
    }

    @Override
    public void run() {
        if (!this.canExecuteCommand()) {
            return;
        }
        if (this.arg.isEmpty()) {
            this.sender.sendMessage(MinecraftLang.NO_TOKEN.toString());
            return;
        }
        if (this.arg.length() != 59 || !this.arg.matches("[a-zA-Z0-9_.]+")) {
            this.sender.sendMessage(MinecraftLang.INVALID_TOKEN.toString());
            return;
        }
        Config.setToken(this.arg);
        Skoice.getPlugin().setTokenBoolean(true);
        if (Bot.getJda() == null) {
            Skoice.getBot().connectBot(false, this.sender);
        } else {
            this.sender.sendMessage(MinecraftLang.BOT_ALREADY_CONNECTED.toString());
        }
    }
}
