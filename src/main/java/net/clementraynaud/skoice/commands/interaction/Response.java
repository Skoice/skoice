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

package net.clementraynaud.skoice.commands.interaction;

import net.clementraynaud.skoice.menus.Menu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.bot.Bot.getJda;
import static net.clementraynaud.skoice.config.Config.*;

public class Response {

    public Message getMessage() {
        if (!getPlugin().isGuildUnique()) {
            return Menu.SERVER.getMessage();
        } else if (!getPlugin().getConfig().contains(LOBBY_ID_FIELD)) {
            return Menu.LOBBY.getMessage();
        } else if (!getPlugin().getConfig().contains(HORIZONTAL_RADIUS_FIELD)
                || !getPlugin().getConfig().contains(VERTICAL_RADIUS_FIELD)) {
            return Menu.MODE.getMessage();
        } else {
            return Menu.CONFIGURATION.getMessage();
        }
    }

    public void deleteMessage() {
        Message configurationMessage = getConfigurationMessage();
        if (configurationMessage != null)
            try {
                getConfigurationMessage().delete().queue(success -> {
                }, failure -> {
                });
            } catch (ErrorResponseException ignored) {
            }
    }

    public Message getConfigurationMessage() {
        if (getPlugin().getConfig().contains(TEMP_FIELD)) {
            Guild guild = getJda().getGuildById(getPlugin().getConfig().getString(TEMP_GUILD_ID_FIELD));
            if (guild != null) {
                TextChannel textChannel = guild.getTextChannelById(getPlugin().getConfig().getString(TEMP_TEXT_CHANNEL_ID_FIELD));
                if (textChannel != null)
                    try {
                        return textChannel.retrieveMessageById(getPlugin().getConfig().getString(TEMP_MESSAGE_ID_FIELD)).complete();
                    } catch (ErrorResponseException ignored) {
                    }
            }
        }
        return null;
    }
}
