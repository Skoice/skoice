/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.system;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.clementraynaud.skoice.util.DistanceUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class LinkedPlayer {

    private static final double FALLOFF = 2.5;

    private static final Set<LinkedPlayer> onlineLinkedPlayers = new HashSet<>();

    private final Skoice plugin;
    private final Player player;
    private final Member member;

    public LinkedPlayer(Skoice plugin, Player player, Member member) {
        this.plugin = plugin;
        this.player = player;
        this.member = member;
    }

    public static Set<LinkedPlayer> getOnlineLinkedPlayers() {
        return LinkedPlayer.onlineLinkedPlayers;
    }

    public boolean isStateEligible() {
        return (this.plugin.getConfigYamlFile().getBoolean(ConfigField.CORPSES_INCLUDED.toString()) || !this.player.isDead())
                && (this.plugin.getConfigYamlFile().getBoolean(ConfigField.SPECTATORS_INCLUDED.toString()) || this.player.getGameMode() != GameMode.SPECTATOR);
    }

    public void sendActionBarAlert() {
        Networks.getAll().stream()
                .filter(network -> network.contains(this))
                .filter(network -> network.canPlayerStayConnected(this))
                .filter(network -> !network.canPlayerBeAdded(this))
                .forEach(network -> this.plugin.adventure().player(this.player).sendActionBar(
                                Component.text(ChatColor.translateAlternateColorCodes('&',
                                                this.plugin.getLang().getMessage("minecraft.action-bar.alert")
                                        )
                                )
                        )
                );
    }

    public Set<LinkedPlayer> getPlayersWithinRange() {
        return LinkedPlayer.getOnlineLinkedPlayers().stream()
                .filter(p -> !p.isInAnyNetwork())
                .filter(LinkedPlayer::isInMainVoiceChannel)
                .filter(p -> !p.equals(this))
                .filter(LinkedPlayer::isStateEligible)
                .filter(p -> p.isCloseEnoughToPlayer(this, false))
                .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet));
    }

    public void updateNearNetworks() {
        Networks.getAll().stream()
                .filter(network -> network.canPlayerBeAdded(this))
                .reduce((network1, network2) -> network1.size() > network2.size()
                        ? network1.engulf(network2)
                        : network2.engulf(network1))
                .filter(network -> !network.contains(this))
                .ifPresent(network -> network.add(this));
        Networks.getAll().stream()
                .filter(network -> network.contains(this))
                .filter(network -> !network.canPlayerStayConnected(this))
                .forEach(network -> {
                    network.remove(this);
                    if (network.size() == 1) {
                        network.clear();
                    }
                });
    }

    private VoiceChannel getVoiceChannel() {
        if (this.member.getVoiceState() == null) {
            return null;
        }
        AudioChannelUnion audioChannel = this.member.getVoiceState().getChannel();
        if (audioChannel == null || audioChannel.getType() != ChannelType.VOICE) {
            return null;
        }
        return audioChannel.asVoiceChannel();
    }

    public boolean isInMainVoiceChannel() {
        VoiceChannel voiceChannel = this.getVoiceChannel();
        return voiceChannel != null && voiceChannel.equals(this.plugin.getConfigYamlFile().getVoiceChannel());
    }

    public boolean isInAnyNetwork() {
        return Networks.getAll().stream().anyMatch(network -> network.contains(this.getBukkitPlayer()));
    }

    public boolean isInAnyNetworkChannel() {
        VoiceChannel voiceChannel = this.getVoiceChannel();
        return voiceChannel != null && Networks.getInitialized().stream()
                .anyMatch(network -> network.getChannel().equals(voiceChannel));
    }

    public boolean isCloseEnoughToPlayer(LinkedPlayer linkedPlayer, boolean falloff) {
        if (this.player.getWorld().getName().equals(linkedPlayer.player.getWorld().getName())) {
            return false;
        }
        int horizontalRadius = this.plugin.getConfigYamlFile().getInt(ConfigField.HORIZONTAL_RADIUS.toString());
        int verticalRadius = this.plugin.getConfigYamlFile().getInt(ConfigField.VERTICAL_RADIUS.toString());
        if (falloff) {
            horizontalRadius += LinkedPlayer.FALLOFF;
            verticalRadius += LinkedPlayer.FALLOFF;
        }
        return DistanceUtil.getHorizontalDistance(this.player.getLocation(), linkedPlayer.player.getLocation()) <= horizontalRadius
                && DistanceUtil.getVerticalDistance(this.player.getLocation(), linkedPlayer.player.getLocation()) <= verticalRadius;
    }

    public Player getBukkitPlayer () {
        return this.player;
    }

    public Member getMember () {
        return this.member;
    }
}
