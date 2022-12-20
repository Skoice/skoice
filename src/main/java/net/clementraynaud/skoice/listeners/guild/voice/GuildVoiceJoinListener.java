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
import net.clementraynaud.skoice.tasks.UpdateVoiceStateTask;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildVoiceJoinListener extends ListenerAdapter {

    private final Skoice plugin;

    public GuildVoiceJoinListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if (event.getChannelJoined().getType() != ChannelType.VOICE) {
            return;
        }
        VoiceChannel voiceChannel = (VoiceChannel) event.getChannelJoined();
        new UpdateVoiceStateTask(this.plugin.getConfigYamlFile(), this.plugin.getTempYamlFile(), event.getMember(), voiceChannel).run();
        if (event.getChannelJoined().equals(this.plugin.getConfigYamlFile().getVoiceChannel())) {
            this.plugin.getBot().checkMemberStatus(event.getMember());
        }
    }
}
