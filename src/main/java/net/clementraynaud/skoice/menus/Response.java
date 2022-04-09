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

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.listeners.interaction.ButtonClickListener;
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

public class Response {

    public Message getMessage() {
        if (!Skoice.getPlugin().isGuildUnique()) {
            return Menu.SERVER.getMessage();
        } else if (!Skoice.getPlugin().getConfig().contains(Config.LOBBY_ID_FIELD)) {
            return Menu.LOBBY.getMessage();
        } else if (!Skoice.getPlugin().getConfig().contains(Config.HORIZONTAL_RADIUS_FIELD)
                || !Skoice.getPlugin().getConfig().contains(Config.VERTICAL_RADIUS_FIELD)) {
            return Menu.MODE.getMessage();
        } else {
            return Menu.CONFIGURATION.getMessage();
        }
    }

    public void deleteMessage() {
        Message configurationMessage = this.getConfigurationMessage();
        if (configurationMessage != null) {
            this.getConfigurationMessage().delete().queue();
        }
    }

    public Message getConfigurationMessage() {
        if (Skoice.getPlugin().getConfig().contains(Config.TEMP_FIELD)) {
            Guild guild = Bot.getJda().getGuildById(Skoice.getPlugin().getConfig().getString(Config.TEMP_GUILD_ID_FIELD));
            if (guild != null) {
                TextChannel textChannel = guild.getTextChannelById(Skoice.getPlugin().getConfig().getString(Config.TEMP_TEXT_CHANNEL_ID_FIELD));
                if (textChannel != null) {
                    try {
                        return textChannel.retrieveMessageById(Skoice.getPlugin().getConfig().getString(Config.TEMP_MESSAGE_ID_FIELD)).complete();
                    } catch (ErrorResponseException e) {
                        Skoice.getPlugin().getConfig().set(Config.TEMP_FIELD, null);
                        Skoice.getPlugin().saveConfig();
                        ButtonClickListener.discordIDAxis.clear();
                    }
                }
            }
        }
        return null;
    }

    public void sendLobbyDeletedAlert(Guild guild) {
        Skoice.getPlugin().getConfig().set(Config.LOBBY_ID_FIELD, null);
        Skoice.getPlugin().saveConfig();
        Skoice.getPlugin().updateConfigurationStatus(false);
        User user = guild.retrieveAuditLogs().limit(1).type(ActionType.CHANNEL_DELETE).complete().get(0).getUser();
        if (user != null && !user.isBot()) {
            user.openPrivateChannel().complete()
                    .sendMessageEmbeds(new EmbedBuilder().setTitle(MenuEmoji.GEAR + DiscordLang.CONFIGURATION_EMBED_TITLE.toString())
                            .addField(MenuEmoji.WARNING + DiscordLang.INCOMPLETE_CONFIGURATION_FIELD_TITLE.toString(),
                                    DiscordLang.INCOMPLETE_CONFIGURATION_SERVER_MANAGER_FIELD_ALTERNATIVE_DESCRIPTION.toString(), false)
                            .setColor(Color.RED).build())
                    .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
        }
    }
}
