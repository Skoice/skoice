/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
 * Copyright 2016, 2017, 2018, 2019, 2020, 2021 Austin "Scarsz" Shapiro
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

package net.clementraynaud.skoice.listeners.guild.voice;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.storage.TempYamlFile;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

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
        if (this.plugin.getConfigYamlFile().getVoiceChannel().equals(audioChannel) && !event.isGuildMuted()) {
            if (event.getMember().hasPermission(Permission.VOICE_MUTE_OTHERS)) {
                event.getMember().mute(true).queue();
                List<String> mutedUsers = this.plugin.getTempYamlFile().getStringList(TempYamlFile.MUTED_USERS_ID_FIELD);
                if (!mutedUsers.contains(event.getMember().getId())) {
                    mutedUsers.add(event.getMember().getId());
                    this.plugin.getTempYamlFile().set(TempYamlFile.MUTED_USERS_ID_FIELD, mutedUsers);
                }
            } else if (this.plugin.getTempYamlFile().getStringList(TempYamlFile.MUTED_USERS_ID_FIELD).contains(event.getMember().getId())) {
                event.getMember().mute(true).queue();
            }
        }
    }
}
