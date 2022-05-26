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

package net.clementraynaud.skoice.listeners.channel.voice.lobby;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.menus.MenuType;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.Collections;

public class VoiceChannelDeleteListener extends ListenerAdapter {

    private final Skoice plugin;

    public VoiceChannelDeleteListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
        if (event.getChannel().getId().equals(this.plugin.readConfig().getFile().getString(ConfigField.LOBBY_ID.get()))) {
            this.plugin.readConfig().getFile().set(ConfigField.LOBBY_ID.get(), null);
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
