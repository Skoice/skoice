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

package net.clementraynaud.skoice.common.commands;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.bot.BotStatus;
import net.clementraynaud.skoice.common.menus.ConfigurationMenu;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public class ConfigureCommand extends Command {

    public ConfigureCommand(Skoice plugin, CommandExecutor executor, SlashCommandInteraction interaction) {
        super(plugin, executor, CommandInfo.CONFIGURE.isServerManagerRequired(), CommandInfo.CONFIGURE.isBotReadyRequired(), interaction);
    }

    @Override
    public void run() {
        if (this.plugin.getBot().getStatus() == BotStatus.MFA_REQUIRED) {
            this.plugin.getListenerManager().update(this.executor.getUser());
        }

        ConfigurationMenu menu = new ConfigurationMenu(super.plugin.getBot());
        menu.reply(super.interaction);
    }
}
