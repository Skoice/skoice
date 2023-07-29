/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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
import net.clementraynaud.skoice.storage.TempYamlFile;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.function.Consumer;

public class ConfigurationMenu {

    private final Skoice plugin;

    public ConfigurationMenu(Skoice plugin) {
        this.plugin = plugin;
    }

    public MessageEditData update() {
        String menuId;
        switch (this.plugin.getBot().getStatus()) {
            case MULTIPLE_GUILDS:
                menuId = "server";
                break;
            case MISSING_PERMISSION:
                menuId = "permissions";
                break;
            case NO_VOICE_CHANNEL:
                menuId = "voice-channel";
                break;
            case NO_RADIUS:
                menuId = "range";
                break;
            default:
                menuId = "settings";
        }
        return MessageEditData.fromCreateData(this.plugin.getBot().getMenu(menuId).build());
    }

    public String getMessageId() {
        String messageId = this.plugin.getTempYamlFile()
                .getString(TempYamlFile.CONFIG_MENU_FIELD + "." + TempYamlFile.MESSAGE_ID_FIELD);
        if (messageId != null) {
            return messageId;
        }
        return "";
    }

    public void retrieveMessage(Consumer<Message> success) {
        String guildId = this.plugin.getTempYamlFile()
                .getString(TempYamlFile.CONFIG_MENU_FIELD + "." + TempYamlFile.GUILD_ID_FIELD);
        String channelId = this.plugin.getTempYamlFile()
                .getString(TempYamlFile.CONFIG_MENU_FIELD + "." + TempYamlFile.CHANNEL_ID_FIELD);
        String messageId = this.plugin.getTempYamlFile()
                .getString(TempYamlFile.CONFIG_MENU_FIELD + "." + TempYamlFile.MESSAGE_ID_FIELD);
        if (guildId == null || channelId == null || messageId == null) {
            return;
        }

        Guild guild = this.plugin.getBot().getJDA().getGuildById(guildId);
        if (guild == null) {
            return;
        }

        GuildMessageChannel channel = guild.getChannelById(GuildMessageChannel.class, channelId);
        if (channel == null) {
            return;
        }

        channel.retrieveMessageById(messageId).queue(success,
                new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, e -> this.clearConfig()));
    }

    public void delete() {
        this.retrieveMessage(message -> message.delete().queue());
    }

    public void store(Message message) {
        this.plugin.getTempYamlFile()
                .set(TempYamlFile.CONFIG_MENU_FIELD + "." + TempYamlFile.GUILD_ID_FIELD, message.getGuild().getId());
        this.plugin.getTempYamlFile()
                .set(TempYamlFile.CONFIG_MENU_FIELD + "." + TempYamlFile.CHANNEL_ID_FIELD, message.getGuildChannel().getId());
        this.plugin.getTempYamlFile()
                .set(TempYamlFile.CONFIG_MENU_FIELD + "." + TempYamlFile.MESSAGE_ID_FIELD, message.getId());
    }

    public void clearConfig() {
        this.plugin.getTempYamlFile().remove(TempYamlFile.CONFIG_MENU_FIELD);
    }
}
