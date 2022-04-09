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

package net.clementraynaud.skoice.menus;

import net.clementraynaud.skoice.lang.DiscordLang;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.awt.*;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.bot.Bot.getJda;
import static net.clementraynaud.skoice.listeners.interaction.ButtonClickListener.discordIDAxis;
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
            getConfigurationMessage().delete().queue();
    }

    public Message getConfigurationMessage() {
        if (getPlugin().getConfig().contains(TEMP_FIELD)) {
            Guild guild = getJda().getGuildById(getPlugin().getConfig().getString(TEMP_GUILD_ID_FIELD));
            if (guild != null) {
                TextChannel textChannel = guild.getTextChannelById(getPlugin().getConfig().getString(TEMP_TEXT_CHANNEL_ID_FIELD));
                if (textChannel != null)
                    try {
                        return textChannel.retrieveMessageById(getPlugin().getConfig().getString(TEMP_MESSAGE_ID_FIELD)).complete();
                    } catch (ErrorResponseException e) {
                        getPlugin().getConfig().set(TEMP_FIELD, null);
                        getPlugin().saveConfig();
                        discordIDAxis.clear();
                    }
            }
        }
        return null;
    }

    public void sendLobbyDeletedAlert(Guild guild) {
        getPlugin().getConfig().set(LOBBY_ID_FIELD, null);
        getPlugin().saveConfig();
        getPlugin().updateConfigurationStatus(false);
        User user = guild.retrieveAuditLogs().limit(1).type(ActionType.CHANNEL_DELETE).complete().get(0).getUser();
        if (user != null && !user.isBot())
            user.openPrivateChannel().complete()
                    .sendMessageEmbeds(new EmbedBuilder().setTitle(MenuEmoji.GEAR + DiscordLang.CONFIGURATION_EMBED_TITLE.toString())
                            .addField(MenuEmoji.WARNING + DiscordLang.INCOMPLETE_CONFIGURATION_FIELD_TITLE.toString(),
                                    DiscordLang.INCOMPLETE_CONFIGURATION_SERVER_MANAGER_FIELD_ALTERNATIVE_DESCRIPTION.toString(), false)
                            .setColor(Color.RED).build())
                    .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
    }
}
