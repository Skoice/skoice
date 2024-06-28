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
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.menus.EmbeddedMenu;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public abstract class Command {

    protected final Skoice plugin;
    protected final CommandExecutor executor;
    protected final boolean serverManagerRequired;
    protected final boolean botReadyRequired;
    protected final SlashCommandInteraction interaction;

    protected Command(Skoice plugin, CommandExecutor executor, boolean serverManagerRequired, boolean botReadyRequired, SlashCommandInteraction interaction) {
        this.plugin = plugin;
        this.executor = executor;
        this.serverManagerRequired = serverManagerRequired;
        this.botReadyRequired = botReadyRequired;
        this.interaction = interaction;
    }

    public abstract void run();

    public boolean canBeExecuted() {
        if (this.serverManagerRequired
                && !this.executor.isInGuild()
                && this.plugin.getBot().getStatus() == BotStatus.MULTIPLE_GUILDS) {
            this.interaction.reply("Requires to be performed in a guild (temporary message)").setEphemeral(true).queue();
            return false;
        }

        if (this.serverManagerRequired
                && !this.executor.isServerManager()) {
            new EmbeddedMenu(this.plugin.getBot()).setContent("access-denied")
                    .reply(this.interaction);
            return false;
        }

        if (this.botReadyRequired
                && this.plugin.getBot().getStatus() != BotStatus.READY) {
            if (this.executor.isServerManager()) {
                new EmbeddedMenu(this.plugin.getBot()).setContent("incomplete-configuration-server-manager",
                                this.plugin.getBotCommands().getAsMention(CommandInfo.CONFIGURE.toString()))
                        .reply(this.interaction);
            } else {
                new EmbeddedMenu(this.plugin.getBot()).setContent("incomplete-configuration",
                                this.plugin.getBotCommands().getAsMention(CommandInfo.CONFIGURE.toString()))
                        .reply(this.interaction);
            }
            return false;
        }
        return true;
    }
}
