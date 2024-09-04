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

package net.clementraynaud.skoice.tasks;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.storage.TempYamlFile;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UpdateVoiceStateTask {

    private static final Set<String> mutedUsers = ConcurrentHashMap.newKeySet();

    private final Skoice plugin;
    private final Member member;
    private final VoiceChannel channel;

    public UpdateVoiceStateTask(Skoice plugin, Member member, VoiceChannel channel) {
        this.plugin = plugin;
        this.member = member;
        this.channel = channel;
    }

    public static Set<String> getMutedUsers() {
        try {
            if (Bukkit.isPrimaryThread()) {
                new IllegalStateException("This method should not be called from the main thread.").printStackTrace();
            }
        } catch (NullPointerException ignored) {
        }
        return UpdateVoiceStateTask.mutedUsers;
    }

    public void run() {
        try {
            if (Bukkit.isPrimaryThread()) {
                new IllegalStateException("This method should not be called from the main thread.").printStackTrace();
            }
        } catch (NullPointerException ignored) {
        }
        if (this.member.getVoiceState() == null) {
            return;
        }

        VoiceChannel voiceChannel = this.plugin.getConfigYamlFile().getVoiceChannel();
        if (voiceChannel == null || voiceChannel.getParentCategory() == null) {
            return;
        }

        boolean isMainVoiceChannel = this.channel.getId().equals(this.plugin.getConfigYamlFile().getString(ConfigField.VOICE_CHANNEL_ID.toString()));
        if (isMainVoiceChannel) {
            if (!this.member.getVoiceState().isGuildMuted()
                    && this.member.hasPermission(this.channel, Permission.VOICE_SPEAK, Permission.VOICE_MUTE_OTHERS)
                    && this.plugin.getBot().isAdministrator()) {
                this.member.mute(true).queue(success -> {
                    UpdateVoiceStateTask.mutedUsers.add(this.member.getId());
                    this.plugin.getTempYamlFile().set(TempYamlFile.MUTED_USERS_ID_FIELD,
                            new ArrayList<>(UpdateVoiceStateTask.mutedUsers));
                });
            }
        } else {
            VoiceChannel afkChannel = this.plugin.getBot().getGuild().getAfkChannel();
            if (afkChannel != null && this.channel.getId().equals(afkChannel.getId())) {
                return;
            }
            if (UpdateVoiceStateTask.mutedUsers.contains(this.member.getId())
                    || this.member.hasPermission(Permission.VOICE_MUTE_OTHERS)) {
                this.member.mute(false).queue(success -> {
                    UpdateVoiceStateTask.mutedUsers.remove(this.member.getId());
                    this.plugin.getTempYamlFile().set(TempYamlFile.MUTED_USERS_ID_FIELD,
                            new ArrayList<>(UpdateVoiceStateTask.mutedUsers));
                });
            }
        }
    }
}
