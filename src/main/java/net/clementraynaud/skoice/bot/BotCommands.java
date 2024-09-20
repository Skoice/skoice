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

package net.clementraynaud.skoice.bot;

import net.clementraynaud.skoice.commands.CommandInfo;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BotCommands {

    private final Bot bot;

    public BotCommands(Bot bot) {
        this.bot = bot;
    }

    public CompletableFuture<Void> register() {
        return this.bot.getJDA().updateCommands().addCommands(this.getCommands()).submit()
                .thenAccept(commands -> commands.forEach(command ->
                        this.bot.getLang().getFormatter().set(command.getName() + "-discord-command", command.getAsMention()))
                );
    }

    public void clearGuildCommands() {
        this.bot.getJDA().getGuilds().forEach(guild -> guild.updateCommands().queue());
    }

    private Set<SlashCommandData> getCommands() {
        return Arrays.stream(CommandInfo.values())
                .map(CommandInfo::toString)
                .map(command -> Commands.slash(command, this.bot.getLang().getMessage("command-description." + command)))
                .collect(Collectors.toSet());
    }
}
