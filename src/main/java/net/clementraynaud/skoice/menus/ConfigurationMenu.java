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

package net.clementraynaud.skoice.menus;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.storage.TempYamlFile;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Consumer;

public class ConfigurationMenu {

    private final Skoice plugin;
    private String menuId;

    public ConfigurationMenu(Skoice plugin) {
        this.plugin = plugin;
    }

    public MessageEditData update() {
        switch (this.plugin.getBot().getStatus()) {
            case MULTIPLE_GUILDS:
                this.menuId = "server";
                break;
            case MISSING_PERMISSION:
                this.menuId = "permissions";
                break;
            case NO_VOICE_CHANNEL:
                this.menuId = "voice-channel";
                break;
            case NO_RADIUS:
                this.menuId = "range";
                break;
            default:
                this.menuId = "settings";
        }
        return MessageEditData.fromCreateData(this.plugin.getBot().getMenu(this.menuId).build());
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
        String channelTypeString = this.plugin.getTempYamlFile()
                .getString(TempYamlFile.CONFIG_MENU_FIELD + "." + TempYamlFile.CHANNEL_TYPE_FIELD);
        if (Arrays.stream(ChannelType.values()).noneMatch(value -> value.toString().equalsIgnoreCase(channelTypeString))) {
            return;
        }
        ChannelType channelType = ChannelType.valueOf(this.plugin.getTempYamlFile()
                .getString(TempYamlFile.CONFIG_MENU_FIELD + "." + TempYamlFile.CHANNEL_TYPE_FIELD));

        String channelId = this.plugin.getTempYamlFile()
                .getString(TempYamlFile.CONFIG_MENU_FIELD + "." + TempYamlFile.CHANNEL_ID_FIELD);
        String messageId = this.plugin.getTempYamlFile()
                .getString(TempYamlFile.CONFIG_MENU_FIELD + "." + TempYamlFile.MESSAGE_ID_FIELD);
        if (channelId == null || messageId == null) {
            return;
        }

        MessageChannel channel;
        if (channelType.isGuild()) {
            channel = this.plugin.getBot().getJDA().getChannelById(GuildMessageChannel.class, channelId);
        } else {
            channel = this.plugin.getBot().getJDA().getPrivateChannelById(channelId);
        }
        if (channel == null) {
            return;
        }

        channel.retrieveMessageById(messageId).queue(
                success,
                new ErrorHandler().handle(
                        EnumSet.of(ErrorResponse.UNKNOWN_MESSAGE, ErrorResponse.MISSING_ACCESS),
                        e -> this.clearConfig()
                )
        );
    }

    public void delete() {
        this.retrieveMessage(message -> message.delete().queue());
    }

    public void store(Message message) {
        this.plugin.getTempYamlFile()
                .set(TempYamlFile.CONFIG_MENU_FIELD + "." + TempYamlFile.CHANNEL_TYPE_FIELD, message.getChannelType().toString());
        this.plugin.getTempYamlFile()
                .set(TempYamlFile.CONFIG_MENU_FIELD + "." + TempYamlFile.CHANNEL_ID_FIELD, message.getChannelId());
        this.plugin.getTempYamlFile()
                .set(TempYamlFile.CONFIG_MENU_FIELD + "." + TempYamlFile.MESSAGE_ID_FIELD, message.getId());
    }

    public void clearConfig() {
        this.plugin.getTempYamlFile().remove(TempYamlFile.CONFIG_MENU_FIELD);
    }

    public String getMenuId() {
        return this.menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }
}
