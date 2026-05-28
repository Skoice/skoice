/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025, 2026 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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
import net.clementraynaud.skoice.common.model.minecraft.BasePlayer;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Network {

    private final Skoice plugin;
    private final Set<LinkedPlayer> players = ConcurrentHashMap.newKeySet();
    private volatile ProximityChannel proximityChannel;

    public Network(Skoice plugin, Set<LinkedPlayer> players) {
        this.plugin = plugin;
        this.players.addAll(players);
        for (LinkedPlayer player : players) {
            player.setNetwork(this);
        }
        Networks.add(this);
    }

    public void build() {
        Set<ProximityChannel> usedChannels = Networks.getProximityChannels();
        Set<String> playerDiscordIds = this.players.stream()
                .map(LinkedPlayer::getDiscordId)
                .collect(Collectors.toSet());
        Map<String, ProximityChannel> isolationMap = ProximityChannels.getIsolationChannelMap();
        this.proximityChannel = ProximityChannels.getAll().stream()
                .filter(channel -> !usedChannels.contains(channel))
                .filter(channel ->
                        isolationMap.entrySet().stream()
                                .filter(entry -> entry.getValue().equals(channel))
                                .allMatch(entry -> playerDiscordIds.contains(entry.getKey()))
                )
                .min(Comparator.comparing(ProximityChannel::getChannelId))
                .orElseGet(() -> new ProximityChannel(this.plugin, this));
    }

    public boolean canPlayerConnect(LinkedPlayer player) {
        if (!player.isStateEligible()) {
            return false;
        }
        return this.players.stream().anyMatch(p -> p.isStateEligible() && p.isCloseEnoughToPlayer(player, false) && !p.equals(player));
    }

    public boolean canPlayerStayConnected(LinkedPlayer player) {
        if (!player.isStateEligible()) {
            return false;
        }
        return this.players.stream().anyMatch(p -> p.isStateEligible() && p.isCloseEnoughToPlayer(player, true) && !p.equals(player));
    }

    public void splitIfSpread(Set<String> connectedMembers) {
        this.splitIfSpread(connectedMembers, null);
    }

    public void splitIfSpread(Set<String> connectedMembers, SpatialIndex index) {
        if (this.size() < 4) {
            return;
        }

        Set<LinkedPlayer> playersWithinRange = this.getChainingPlayers(connectedMembers, this.players.iterator().next(), index);
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

    private Set<LinkedPlayer> getChainingPlayers(Set<String> connectedMembers, LinkedPlayer startingPoint, SpatialIndex index) {
        return this.getChainingPlayers(connectedMembers, new HashSet<>(Collections.singleton(startingPoint)), Collections.singleton(startingPoint), index);
    }

    private Set<LinkedPlayer> getChainingPlayers(Set<String> connectedMembers, Set<LinkedPlayer> chainingPlayers, Set<LinkedPlayer> children, SpatialIndex index) {
        Set<LinkedPlayer> newChildren = new HashSet<>();
        children.forEach(p -> p.getPlayersWithinRange(connectedMembers, index).stream()
                .filter(this::contains)
                .filter(playerWithingRange -> !chainingPlayers.contains(playerWithingRange))
                .forEach(newChildren::add)
        );

        if (newChildren.isEmpty()) {
            return chainingPlayers;
        }

        chainingPlayers.addAll(newChildren);
        return this.getChainingPlayers(connectedMembers, chainingPlayers, newChildren, index);
    }

    public void engulf(Network network) {
        for (LinkedPlayer player : network.players) {
            player.setNetwork(this);
        }
        this.players.addAll(network.players);
        network.players.clear();
    }

    public void clear() {
        for (LinkedPlayer player : this.players) {
            if (player.getNetwork() == this) {
                player.setNetwork(null);
            }
        }
        this.players.clear();
    }

    public void add(LinkedPlayer player) {
        if (this.players.add(player)) {
            player.setNetwork(this);
        }
    }

    public void remove(LinkedPlayer player) {
        if (this.players.remove(player) && player.getNetwork() == this) {
            player.setNetwork(null);
        }
    }

    public void remove(BasePlayer player) {
        LinkedPlayer match = this.players.stream()
                .filter(p -> p.getFullPlayer().equals(player))
                .findFirst().orElse(null);
        if (match != null) {
            this.players.remove(match);
            if (match.getNetwork() == this) {
                match.setNetwork(null);
            }
        }
    }

    public boolean contains(LinkedPlayer player) {
        return this.players.contains(player);
    }

    public boolean contains(BasePlayer player) {
        return this.players.stream().anyMatch(p -> p.getFullPlayer().equals(player));
    }

    public int size() {
        return this.players.size();
    }

    public boolean isEmpty() {
        return this.size() < 2;
    }

    public ProximityChannel getProximityChannel() {
        return this.proximityChannel;
    }
}
