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
import net.clementraynaud.skoice.config.ConfigurationField;
import net.clementraynaud.skoice.listeners.interaction.ButtonClickListener;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class ConfigurationMenu {

    private final Skoice plugin;

    public ConfigurationMenu(Skoice plugin) {
        this.plugin = plugin;
    }

    public Message getMessage() {
        if (this.plugin.getBot().isOnMultipleGuilds()) {
            return this.plugin.getBot().getMenu("server").toMessage();
        } else if (!this.plugin.getBot().getJda().getGuilds().get(0).getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
            return this.plugin.getBot().getMenu("permissions").toMessage();
        } else if (!this.plugin.getConfiguration().getFile().contains(ConfigurationField.LOBBY_ID.toString())) {
            return this.plugin.getBot().getMenu("lobby").toMessage();
        } else if (!this.plugin.getConfiguration().getFile().contains(ConfigurationField.HORIZONTAL_RADIUS.toString())
                || !this.plugin.getConfiguration().getFile().contains(ConfigurationField.VERTICAL_RADIUS.toString())) {
            return this.plugin.getBot().getMenu("mode").toMessage();
        } else {
            return this.plugin.getBot().getMenu("settings").toMessage();
        }
    }

    public boolean exists() {
        return this.plugin.getConfiguration().getFile().contains(ConfigurationField.CONFIG_MENU.toString());
    }

    public String getMessageId() {
        Message message = this.retrieveMessage();
        if (message != null) {
            return message.getId();
        }
        return "";
    }

    public Message retrieveMessage() {
        if (!this.exists()) {
            return null;
        }
        Guild guild = this.plugin.getBot().getJda().getGuildById(this.plugin.getConfiguration().getFile()
                .getString(ConfigurationField.getPath(ConfigurationField.CONFIG_MENU, ConfigurationField.GUILD_ID)));
        if (guild == null) {
            return null;
        }
        TextChannel textChannel = guild.getTextChannelById(this.plugin.getConfiguration().getFile()
                .getString(ConfigurationField.getPath(ConfigurationField.CONFIG_MENU, ConfigurationField.TEXT_CHANNEL_ID)));
        if (textChannel == null) {
            return null;
        }
        try {
            return textChannel.retrieveMessageById(this.plugin.getConfiguration().getFile()
                    .getString(ConfigurationField.getPath(ConfigurationField.CONFIG_MENU, ConfigurationField.MESSAGE_ID))).complete();
        } catch (ErrorResponseException e) {
            this.clearConfig();
            ButtonClickListener.getDiscordIdAxis().clear();
        }
        return null;
    }

    public void deleteMessage() {
        Message configurationMessage = this.retrieveMessage();
        if (configurationMessage != null) {
            configurationMessage.delete().queue();
        }
    }

    public void storeInConfig(Message message) {
        this.plugin.getConfiguration().getFile()
                .set(ConfigurationField.getPath(ConfigurationField.CONFIG_MENU, ConfigurationField.GUILD_ID),
                        message.getGuild().getId());
        this.plugin.getConfiguration().getFile()
                .set(ConfigurationField.getPath(ConfigurationField.CONFIG_MENU, ConfigurationField.TEXT_CHANNEL_ID),
                        message.getTextChannel().getId());
        this.plugin.getConfiguration().getFile()
                .set(ConfigurationField.getPath(ConfigurationField.CONFIG_MENU, ConfigurationField.MESSAGE_ID),
                        message.getId());
        this.plugin.getConfiguration().saveFile();
    }

    public void clearConfig() {
        this.plugin.getConfiguration().getFile().set(ConfigurationField.CONFIG_MENU.toString(), null);
        this.plugin.getConfiguration().saveFile();
    }
}
