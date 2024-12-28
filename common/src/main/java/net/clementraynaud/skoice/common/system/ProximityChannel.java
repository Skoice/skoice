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

package net.clementraynaud.skoice.common.system;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.storage.TempYamlFile;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.Collections;
import java.util.EnumSet;
import java.util.stream.Collectors;

public class ProximityChannel {

    private final Skoice plugin;
    private boolean initialized = false;
    private String channelId;

    public ProximityChannel(Skoice plugin, Network network) {
        this.plugin = plugin;

        Guild guild = this.plugin.getBot().getGuild();

        EnumSet<Permission> deniedPermissions = EnumSet.of(
                this.plugin.getConfigYamlFile().getBoolean(ConfigField.CHANNEL_VISIBILITY.toString())
                        ? Permission.VOICE_CONNECT
                        : Permission.VIEW_CHANNEL,
                Permission.VOICE_MOVE_OTHERS
        );
        if (!this.plugin.getConfigYamlFile().getBoolean(ConfigField.TEXT_CHAT.toString())) {
            deniedPermissions.add(Permission.MESSAGE_SEND);
        }

        this.plugin.getConfigYamlFile().getCategory()
                .createVoiceChannel(this.plugin.getBot().getLang().getMessage("proximity-channel-name"))
                .addPermissionOverride(guild.getPublicRole(),
                        EnumSet.of(Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD),
                        deniedPermissions)
                .addPermissionOverride(guild.getSelfMember(),
                        EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_MOVE_OTHERS),
                        Collections.emptyList())
                .queue(voiceChannel -> {
                    this.channelId = voiceChannel.getId();
                    ProximityChannels.add(this);
                    this.initialized = true;
                    this.plugin.getTempYamlFile().set(TempYamlFile.VOICE_CHANNELS_ID_FIELD,
                            ProximityChannels.getInitialized().stream()
                                    .map(ProximityChannel::getChannelId)
                                    .collect(Collectors.toList()));
                }, e -> Networks.remove(network));
    }

    public ProximityChannel(Skoice plugin, String channelId) {
        this.plugin = plugin;
        this.channelId = channelId;
        this.initialized = true;
        ProximityChannels.add(this);
    }

    public void delete() {
        VoiceChannel channel = this.getChannel();
        if (channel != null) {
            channel.delete().queue(null, new ErrorHandler().handle(ErrorResponse.UNKNOWN_CHANNEL, e ->
                    ProximityChannels.remove(this)
            ));
        } else {
            ProximityChannels.remove(this);
        }
    }

    public String getChannelId() {
        return this.channelId;
    }

    public VoiceChannel getChannel() {
        if (this.channelId == null) {
            return null;
        }
        Guild guild = this.plugin.getBot().getGuild();
        if (guild != null) {
            return guild.getVoiceChannelById(this.channelId);
        }
        return null;
    }

    public boolean isInitialized() {
        return this.initialized;
    }
}
