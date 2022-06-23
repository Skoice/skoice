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

import net.clementraynaud.skoice.config.Configuration;
import net.clementraynaud.skoice.storage.TempFileStorage;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.List;

public class UpdateVoiceStateTask implements Task {

    private final Configuration configuration;
    private final TempFileStorage tempDataFile;
    private final Member member;
    private final VoiceChannel channel;

    public UpdateVoiceStateTask(Configuration configuration, TempFileStorage tempDataFile, Member member, VoiceChannel channel) {
        this.configuration = configuration;
        this.tempDataFile = tempDataFile;
        this.member = member;
        this.channel = channel;
    }

    @Override
    public void run() {
        if (this.member.getVoiceState() == null || this.configuration.getLobby() == null) {
            return;
        }
        boolean isLobby = this.channel.getId().equals(this.configuration.getLobby().getId());
        if (isLobby && !this.member.getVoiceState().isGuildMuted()) {
            PermissionOverride override = this.channel.getPermissionOverride(this.channel.getGuild().getPublicRole());
            if (override != null && override.getDenied().contains(Permission.VOICE_SPEAK)
                    && this.member.hasPermission(this.channel, Permission.VOICE_SPEAK, Permission.VOICE_MUTE_OTHERS)
                    && this.channel.getGuild().getSelfMember().hasPermission(this.channel, Permission.VOICE_MUTE_OTHERS)
                    && this.channel.getGuild().getSelfMember().hasPermission(this.configuration.getCategory(), Permission.VOICE_MOVE_OTHERS)) {
                this.member.mute(true).queue();
                List<String> mutedUsers = this.tempDataFile.getFile().getStringList(TempFileStorage.MUTED_USERS_ID_FIELD);
                mutedUsers.add(this.member.getId());
                this.tempDataFile.getFile().set(TempFileStorage.MUTED_USERS_ID_FIELD, mutedUsers);
                this.tempDataFile.saveFile();
            }
        } else if (!isLobby) {
            List<String> mutedUsers = this.tempDataFile.getFile().getStringList(TempFileStorage.MUTED_USERS_ID_FIELD);
            if (mutedUsers.contains(this.member.getId())) {
                this.member.mute(false).queue();
                mutedUsers.remove(this.member.getId());
                this.tempDataFile.getFile().set(TempFileStorage.MUTED_USERS_ID_FIELD, mutedUsers);
                this.tempDataFile.saveFile();
            }
        }
    }
}
