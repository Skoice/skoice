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

package net.clementraynaud.skoice.tasks;

import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.system.Network;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class UpdateVoiceStateTask implements Task {

    private final Member member;
    private final VoiceChannel channel;

    public UpdateVoiceStateTask(Member member, VoiceChannel channel) {
        this.member = member;
        this.channel = channel;
    }

    @Override
    public void run() {
        if (this.member.getVoiceState() == null || Config.getLobby() == null) {
            return;
        }
        boolean isLobby = this.channel.getId().equals(Config.getLobby().getId());
        if (isLobby && !this.member.getVoiceState().isGuildMuted()) {
            PermissionOverride override = this.channel.getPermissionOverride(this.channel.getGuild().getPublicRole());
            if (override != null && override.getDenied().contains(Permission.VOICE_SPEAK)
                    && this.member.hasPermission(this.channel, Permission.VOICE_SPEAK, Permission.VOICE_MUTE_OTHERS)
                    && this.channel.getGuild().getSelfMember().hasPermission(this.channel, Permission.VOICE_MUTE_OTHERS)
                    && this.channel.getGuild().getSelfMember().hasPermission(Config.getCategory(), Permission.VOICE_MOVE_OTHERS)) {
                this.member.mute(true).queue();
                Network.mutedUsers.add(this.member.getId());
            }
        } else if (!isLobby && Network.mutedUsers.remove(this.member.getId())) {
            this.member.mute(false).queue();
        }
    }
}
