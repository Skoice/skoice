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

package net.clementraynaud.skoice.common.bot;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.util.EnumSet;

public class BotVoiceChannel {

    private final Skoice plugin;

    public BotVoiceChannel(Skoice plugin) {
        this.plugin = plugin;
    }

    public void setup(VoiceChannel channel, User user) {
        VoiceChannel oldVoiceChannel = this.plugin.getConfigYamlFile().getVoiceChannel();
        this.plugin.getConfigYamlFile().set(ConfigField.VOICE_CHANNEL_ID.toString(), channel.getId());
        if (oldVoiceChannel != null) {
            oldVoiceChannel.modifyStatus("").queue();
        }

        if (this.plugin.getBot().getStatus() == BotStatus.READY) {
            this.plugin.getBot().getVoiceChannel().notifyUnlinkedUsers();
            this.plugin.getBot().getVoiceChannel().setStatus();
            this.plugin.getBot().getVoiceChannel().updatePermissions();
            this.plugin.getBot().updateVoiceState();
        } else {
            this.plugin.getListenerManager().update(user);
        }
    }

    public VoiceChannel getVoiceChannel() {
        return this.plugin.getConfigYamlFile().getVoiceChannel();
    }

    public void setStatus() {
        if (!this.plugin.getBot().isAdministrator()) {
            return;
        }
        VoiceChannel voiceChannel = this.getVoiceChannel();
        if (voiceChannel == null) {
            return;
        }
        voiceChannel.modifyStatus(this.plugin.getBot().getLang().getMessage("voice-channel-status")).queue();
    }

    public void updatePermissions() {
        if (!this.plugin.getBot().isAdministrator()) {
            return;
        }
        VoiceChannel voiceChannel = this.getVoiceChannel();
        if (voiceChannel == null) {
            return;
        }
        voiceChannel.getRolePermissionOverrides().forEach(override -> {
            EnumSet<Permission> deniedPermissions = override.getDenied();
            if (!deniedPermissions.contains(Permission.VOICE_SPEAK)) {
                override.getManager().deny(Permission.VOICE_SPEAK).queue();
            }
            if (!deniedPermissions.contains(Permission.VOICE_SET_STATUS)) {
                override.getManager().deny(Permission.VOICE_SET_STATUS).queue();
            }
        });
        Role publicRole = voiceChannel.getGuild().getPublicRole();
        PermissionOverride permissionOverride = voiceChannel.getPermissionOverride(publicRole);
        if (permissionOverride == null) {
            voiceChannel.upsertPermissionOverride(publicRole)
                    .deny(Permission.VOICE_SPEAK, Permission.VOICE_SET_STATUS)
                    .queue();
        }
    }

    public void notifyUnlinkedUsers() {
        if (this.plugin.getBot().getStatus() != BotStatus.READY) {
            return;
        }
        VoiceChannel voiceChannel = this.getVoiceChannel();
        if (voiceChannel == null) {
            return;
        }
        for (Member member : voiceChannel.getMembers()) {
            this.plugin.getBot().notifyIfUnlinked(member);
        }
    }
}
