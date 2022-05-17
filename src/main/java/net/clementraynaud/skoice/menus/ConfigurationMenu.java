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

import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.lang.Lang;
import net.clementraynaud.skoice.listeners.interaction.ButtonClickListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class ConfigurationMenu {

    private final Config config;
    private final Lang lang;
    private final Bot bot;

    public ConfigurationMenu(Config config, Lang lang, Bot bot) {
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

    public boolean exists() {
        return this.config.getFile().contains(ConfigField.CONFIG_MENU.get());
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
        Guild guild = this.bot.getJda().getGuildById(this.config.getFile()
                .getString(ConfigField.CONFIG_MENU.get() + "." + ConfigField.GUILD_ID.get()));
        if (guild == null) {
            return null;
        }
        TextChannel textChannel = guild.getTextChannelById(this.config.getFile()
                .getString(ConfigField.CONFIG_MENU.get() + "." + ConfigField.TEXT_CHANNEL_ID.get()));
        if (textChannel == null) {
            return null;
        }
        try {
            return textChannel.retrieveMessageById(this.config.getFile()
                    .getString(ConfigField.CONFIG_MENU.get() + "." + ConfigField.MESSAGE_ID.get())).complete();
        } catch (ErrorResponseException e) {
            this.clearConfig();
            ButtonClickListener.discordIDAxis.clear();
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
        this.config.getFile().set(ConfigField.CONFIG_MENU.get() + "." + ConfigField.GUILD_ID.get(), message.getGuild().getId());
        this.config.getFile().set(ConfigField.CONFIG_MENU.get() + "." + ConfigField.TEXT_CHANNEL_ID.get(), message.getTextChannel().getId());
        this.config.getFile().set(ConfigField.CONFIG_MENU.get() + "." + ConfigField.MESSAGE_ID.get(), message.getId());
        this.config.saveFile();
    }

    public void clearConfig() {
        this.config.getFile().set(ConfigField.CONFIG_MENU.get(), null);
        this.config.saveFile();
    }

    /*public void sendLobbyDeletedAlert(Guild guild) {
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
    }*/
}
