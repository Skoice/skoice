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

import net.clementraynaud.skoice.Skoice;
import org.bukkit.command.CommandSender;

public class ConnectArgument extends Argument {

    private final String arg;

    public ConnectArgument(Skoice plugin, CommandSender sender, String arg) {
        super(plugin, sender, ArgumentInfo.CONNECT.isAllowedInConsole(), ArgumentInfo.CONNECT.isRestrictedToOperators());
        this.arg = arg;
    }

    @Override
    public void run() {
        if (!this.canExecuteCommand()) {
            return;
        }
        if (this.arg.isEmpty()) {
            this.sender.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.configuration.no-token"));
            return;
        }
        super.plugin.getConfiguration().setToken(this.arg);
        if (super.plugin.getBot().getJDA() == null) {
            super.plugin.getBot().connect(this.sender);
            if (super.plugin.getBot().getJDA() != null) {
                super.plugin.getBot().setup(this.sender);
            }
        } else {
            this.sender.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.configuration.bot-already-connected"));
        }
    }
}
