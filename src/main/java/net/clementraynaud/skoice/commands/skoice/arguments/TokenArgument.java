/*
 * Copyright 2020, 2021, 2022, 2023, 2024 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import org.bukkit.command.CommandSender;

public class TokenArgument extends Argument {

    private final String arg;

    public TokenArgument(Skoice plugin, CommandSender sender, String arg) {
        super(plugin, sender, ArgumentInfo.TOKEN.isAllowedInConsole(), ArgumentInfo.TOKEN.isPermissionRequired(), ArgumentInfo.TOKEN.isHidden());
        this.arg = arg;
    }

    @Override
    public void run() {
        if (this.arg.isEmpty()) {
            super.sender.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.configuration.no-token"));
            return;
        }
        super.plugin.getConfigYamlFile().setToken(this.arg);
        if (super.plugin.getBot().getStatus() == BotStatus.NOT_CONNECTED) {
            super.plugin.getBot().connect(super.sender);
        } else {
            this.sender.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.configuration.bot-already-connected"));
        }
    }
}
