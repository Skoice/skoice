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
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.lang.Lang;
import net.clementraynaud.skoice.listeners.interaction.ButtonClickListener;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.awt.*;

public class Response {

    private final Skoice plugin;
    private final Config config;
    private final Lang lang;
    private final Bot bot;

    public Response(Skoice plugin, Config config, Lang lang, Bot bot) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        this.bot = bot;
    }

    public Message getMessage() {
        if (this.bot.isOnMultipleGuilds()) {
            return this.bot.getMenus().get("server").toMessage(this.config, this.lang, this.bot);
        } else if (!this.config.getFile().contains(ConfigField.LOBBY_ID.get())) {
            return this.bot.getMenus().get("lobby").toMessage(this.config, this.lang, this.bot);
        } else if (!this.config.getFile().contains(ConfigField.HORIZONTAL_RADIUS.get())
                || !this.config.getFile().contains(ConfigField.VERTICAL_RADIUS.get())) {
            return this.bot.getMenus().get("mode").toMessage(this.config, this.lang, this.bot);
        } else {
            return this.bot.getMenus().get("configuration").toMessage(this.config, this.lang, this.bot);
        }
    }

    public void deleteMessage() {
        Message configurationMessage = this.getConfigurationMessage();
        if (configurationMessage != null) {
            this.getConfigurationMessage().delete().queue();
        }
    }

    public Message getConfigurationMessage() {
        if (!this.config.getFile().contains(ConfigField.TEMP_MESSAGE.get())) {
            return null;
        }
        Message message = this.config.getFile().getObject(ConfigField.TEMP_MESSAGE.get(), Message.class);
        if (message == null) {
            this.config.getFile().set(ConfigField.TEMP_MESSAGE.get(), null);
            this.config.saveFile();
            ButtonClickListener.discordIDAxis.clear();
            return null;
        }
        return message;
    }

    public void sendLobbyDeletedAlert(Guild guild) {
        this.config.getFile().set(ConfigField.LOBBY_ID.get(), null);
        this.config.saveFile();
        this.plugin.updateConfigurationStatus(false);
        User user = guild.retrieveAuditLogs().limit(1).type(ActionType.CHANNEL_DELETE).complete().get(0).getUser();
        if (user != null && !user.isBot()) {
            user.openPrivateChannel().complete()
                    .sendMessageEmbeds(new EmbedBuilder().setTitle(MenuEmoji.GEAR + this.lang.getMessage("discord.menu.configuration.title"))
                            .addField(MenuEmoji.WARNING + this.lang.getMessage("discord.field.incomplete-configuration.title"),
                                    this.lang.getMessage("discord.field.incomplete-configuration.server-manager-alternative-description"), false)
                            .setColor(Color.RED).build())
                    .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
        }
    }
}
