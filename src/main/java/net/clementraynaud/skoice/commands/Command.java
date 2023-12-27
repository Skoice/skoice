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

package net.clementraynaud.skoice.commands;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class Command {

    protected final Skoice plugin;
    protected final CommandExecutor executor;
    protected final boolean serverManagerRequired;
    protected final boolean botReadyRequired;
    protected final SlashCommandInteractionEvent event;

    protected Command(Skoice plugin, CommandExecutor executor, boolean serverManagerRequired, boolean botReadyRequired, SlashCommandInteractionEvent event) {
        this.plugin = plugin;
        this.executor = executor;
        this.serverManagerRequired = serverManagerRequired;
        this.botReadyRequired = botReadyRequired;
        this.event = event;
    }

    public abstract void run();

    public boolean cannotBeExecuted() {
        if (this.serverManagerRequired && !this.executor.isInGuild()) {
            this.event.reply("Temporary message").setEphemeral(true).queue();
            return true;
        }

        if (this.serverManagerRequired && this.executor.isInGuild() && !this.executor.isServerManager()) {
            this.event.reply(this.plugin.getBot().getMenu("access-denied").build())
                    .setEphemeral(true).queue();
            return true;
        }

        if (this.botReadyRequired && this.plugin.getBot().getStatus() != BotStatus.READY) {
            if (this.executor.isServerManager()) {
                this.event.reply(this.plugin.getBot().getMenu("incomplete-configuration-server-manager")
                                .build(this.plugin.getBotCommands().getCommandMentions().get(CommandInfo.CONFIGURE.toString())))
                        .setEphemeral(true).queue();
            } else {
                this.event.reply(this.plugin.getBot().getMenu("incomplete-configuration").build())
                        .setEphemeral(true).queue();
            }
            return true;
        }
        return false;
    }
}
