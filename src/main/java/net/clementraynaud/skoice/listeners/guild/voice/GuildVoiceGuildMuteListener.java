/*
 * Copyright 2020, 2021, 2022 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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
import net.clementraynaud.skoice.storage.TempFileStorage;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildVoiceGuildMuteListener extends ListenerAdapter {

    private final Skoice plugin;

    public GuildVoiceGuildMuteListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onGuildVoiceGuildMute(GuildVoiceGuildMuteEvent event) {
        if (event.getMember().getVoiceState() != null
                && !(event.getMember().getVoiceState().getChannel() instanceof VoiceChannel)) {
            return;
        }
        VoiceChannel voiceChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();
        if (this.plugin.getConfiguration().getVoiceChannel().equals(voiceChannel) && !event.isGuildMuted()
                && this.plugin.getTempFileStorage().getStringList(TempFileStorage.MUTED_USERS_ID_FIELD).contains(event.getMember().getId())) {
            event.getMember().mute(true).queue();
        }
    }
}
