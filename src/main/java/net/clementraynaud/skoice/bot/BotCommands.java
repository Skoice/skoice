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
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BotCommands {

    private final Skoice plugin;

    public BotCommands(Skoice plugin) {
        this.plugin = plugin;
    }

    public void register(Guild guild) {
        guild.updateCommands().addCommands(this.getCommands())
                .queue(null, new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS,
                        e -> this.plugin.getLogger().severe(this.plugin.getLang().getMessage("logger.error.missing-access",
                                this.plugin.getBot().getJDA().getSelfUser().getApplicationId()))));
    }

    private Set<SlashCommandData> getCommands() {
        return new HashSet<>(Arrays.asList(
                Commands.slash("configure", this.plugin.getLang().getMessage("discord.command-description.configure")),
                Commands.slash("link", this.plugin.getLang().getMessage("discord.command-description.link")),
                Commands.slash("unlink", this.plugin.getLang().getMessage("discord.command-description.unlink")),
                Commands.slash("invite", this.plugin.getLang().getMessage("discord.command-description.invite"))));
    }
}
