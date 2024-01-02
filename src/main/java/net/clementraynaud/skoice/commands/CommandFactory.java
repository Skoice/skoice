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

package net.clementraynaud.skoice.commands;

import net.clementraynaud.skoice.Skoice;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public class CommandFactory {

    public Command getCommand(Skoice plugin, CommandInfo commandInfo, CommandExecutor executor, SlashCommandInteraction interaction) {
        switch (commandInfo) {
            case CONFIGURE:
                return new ConfigureCommand(plugin, executor, interaction);
            case LINK:
                return new LinkCommand(plugin, executor, interaction);
            case UNLINK:
                return new UnlinkCommand(plugin, executor, interaction);
            case INVITE:
                return new InviteCommand(plugin, executor, interaction);
            default:
                return null;
        }
    }
}
