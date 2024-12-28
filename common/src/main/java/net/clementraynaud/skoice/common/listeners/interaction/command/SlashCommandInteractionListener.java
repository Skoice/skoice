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

package net.clementraynaud.skoice.common.listeners.interaction.command;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.bot.BotStatus;
import net.clementraynaud.skoice.common.commands.Command;
import net.clementraynaud.skoice.common.commands.CommandExecutor;
import net.clementraynaud.skoice.common.commands.CommandFactory;
import net.clementraynaud.skoice.common.commands.CommandInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.Arrays;

public class SlashCommandInteractionListener extends ListenerAdapter {

    private final Skoice plugin;
    private final CommandFactory commandFactory;

    public SlashCommandInteractionListener(Skoice plugin) {
        this.plugin = plugin;
        this.commandFactory = new CommandFactory();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (Arrays.stream(CommandInfo.values()).noneMatch(value -> value.toString().equalsIgnoreCase(event.getName()))) {
            return;
        }
        CommandInfo commandInfo = CommandInfo.valueOf(event.getName().toUpperCase());
        CommandExecutor executor = new CommandExecutor(event.getInteraction());
        Command command = this.commandFactory.getCommand(this.plugin, commandInfo, executor, event.getInteraction());
        if (command == null) {
            return;
        }

        if (!executor.isInGuild()
                && this.plugin.getBot().getStatus() != BotStatus.MULTIPLE_GUILDS) {
            this.plugin.getBot().getGuild().retrieveMember(executor.getUser()).queue(member -> {
                if (member.hasPermission(Permission.MANAGE_SERVER)) {
                    executor.setServerManager(true);
                }

                if (command.canBeExecuted()) {
                    command.run();
                }
            }, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MEMBER));

        } else {
            if (command.canBeExecuted()) {
                command.run();
            }
        }
    }
}
