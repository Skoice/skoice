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
import net.clementraynaud.skoice.common.api.events.player.PlayerProximityDisconnectEvent;
import net.clementraynaud.skoice.common.model.minecraft.BasePlayer;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.clementraynaud.skoice.common.system.Networks;
import net.clementraynaud.skoice.common.system.ProximityChannels;
import net.clementraynaud.skoice.common.tasks.UpdateVoiceStateTask;
import net.clementraynaud.skoice.common.util.MapUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.UUID;

public class GuildVoiceUpdateListener extends ListenerAdapter {

    private final Skoice plugin;

    public GuildVoiceUpdateListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        if (event.getChannelLeft() == null && event.getChannelJoined() != null) {
            this.manageJoiningChannel(event.getMember(), event.getChannelJoined());

        } else if (event.getChannelJoined() == null) {
            this.manageLeavingChannel(event.getMember(), event.getChannelLeft());

        } else {
            this.manageMovingToChannel(event.getMember(), event.getChannelLeft(), event.getChannelJoined());
        }
    }

    private void manageJoiningChannel(Member member, AudioChannelUnion audioChannel) {
        if (audioChannel.getType() != ChannelType.VOICE) {
            return;
        }
        VoiceChannel voiceChannel = audioChannel.asVoiceChannel();
        new UpdateVoiceStateTask(this.plugin, member, voiceChannel).run();

        if (voiceChannel.getId().equals(this.plugin.getConfigYamlFile().getString(ConfigField.VOICE_CHANNEL_ID.toString()))
                || ProximityChannels.getInitialized().stream().anyMatch(proximityChannel -> proximityChannel.getChannelId().equals(voiceChannel.getId()))) {
            this.plugin.getBot().notifyIfUnlinked(member);
        }
    }

    private void manageLeavingChannel(Member member, AudioChannelUnion audioChannel) {
        if (audioChannel.getType() != ChannelType.VOICE) {
            return;
        }

        VoiceChannel voiceChannel = audioChannel.asVoiceChannel();
        if (!voiceChannel.getId().equals(this.plugin.getConfigYamlFile().getString(ConfigField.VOICE_CHANNEL_ID.toString()))
                && ProximityChannels.getInitialized().stream().noneMatch(proximityChannel -> proximityChannel.getChannelId().equals(voiceChannel.getId()))) {
            return;
        }

        String minecraftId = MapUtil.getKeyFromValue(this.plugin.getLinksYamlFile().getLinks(), member.getId());
        if (minecraftId == null) {
            return;
        }

        BasePlayer player = this.plugin.getPlayer(UUID.fromString(minecraftId));
        if (player != null) {
            Networks.getAll().forEach(network -> network.remove(player));
            player.sendMessage(this.plugin.getLang().getMessage("chat.player.disconnected"));
            Skoice.eventBus().fireAsync(new PlayerProximityDisconnectEvent(minecraftId));
        }
    }

    private void manageMovingToChannel(Member member, AudioChannelUnion audioChannelLeft, AudioChannelUnion audioChannelJoined) {
        if (audioChannelJoined.getType() != ChannelType.VOICE) {
            return;
        }

        VoiceChannel voiceChannelJoined = audioChannelJoined.asVoiceChannel();
        new UpdateVoiceStateTask(this.plugin, member, voiceChannelJoined).run();

        if (audioChannelLeft.getType() != ChannelType.VOICE) {
            return;
        }
        VoiceChannel voiceChannelLeft = audioChannelLeft.asVoiceChannel();

        String mainVoiceChannelId = this.plugin.getConfigYamlFile().getString(ConfigField.VOICE_CHANNEL_ID.toString());

        if (voiceChannelJoined.getId().equals(mainVoiceChannelId)
                && ProximityChannels.getInitialized().stream().noneMatch(proximityChannel -> proximityChannel.getChannelId().equals(voiceChannelLeft.getId()))
                || ProximityChannels.getInitialized().stream().anyMatch(proximityChannel -> proximityChannel.getChannelId().equals(voiceChannelJoined.getId()))
                && !voiceChannelLeft.getId().equals(mainVoiceChannelId)
                && ProximityChannels.getInitialized().stream().noneMatch(proximityChannel -> proximityChannel.getChannelId().equals(voiceChannelLeft.getId()))) {
            this.plugin.getBot().notifyIfUnlinked(member);
        }

        if (!ProximityChannels.isProximityChannel(voiceChannelJoined.getId())) {
            String minecraftId = MapUtil.getKeyFromValue(this.plugin.getLinksYamlFile().getLinks(), member.getId());
            if (minecraftId == null) {
                return;
            }

            BasePlayer player = this.plugin.getPlayer(UUID.fromString(minecraftId));
            if (player == null) {
                return;
            }

            if (ProximityChannels.isProximityChannel(voiceChannelLeft.getId())) {
                Networks.getAll().forEach(network -> network.remove(player));

                if (!voiceChannelJoined.getId().equals(mainVoiceChannelId)) {
                    player.sendMessage(this.plugin.getLang().getMessage("chat.player.disconnected"));
                    Skoice.eventBus().fireAsync(new PlayerProximityDisconnectEvent(minecraftId));
                }

            } else if (voiceChannelLeft.getId().equals(mainVoiceChannelId)) {
                player.sendMessage(this.plugin.getLang().getMessage("chat.player.disconnected"));
                Skoice.eventBus().fireAsync(new PlayerProximityDisconnectEvent(minecraftId));
            }
        }
    }
}
