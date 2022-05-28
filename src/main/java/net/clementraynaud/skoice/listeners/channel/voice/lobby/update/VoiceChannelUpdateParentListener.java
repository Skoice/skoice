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

package net.clementraynaud.skoice.listeners.channel.voice.lobby.update;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.config.ConfigurationField;
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.menus.MenuType;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdateParentEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.Collections;

public class VoiceChannelUpdateParentListener extends ListenerAdapter {

    private final Skoice plugin;

    public VoiceChannelUpdateParentListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onVoiceChannelUpdateParent(VoiceChannelUpdateParentEvent event) {
        if (event.getChannel().getId().equals(this.plugin.getConfiguration().getFile().getString(ConfigurationField.LOBBY_ID.toString()))) {
            this.plugin.getConfiguration().getFile().set(ConfigurationField.LOBBY_ID.toString(), null);
            this.plugin.updateStatus(false);
            User user = event.getGuild().retrieveAuditLogs().limit(1).type(ActionType.CHANNEL_DELETE).complete().get(0).getUser();
            if (user != null && !user.isBot()) {
                user.openPrivateChannel().complete()
                        .sendMessage(new Menu(this.plugin, "empty-configuration",
                                Collections.singleton(this.plugin.getBot().getFields().get("incomplete-configuration-alternative-server-manager")),
                                MenuType.ERROR)
                                .toMessage())
                        .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
            }
        }
    }
}
