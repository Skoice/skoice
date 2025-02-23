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

package net.clementraynaud.skoice.common.system;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.model.minecraft.FullPlayer;
import net.clementraynaud.skoice.common.model.minecraft.SkoiceGameMode;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.clementraynaud.skoice.common.util.DistanceUtil;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class LinkedPlayer {

    private static final int FALLOFF = 3;

    private static final Set<LinkedPlayer> onlineLinkedPlayers = ConcurrentHashMap.newKeySet();
    private final EnumSet<ActionBarAlert> alerts = EnumSet.noneOf(ActionBarAlert.class);

    private final Skoice plugin;
    private final FullPlayer player;
    private final String discordId;

    public LinkedPlayer(Skoice plugin, FullPlayer player, String discordId) {
        this.plugin = plugin;
        this.player = player;
        this.discordId = discordId;
        if (this.player != null) {
            LinkedPlayer.onlineLinkedPlayers.add(this);
        }
    }

    public static void sendActionBarAlerts() {
        LinkedPlayer.onlineLinkedPlayers.forEach(LinkedPlayer::sendActionBarAlert);
    }

    public static Set<LinkedPlayer> getOnlineLinkedPlayers() {
        return LinkedPlayer.onlineLinkedPlayers;
    }

    public static LinkedPlayer fromMemberId(String memberId) {
        return LinkedPlayer.onlineLinkedPlayers.stream()
                .filter(p -> p.getDiscordId().equals(memberId))
                .findFirst().orElse(null);
    }

    public boolean isStateEligible() {
        return (this.plugin.getConfigYamlFile().getBoolean(ConfigField.PLAYERS_ON_DEATH_SCREEN_INCLUDED.toString()) || !this.player.isDead())
                && (this.plugin.getConfigYamlFile().getBoolean(ConfigField.SPECTATORS_INCLUDED.toString()) || this.player.getGameMode() != SkoiceGameMode.SPECTATOR)
                && !this.plugin.getConfigYamlFile().getStringList(ConfigField.DISABLED_WORLDS.toString()).contains(this.player.getWorld());
    }

    public void addActionBarAlert(ActionBarAlert alert) {
        this.alerts.add(alert);
    }

    public void sendActionBarAlert() {
        ActionBarAlert priorityAlert = ActionBarAlert.getPriorityAlert(this.alerts);
        if (priorityAlert != null) {
            this.player.sendActionBar(this.plugin.getLang().getMessage("action-bar." + priorityAlert));
        }

        this.alerts.clear();
    }

    public Set<LinkedPlayer> getPlayersWithinRange() {
        return LinkedPlayer.onlineLinkedPlayers.stream()
                .filter(p -> (p.isInMainVoiceChannel() || p.isInAnyProximityChannel()) && !p.equals(this) && p.isStateEligible() && p.isCloseEnoughToPlayer(this, false))
                .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet));
    }

    public boolean isInMainVoiceChannel() {
        VoiceChannel mainVoiceChannel = this.plugin.getConfigYamlFile().getVoiceChannel();
        if (mainVoiceChannel == null) {
            return false;
        }
        return mainVoiceChannel.getMembers().stream().anyMatch(member -> this.discordId.equals(member.getId()));
    }

    public boolean isInAnyNetwork() {
        return Networks.getAll().stream().anyMatch(network -> network.contains(this));
    }

    public Network getNetwork() {
        return Networks.getAll().stream()
                .filter(network -> network.contains(this))
                .findFirst().orElse(null);
    }

    public boolean isInAnyProximityChannel() {
        return ProximityChannels.getInitialized().stream()
                .anyMatch(proximityChannel -> proximityChannel.getChannel().getMembers().stream()
                        .anyMatch(member -> member.getId().equals(this.discordId)));
    }

    public boolean isCloseEnoughToPlayer(LinkedPlayer linkedPlayer, boolean falloff) {
        if (!this.player.getWorld().equals(linkedPlayer.player.getWorld())) {
            return false;
        }

        if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.SEPARATED_TEAMS.toString())) {
            String playerTeam = this.player.getTeam();
            if (playerTeam == null) {
                if (linkedPlayer.getFullPlayer().getTeam() != null) {
                    return false;
                }
            } else if (!playerTeam.equals(linkedPlayer.getFullPlayer().getTeam())) {
                return false;
            }
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

    public FullPlayer getFullPlayer() {
        return this.player;
    }

    public String getDiscordId() {
        return this.discordId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        LinkedPlayer that = (LinkedPlayer) o;
        return Objects.equals(this.player, that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.player);
    }
}
