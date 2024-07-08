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

package net.clementraynaud.skoice.system;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.storage.TempYamlFile;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Network {

    private final Skoice plugin;
    private final Set<LinkedPlayer> players;
    private boolean initialized = false;
    private String channelId;

    public Network(Skoice plugin, String channelId) {
        this.plugin = plugin;
        this.players = new HashSet<>();
        this.channelId = channelId;
        Networks.add(this);
    }

    public Network(Skoice plugin, Set<LinkedPlayer> players) {
        this.plugin = plugin;
        this.players = players;
        Networks.add(this);
    }

    public void build() {
        if (this.channelId != null) {
            this.initialized = true;
            return;
        }
        Guild guild = this.plugin.getBot().getGuild();
        EnumSet<Permission> deniedPermissions = EnumSet.of(
                this.plugin.getConfigYamlFile().getBoolean(ConfigField.CHANNEL_VISIBILITY.toString())
                        ? Permission.VOICE_CONNECT
                        : Permission.VIEW_CHANNEL,
                Permission.VOICE_MOVE_OTHERS
        );
        this.plugin.getConfigYamlFile().getCategory()
                .createVoiceChannel(this.plugin.getBot().getLang().getMessage("proximity-channel-name"))
                .addPermissionOverride(guild.getPublicRole(),
                        EnumSet.of(Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD),
                        deniedPermissions)
                .addPermissionOverride(guild.getSelfMember(),
                        EnumSet.of(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_MOVE_OTHERS),
                        Collections.emptyList())
                .setBitrate(this.plugin.getConfigYamlFile().getVoiceChannel().getBitrate())
                .queue(voiceChannel -> {
                    this.channelId = voiceChannel.getId();
                    List<String> voiceChannels = this.plugin.getTempYamlFile()
                            .getStringList(TempYamlFile.VOICE_CHANNELS_ID_FIELD);
                    voiceChannels.add(this.channelId);
                    this.plugin.getTempYamlFile().set(TempYamlFile.VOICE_CHANNELS_ID_FIELD, voiceChannels);
                    this.initialized = true;
                }, e -> Networks.remove(this));
    }

    public boolean canPlayerConnect(LinkedPlayer player) {
        if (!player.isStateEligible()) {
            return false;
        }
        return this.players.stream()
                .filter(LinkedPlayer::isStateEligible)
                .filter(p -> p.isCloseEnoughToPlayer(player, false))
                .anyMatch(p -> !p.equals(player));
    }

    public boolean canPlayerStayConnected(LinkedPlayer player) {
        if (!player.isStateEligible()) {
            return false;
        }
        return this.players.stream()
                .filter(LinkedPlayer::isStateEligible)
                .filter(p -> p.isCloseEnoughToPlayer(player, true))
                .anyMatch(p -> !p.equals(player));
    }

    public void splitIfSpread() {
        if (this.size() < 4) {
            return;
        }

        Set<LinkedPlayer> playersWithinRange = this.getChainingPlayers(this.players.iterator().next());
        if (playersWithinRange.size() == 1 || playersWithinRange.size() + 1 >= this.size()) {
            return;
        }

        Set<LinkedPlayer> playersToExclude;
        if (this.size() / 2 >= playersWithinRange.size()) {
            playersToExclude = playersWithinRange;
        } else {
            playersToExclude = this.players.stream()
                    .filter(p -> !playersWithinRange.contains(p))
                    .collect(Collectors.toSet());
        }
        playersToExclude.forEach(this::remove);
        new Network(this.plugin, playersToExclude).build();
    }

    private Set<LinkedPlayer> getChainingPlayers(LinkedPlayer startingPoint) {
        return this.getChainingPlayers(new HashSet<>(Collections.singleton(startingPoint)), Collections.singleton(startingPoint));
    }

    private Set<LinkedPlayer> getChainingPlayers(Set<LinkedPlayer> chainingPlayers, Set<LinkedPlayer> children) {
        Set<LinkedPlayer> newChildren = new HashSet<>();
        children.forEach(p -> p.getPlayersWithinRange().stream()
                .filter(this::contains)
                .filter(playerWithingRange -> !chainingPlayers.contains(playerWithingRange))
                .forEach(newChildren::add)
        );

        if (newChildren.isEmpty()) {
            return chainingPlayers;
        }

        chainingPlayers.addAll(newChildren);
        return this.getChainingPlayers(chainingPlayers, newChildren);
    }

    public void engulf(Network network) {
        this.players.addAll(network.players);
        network.players.clear();
    }

    public void clear() {
        this.players.clear();
    }

    public void add(LinkedPlayer player) {
        this.players.add(player);
    }

    public void remove(LinkedPlayer player) {
        this.players.remove(player);
    }

    public void remove(Player player) {
        this.players.removeIf(p -> p.getBukkitPlayer().equals(player));
    }

    public boolean contains(LinkedPlayer player) {
        return this.players.contains(player);
    }

    public boolean contains(Player player) {
        return this.players.stream().anyMatch(p -> p.getBukkitPlayer().equals(player));
    }

    public void delete(String reason) {
        VoiceChannel channel = this.getChannel();
        if (channel != null) {
            channel.delete().reason(this.plugin.getBot().getLang().getMessage(reason)).queue();
        }
        List<String> voiceChannels = this.plugin.getTempYamlFile()
                .getStringList(TempYamlFile.VOICE_CHANNELS_ID_FIELD);
        voiceChannels.remove(this.channelId);
        this.plugin.getTempYamlFile().set(TempYamlFile.VOICE_CHANNELS_ID_FIELD, voiceChannels);
        Networks.remove(this);
    }

    public int size() {
        return this.players.size();
    }

    public boolean isEmpty() {
        return this.size() < 2;
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
