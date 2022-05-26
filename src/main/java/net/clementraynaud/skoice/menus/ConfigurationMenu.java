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
import net.clementraynaud.skoice.config.ConfigField;
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
            return this.plugin.getBot().getMenus().get("server").toMessage();
        } else if (!this.plugin.getBot().getJda().getGuilds().get(0).getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
            return this.plugin.getBot().getMenus().get("permissions").toMessage();
        } else if (!this.plugin.readConfig().getFile().contains(ConfigField.LOBBY_ID.get())) {
            return this.plugin.getBot().getMenus().get("lobby").toMessage();
        } else if (!this.plugin.readConfig().getFile().contains(ConfigField.HORIZONTAL_RADIUS.get())
                || !this.plugin.readConfig().getFile().contains(ConfigField.VERTICAL_RADIUS.get())) {
            return this.plugin.getBot().getMenus().get("mode").toMessage();
        } else {
            return this.plugin.getBot().getMenus().get("configuration").toMessage();
        }
    }

    public boolean exists() {
        return this.plugin.readConfig().getFile().contains(ConfigField.CONFIG_MENU.get());
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
        Guild guild = this.plugin.getBot().getJda().getGuildById(this.plugin.readConfig().getFile()
                .getString(ConfigField.CONFIG_MENU.get() + "." + ConfigField.GUILD_ID.get()));
        if (guild == null) {
            return null;
        }
        TextChannel textChannel = guild.getTextChannelById(this.plugin.readConfig().getFile()
                .getString(ConfigField.CONFIG_MENU.get() + "." + ConfigField.TEXT_CHANNEL_ID.get()));
        if (textChannel == null) {
            return null;
        }
        try {
            return textChannel.retrieveMessageById(this.plugin.readConfig().getFile()
                    .getString(ConfigField.CONFIG_MENU.get() + "." + ConfigField.MESSAGE_ID.get())).complete();
        } catch (ErrorResponseException e) {
            this.clearConfig();
            ButtonClickListener.getDiscordIDAxis().clear();
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
        this.plugin.readConfig().getFile().set(ConfigField.CONFIG_MENU.get() + "." + ConfigField.GUILD_ID.get(), message.getGuild().getId());
        this.plugin.readConfig().getFile().set(ConfigField.CONFIG_MENU.get() + "." + ConfigField.TEXT_CHANNEL_ID.get(), message.getTextChannel().getId());
        this.plugin.readConfig().getFile().set(ConfigField.CONFIG_MENU.get() + "." + ConfigField.MESSAGE_ID.get(), message.getId());
        this.plugin.readConfig().saveFile();
    }

    public void clearConfig() {
        this.plugin.readConfig().getFile().set(ConfigField.CONFIG_MENU.get(), null);
        this.plugin.readConfig().saveFile();
    }
}
