/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.common.listeners.channel.network;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.menus.EmbeddedMenu;
import net.clementraynaud.skoice.common.system.Networks;
import net.clementraynaud.skoice.common.system.ProximityChannels;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.ChannelUnion;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateParentEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GenericChannelListener extends ListenerAdapter {

    private final Skoice plugin;

    public GenericChannelListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        ChannelUnion channel = event.getChannel();
        if (channel.getType() != ChannelType.VOICE
                || !ProximityChannels.isProximityChannel(channel.getId())) {
            return;
        }

        Networks.getInitialized().stream()
                .filter(network -> event.getChannel().getId().equals(network.getProximityChannel().getChannelId()))
                .forEach(Networks::remove);

        ProximityChannels.remove(channel.getId());

        if (!this.plugin.getBot().isAdministrator()) {
            return;
        }
        event.getGuild().retrieveAuditLogs()
                .type(ActionType.CHANNEL_DELETE)
                .limit(1)
                .queue(auditLogEntries -> {
                    if (!auditLogEntries.isEmpty()) {
                        User user = auditLogEntries.get(0).getUser();
                        if (user != null && !user.isBot()) {
                            new EmbeddedMenu(this.plugin.getBot()).setContent("proximity-channel-deleted")
                                    .message(user);
                        }
                    }
                });
    }

    @Override
    public void onChannelUpdateParent(ChannelUpdateParentEvent event) {
        ChannelUnion channel = event.getChannel();

        if (channel.getType() == ChannelType.VOICE
                && ProximityChannels.isProximityChannel(channel.getId())
                && (event.getNewValue() == null
                || !event.getNewValue().getId().equals(this.plugin.getConfigYamlFile().getCategory().getId()))) {
            event.getChannel().asVoiceChannel().getManager()
                    .setParent(event.getOldValue()).queue();
        }
    }

    @Override
    public void onChannelUpdateName(ChannelUpdateNameEvent event) {
        ChannelUnion channel = event.getChannel();
        String expectedName = this.plugin.getBot().getLang().getMessage("proximity-channel-name");

        if (channel.getType() == ChannelType.VOICE
                && ProximityChannels.isProximityChannel(channel.getId())
                && !event.getChannel().asVoiceChannel().getName().equals(expectedName)) {
            channel.asVoiceChannel().getManager().
                    setName(expectedName).queue();
        }
    }
}
