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

package net.clementraynaud.skoice.listeners.guild.voice;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.system.Networks;
import net.clementraynaud.skoice.tasks.UpdateVoiceStateTask;
import net.clementraynaud.skoice.util.MapUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.entity.Player;

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

        VoiceChannel mainVoiceChannel = this.plugin.getConfigYamlFile().getVoiceChannel();
        if (voiceChannel.equals(mainVoiceChannel) ||
                Networks.getInitialized().stream().anyMatch(network -> network.getChannel().equals(voiceChannel))) {
            this.plugin.getBot().notifyIfUnlinked(member);
        }
    }

    private void manageLeavingChannel(Member member, AudioChannelUnion audioChannel) {
        if (audioChannel.getType() != ChannelType.VOICE) {
            return;
        }

        VoiceChannel voiceChannel = audioChannel.asVoiceChannel();
        if (!voiceChannel.equals(this.plugin.getConfigYamlFile().getVoiceChannel())
                && Networks.getInitialized().stream().noneMatch(network -> network.getChannel().equals(voiceChannel))) {
            return;
        }

        String minecraftId = MapUtil.getKeyFromValue(this.plugin.getLinksYamlFile().getLinks(), member.getId());
        if (minecraftId == null) {
            return;
        }

        Player player = this.plugin.getServer().getPlayer(UUID.fromString(minecraftId));
        if (player != null) {
            Networks.getAll().stream()
                    .filter(network -> network.contains(player))
                    .forEach(network -> network.remove(player));
            player.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.player.disconnected"));
//            PlayerProximityDisconnectEvent event = new PlayerProximityDisconnectEvent(minecraftId, member.getId());
//            this.plugin.getServer().getPluginManager().callEvent(event);
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

        VoiceChannel mainVoiceChannel = this.plugin.getConfigYamlFile().getVoiceChannel();

        if (voiceChannelJoined.equals(mainVoiceChannel) && Networks.getInitialized().stream().noneMatch(network -> network.getChannel().equals(voiceChannelLeft))
                || Networks.getInitialized().stream().anyMatch(network -> network.getChannel().equals(voiceChannelJoined)) && !voiceChannelLeft.equals(mainVoiceChannel) && Networks.getInitialized().stream().noneMatch(network -> network.getChannel().equals(voiceChannelLeft))) {
            this.plugin.getBot().notifyIfUnlinked(member);
        }

        if (Networks.getInitialized().stream().noneMatch(network -> network.getChannel().equals(voiceChannelJoined))) {
            String minecraftId = MapUtil.getKeyFromValue(this.plugin.getLinksYamlFile().getLinks(), member.getId());
            if (minecraftId == null) {
                return;
            }

            Player player = this.plugin.getServer().getPlayer(UUID.fromString(minecraftId));
            if (player != null) {
                if (Networks.getInitialized().stream().anyMatch(network -> network.getChannel().equals(voiceChannelLeft))) {
                    Networks.getAll().stream()
                            .filter(network -> network.contains(player))
                            .forEach(network -> network.remove(player));

                    if (!voiceChannelJoined.equals(mainVoiceChannel)) {
                        player.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.player.disconnected"));
//                        PlayerProximityDisconnectEvent event = new PlayerProximityDisconnectEvent(minecraftId, member.getId());
//                        this.plugin.getServer().getPluginManager().callEvent(event);
                    }

                } else if (voiceChannelLeft.equals(mainVoiceChannel)) {
                    player.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.player.disconnected"));
//                    PlayerProximityDisconnectEvent event = new PlayerProximityDisconnectEvent(minecraftId, member.getId());
//                    this.plugin.getServer().getPluginManager().callEvent(event);
                }
            }
        }
    }
}
