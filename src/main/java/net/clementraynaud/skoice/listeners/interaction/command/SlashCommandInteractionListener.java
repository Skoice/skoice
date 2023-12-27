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

package net.clementraynaud.skoice.listeners.interaction.command;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.commands.*;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;

public class SlashCommandInteractionListener extends ListenerAdapter {

    private final Skoice plugin;

    public SlashCommandInteractionListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (Arrays.stream(CommandInfo.values()).noneMatch(value -> value.toString().equalsIgnoreCase(event.getName()))) {
            return;
        }

        CommandInfo commandInfo = CommandInfo.valueOf(event.getName().toUpperCase());

        boolean serverManager = false;
        Member member = event.getMember();
        if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
            serverManager = true;
        }

        CommandExecutor executor = new CommandExecutor(event.getUser(), event.isFromGuild(), serverManager);

        Command command = new CommandFactory().getCommand(this.plugin, commandInfo, executor, event);

        if (command != null && !command.cannotBeExecuted()) {
            command.run();
        }
    }
}
