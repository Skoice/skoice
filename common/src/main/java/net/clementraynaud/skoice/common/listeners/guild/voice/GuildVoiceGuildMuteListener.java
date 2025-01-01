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

package net.clementraynaud.skoice.common.listeners.guild.voice;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.clementraynaud.skoice.common.tasks.UpdateVoiceStateTask;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildVoiceGuildMuteListener extends ListenerAdapter {

    private final Skoice plugin;

    public GuildVoiceGuildMuteListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onGuildVoiceGuildMute(GuildVoiceGuildMuteEvent event) {
        if (event.getMember().getVoiceState() == null) {
            return;
        }
        AudioChannel audioChannel = event.getMember().getVoiceState().getChannel();
        if (audioChannel == null) {
            return;
        }

        if (event.isGuildMuted()
                || !audioChannel.getId().equals(this.plugin.getConfigYamlFile().getString(ConfigField.VOICE_CHANNEL_ID.toString()))) {
            return;
        }

        if (event.getMember().hasPermission(Permission.VOICE_MUTE_OTHERS)
                || UpdateVoiceStateTask.getMutedUsers().contains(event.getMember().getId())) {
            event.getMember().mute(true).queue();
        }
    }
}
