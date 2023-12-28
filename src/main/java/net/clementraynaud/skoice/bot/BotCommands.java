/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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
import net.clementraynaud.skoice.commands.CommandInfo;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BotCommands {

    private final Skoice plugin;

    private final Map<String, String> mentions = new HashMap<>();

    public BotCommands(Skoice plugin) {
        this.plugin = plugin;
    }

    public void register() {
        this.plugin.getBot().getJDA().updateCommands().addCommands(this.getCommands()).queue();

        this.plugin.getBot().getJDA().retrieveCommands()
                .queue(commands -> commands.forEach(command -> this.mentions.put(command.getName(), command.getAsMention())));
    }

    public void clearGuildCommands() {
        this.plugin.getBot().getJDA().getGuilds().forEach(guild -> guild.updateCommands().queue());
    }

    private Set<SlashCommandData> getCommands() {
        return Arrays.stream(CommandInfo.values())
                .map(CommandInfo::toString)
                .map(command -> Commands.slash(command, this.plugin.getLang().getMessage("discord.command-description." + command)))
                .collect(Collectors.toSet());
    }

    public String getAsMention(String command) {
        return this.mentions.get(command);
    }
}
