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

package net.clementraynaud.skoice.listeners.channel.main;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.ChannelUnion;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.GenericChannelEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateParentEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class GenericChannelListener extends ListenerAdapter {

    private final Skoice plugin;

    public GenericChannelListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        ChannelUnion channel = event.getChannel();
        if (channel.getType() != ChannelType.VOICE) {
            return;
        }
        this.reloadVoiceChannelMenu(channel.asVoiceChannel());
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        ChannelUnion channel = event.getChannel();
        if (channel.getType() != ChannelType.VOICE) {
            return;
        }
        this.checkForValidVoiceChannel(event);
        this.reloadVoiceChannelMenu(channel.asVoiceChannel());
    }

    @Override
    public void onChannelUpdateName(ChannelUpdateNameEvent event) {
        ChannelUnion channel = event.getChannel();
        if (channel.getType() != ChannelType.VOICE) {
            return;
        }
        this.reloadVoiceChannelMenu(channel.asVoiceChannel());
    }

    @Override
    public void onChannelUpdateParent(ChannelUpdateParentEvent event) {
        ChannelUnion channel = event.getChannel();
        if (channel.getType() != ChannelType.VOICE) {
            return;
        }
        this.checkForValidVoiceChannel(event);
        if ("voice-channel".equals(this.plugin.getConfigurationMenu().getMenuId())
                && this.plugin.getBot().getStatus() == BotStatus.READY) {
            this.plugin.getConfigurationMenu().retrieveMessage(message ->
                    message.editMessage(MessageEditData.fromCreateData(this.plugin.getBot().getMenu("voice-channel").build())).queue());
        }
    }

    private void checkForValidVoiceChannel(GenericChannelEvent event) {
        if (event.getChannel().getId().equals(this.plugin.getConfigYamlFile().getString(ConfigField.VOICE_CHANNEL_ID.toString()))) {
            this.plugin.getConfigYamlFile().remove(ConfigField.VOICE_CHANNEL_ID.toString());
            this.plugin.getListenerManager().update();
            event.getGuild().retrieveAuditLogs().limit(1).type(ActionType.CHANNEL_DELETE).queue(auditLogEntries -> {
                User user = auditLogEntries.get(0).getUser();
                if (user != null && !user.isBot()) {
                    user.openPrivateChannel().queue(channel ->
                            channel.sendMessage(this.plugin.getBot().getMenu("incomplete-configuration-alternative-server-manager").build())
                                    .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER))
                    );
                }
            });
        }
    }

    private void reloadVoiceChannelMenu(VoiceChannel voiceChannel) {
        if (voiceChannel.getParentCategory() != null
                && "voice-channel".equals(this.plugin.getConfigurationMenu().getMenuId())
                && this.plugin.getBot().getStatus() == BotStatus.READY) {
            this.plugin.getConfigurationMenu().retrieveMessage(message ->
                    message.editMessage(MessageEditData.fromCreateData(this.plugin.getBot().getMenu("voice-channel").build())).queue());
        }
    }
}
