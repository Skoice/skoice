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

package net.clementraynaud.skoice.bot;

import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.lang.LoggerLang;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.*;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.bot.Bot.getJda;

public class Commands {

    public void register(Guild guild) {
        guild.updateCommands().addCommands(getCommands())
                .queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS,
                        e -> getPlugin().getLogger().severe(String.format(LoggerLang.MISSING_ACCESS_ERROR.toString(), getJda().getSelfUser().getId()))));
    }

    private Set<CommandData> getCommands() {
        return new HashSet<>(Arrays.asList(
                new CommandData("configure", DiscordLang.CONFIGURE_COMMAND_DESCRIPTION.toString()),
                new CommandData("link", DiscordLang.LINK_COMMAND_DESCRIPTION.toString()),
                new CommandData("unlink", DiscordLang.UNLINK_COMMAND_DESCRIPTION.toString()),
                new CommandData("invite", DiscordLang.INVITE_COMMAND_DESCRIPTION.toString())));
    }
}
