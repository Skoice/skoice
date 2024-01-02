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
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class ConfigureCommand extends Command {

    public ConfigureCommand(Skoice plugin, CommandExecutor executor, SlashCommandInteraction interaction) {
        super(plugin, executor, CommandInfo.CONFIGURE.isServerManagerRequired(), CommandInfo.CONFIGURE.isBotReadyRequired(), interaction);
    }

    @Override
    public void run() {
        super.plugin.getConfigurationMenu().delete();
        super.interaction.reply(MessageCreateData.fromEditData(super.plugin.getConfigurationMenu().update()))
                .queue(interactionHook -> interactionHook.retrieveOriginal()
                        .queue(message -> super.plugin.getConfigurationMenu().store(message)));
    }
}
