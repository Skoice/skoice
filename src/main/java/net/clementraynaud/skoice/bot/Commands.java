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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.lang.LoggerLang;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.*;

import static net.clementraynaud.skoice.Skoice.getPlugin;

public class Commands {

    public void register(Guild guild) {
        Map<String, CommandData> commandsToRegister = getCommands();
        List<Command> registeredCommands = guild.retrieveCommands().complete();
        for (Command registeredCommand : registeredCommands)
            commandsToRegister.remove(registeredCommand.getName());
        try {
            commandsToRegister.values().forEach(command -> guild.upsertCommand(command).queue());
        } catch (ErrorResponseException e) {
            getPlugin().getLogger().severe(LoggerLang.MISSING_ACCESS_ERROR.toString());
        }
    }

    public void reload(Guild guild) {
        try {
            guild.updateCommands().addCommands(getCommands().values()).queue();
        } catch (ErrorResponseException e) {
            getPlugin().getLogger().severe(LoggerLang.MISSING_ACCESS_ERROR.toString());
        }
    }

    private Map<String, CommandData> getCommands() {
        return Maps.newHashMap(ImmutableMap.of(
                "configure", new CommandData("configure", DiscordLang.CONFIGURE_COMMAND_DESCRIPTION.toString()),
                "link", new CommandData("link", DiscordLang.LINK_COMMAND_DESCRIPTION.toString()),
                "unlink", new CommandData("unlink", DiscordLang.UNLINK_COMMAND_DESCRIPTION.toString()),
                "invite", new CommandData("invite", DiscordLang.INVITE_COMMAND_DESCRIPTION.toString())));
    }
}
