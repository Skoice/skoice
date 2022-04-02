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

package net.clementraynaud.skoice.listeners.message.guild;

import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.commands.interaction.ButtonInteraction.discordIDAxis;
import static net.clementraynaud.skoice.config.Config.TEMP_FIELD;
import static net.clementraynaud.skoice.config.Config.TEMP_MESSAGE_ID_FIELD;

public class GuildMessageDeleteListener extends ListenerAdapter {

    @Override
    public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {
        if (getPlugin().getConfig().contains(TEMP_FIELD)
                && event.getMessageId().equals(getPlugin().getConfig().getString(TEMP_MESSAGE_ID_FIELD))) {
            getPlugin().getConfig().set(TEMP_FIELD, null);
            getPlugin().saveConfig();
            discordIDAxis.clear();
        }
    }
}
