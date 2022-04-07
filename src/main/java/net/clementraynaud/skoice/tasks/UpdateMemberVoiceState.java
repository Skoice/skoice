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

import net.clementraynaud.skoice.system.Network;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.VoiceChannel;

import static net.clementraynaud.skoice.config.Config.getCategory;
import static net.clementraynaud.skoice.config.Config.getLobby;

public class UpdateMemberVoiceState implements Task {

    private final VoiceChannel channel;
    private final Member member;

    public UpdateMemberVoiceState(VoiceChannel channel, Member member) {
        this.channel = channel;
        this.member = member;
    }

    @Override
    public void run(){
        if (member.getVoiceState() == null || getLobby() == null) {
            return;
        }
        boolean isLobby = channel.getId().equals(getLobby().getId());
        if (isLobby && !member.getVoiceState().isGuildMuted()) {
            PermissionOverride override = channel.getPermissionOverride(channel.getGuild().getPublicRole());
            if (override != null && override.getDenied().contains(Permission.VOICE_SPEAK)
                    && member.hasPermission(channel, Permission.VOICE_SPEAK, Permission.VOICE_MUTE_OTHERS)
                    && channel.getGuild().getSelfMember().hasPermission(channel, Permission.VOICE_MUTE_OTHERS)
                    && channel.getGuild().getSelfMember().hasPermission(getCategory(), Permission.VOICE_MOVE_OTHERS)) {
                member.mute(true).queue();
                Network.mutedUsers.add(member.getId());
            }
        } else if (!isLobby && Network.mutedUsers.remove(member.getId())) {
            member.mute(false).queue();
        }
    }
}
