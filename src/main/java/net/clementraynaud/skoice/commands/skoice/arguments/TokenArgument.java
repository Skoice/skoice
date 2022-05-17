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

package net.clementraynaud.skoice.commands.skoice.arguments;

import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.lang.Lang;
import org.bukkit.command.CommandSender;

public class TokenArgument extends Argument {

    private final Bot bot;
    private final String arg;

    public TokenArgument(Config config, Lang lang, CommandSender sender, Bot bot, String arg) {
        super(config, lang, sender, true, true);
        this.bot = bot;
        this.arg = arg;
    }

    @Override
    public void run() {
        if (!this.canExecuteCommand()) {
            return;
        }
        if (this.arg.isEmpty()) {
            this.sender.sendMessage(super.lang.getMessage("minecraft.chat.configuration.no-token"));
            return;
        }
        if (this.arg.length() != 59 || !this.arg.matches("[a-zA-Z0-9_.]+")) {
            this.sender.sendMessage(super.lang.getMessage("minecraft.chat.configuration.invalid-token"));
            return;
        }
        this.config.getUpdater().setToken(this.arg);
        if (this.bot.getJda() == null) {
            this.bot.connect(this.sender);
            if (this.bot.getJda() != null) {
                this.config.initializeReader(this.bot);
                this.bot.setup(false, this.sender);
            }
        } else {
            this.sender.sendMessage(super.lang.getMessage("minecraft.chat.configuration.bot-already-connected"));
        }
    }
}
