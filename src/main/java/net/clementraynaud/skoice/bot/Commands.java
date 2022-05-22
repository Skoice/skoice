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

import net.clementraynaud.skoice.Skoice;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.*;

public class Commands {

    private final Skoice plugin;

    public Commands(Skoice plugin) {
        this.plugin = plugin;
    }

    public void register(Guild guild) {
        guild.updateCommands().addCommands(this.getCommands())
                .queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS,
                        e -> this.plugin.getLogger().severe(this.plugin.getLang().getMessage("logger.error.missing-access",
                                this.plugin.getBot().getJda().getSelfUser().getId()))));
    }

    private Set<CommandData> getCommands() {
        return new HashSet<>(Arrays.asList(
                new CommandData("configure", this.plugin.getLang().getMessage("discord.command-description.configure")),
                new CommandData("link", this.plugin.getLang().getMessage("discord.command-description.link")),
                new CommandData("unlink", this.plugin.getLang().getMessage("discord.command-description.unlink")),
                new CommandData("invite", this.plugin.getLang().getMessage("discord.command-description.invite"))));
    }
}
