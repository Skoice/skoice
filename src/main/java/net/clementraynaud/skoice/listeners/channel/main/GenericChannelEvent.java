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

package net.clementraynaud.skoice.listeners.channel.main;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.config.ConfigurationField;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateParentEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class GenericChannelEvent extends ListenerAdapter {

    private final Skoice plugin;

    public GenericChannelEvent(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        this.checkForValidVoiceChannel(event);
    }

    @Override
    public void onChannelUpdateParent(ChannelUpdateParentEvent event) {
        this.checkForValidVoiceChannel(event);
    }

    private void checkForValidVoiceChannel(net.dv8tion.jda.api.events.channel.GenericChannelEvent event) {
        if (!event.getChannelType().isAudio()) {
            return;
        }
        if (event.getChannel().getId().equals(this.plugin.getConfiguration().getString(ConfigurationField.VOICE_CHANNEL_ID.toString()))) {
            this.plugin.getConfiguration().set(ConfigurationField.VOICE_CHANNEL_ID.toString(), null);
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
}
