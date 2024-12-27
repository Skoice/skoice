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

package net.clementraynaud.skoice.common.listeners.channel.main;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.commands.CommandInfo;
import net.clementraynaud.skoice.common.menus.ConfigurationMenu;
import net.clementraynaud.skoice.common.menus.ConfigurationMenus;
import net.clementraynaud.skoice.common.menus.EmbeddedMenu;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
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
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateVoiceStatusEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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
        if (channel.asVoiceChannel().getParentCategory() != null) {
            this.reloadVoiceChannelMenu();
        }
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        ChannelUnion channel = event.getChannel();
        if (channel.getType() != ChannelType.VOICE) {
            return;
        }
        this.checkForValidVoiceChannel(event);
        if (channel.asVoiceChannel().getParentCategory() != null) {
            this.reloadVoiceChannelMenu();
        }
    }

    @Override
    public void onChannelUpdateName(ChannelUpdateNameEvent event) {
        ChannelUnion channel = event.getChannel();
        if (channel.getType() != ChannelType.VOICE) {
            return;
        }
        if (channel.asVoiceChannel().getParentCategory() != null) {
            this.reloadVoiceChannelMenu();
        }
    }

    @Override
    public void onChannelUpdateParent(ChannelUpdateParentEvent event) {
        ChannelUnion channel = event.getChannel();
        if (channel.getType() != ChannelType.VOICE) {
            return;
        }

        if (this.plugin.getBot().isAdministrator()
                && channel.getId().equals(this.plugin.getConfigYamlFile().getString(ConfigField.VOICE_CHANNEL_ID.toString()))
                && channel.asVoiceChannel().getParentCategory() == null) {
            channel.asVoiceChannel().getManager()
                    .setParent(event.getOldValue())
                    .queue();
        }

        if (channel.asVoiceChannel().getParentCategory() != null) {
            this.reloadVoiceChannelMenu();
        }
    }

    @Override
    public void onChannelUpdateVoiceStatus(ChannelUpdateVoiceStatusEvent event) {
        VoiceChannel voiceChannel = this.plugin.getConfigYamlFile().getVoiceChannel();
        if (voiceChannel == null || voiceChannel != event.getChannel().asVoiceChannel()) {
            return;
        }
        this.plugin.getBot().getVoiceChannel().setStatus();
    }

    private void checkForValidVoiceChannel(GenericChannelEvent event) {
        if (!event.getChannel().getId().equals(this.plugin.getConfigYamlFile()
                .getString(ConfigField.VOICE_CHANNEL_ID.toString()))) {
            return;
        }

        this.plugin.getConfigYamlFile().remove(ConfigField.VOICE_CHANNEL_ID.toString());
        this.plugin.getListenerManager().update();

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
                            new EmbeddedMenu(this.plugin.getBot()).setContent("incomplete-configuration-alternative-server-manager",
                                            this.plugin.getBot().getCommands().getAsMention(CommandInfo.CONFIGURE.toString()))
                                    .message(user);
                        }
                    }
                });
    }

    private void reloadVoiceChannelMenu() {
        ConfigurationMenus.getMenuSet().stream()
                .filter(menu -> "voice-channel".equals(menu.getId()))
                .forEach(ConfigurationMenu::editFromHook);
    }
}
