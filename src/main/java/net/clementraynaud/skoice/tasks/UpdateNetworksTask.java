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
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.clementraynaud.skoice.system.LinkedPlayer;
import net.clementraynaud.skoice.system.Network;
import net.clementraynaud.skoice.system.Networks;
import net.clementraynaud.skoice.system.ProximityChannel;
import net.clementraynaud.skoice.system.ProximityChannels;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class UpdateNetworksTask {

    private static final Map<String, Pair<String, CompletableFuture<Void>>> awaitingMoves = new ConcurrentHashMap<>();

    private final ReentrantLock lock = new ReentrantLock();

    private final Skoice plugin;

    public UpdateNetworksTask(Skoice plugin) {
        this.plugin = plugin;
    }

    public static Map<String, Pair<String, CompletableFuture<Void>>> getAwaitingMoves() {
       try {
            if (Bukkit.isPrimaryThread() && Bukkit.getPluginManager().isPluginEnabled("Skoice")) {
                new IllegalStateException("This method should not be called from the main thread.").printStackTrace();
            }
        } catch (NullPointerException ignored) {
        }
        return UpdateNetworksTask.awaitingMoves;
    }

    public void run() {
       try {
            if (Bukkit.isPrimaryThread()) {
                new IllegalStateException("This method should not be called from the main thread.").printStackTrace();
            }
        } catch (NullPointerException ignored) {
        }
        if (!this.lock.tryLock()) {
            return;
        }

        try {
            VoiceChannel mainVoiceChannel = this.plugin.getConfigYamlFile().getVoiceChannel();
            if (mainVoiceChannel == null) {
                return;
            }

            this.manageConnectedPlayers();
            this.splitSpreadNetworks();
            this.manageIsolatedPlayers();
            this.mergeNetworks();
            this.manageMoves();

            Set<Member> connectedMembers = new HashSet<>(mainVoiceChannel.getMembers());
            connectedMembers.addAll(ProximityChannels.getInitialized().stream()
                    .map(ProximityChannel::getChannel)
                    .filter(Objects::nonNull)
                    .flatMap(channel -> channel.getMembers().stream())
                    .collect(Collectors.toSet()));

            for (Member member : connectedMembers) {
                Network network = null;

                LinkedPlayer linkedPlayer = LinkedPlayer.fromMemberId(member.getId());
                if (linkedPlayer != null) {
                    network = linkedPlayer.getNetwork();
                }

                VoiceChannel shouldBeInChannel;
                if (network != null) {
                    if (!network.getProximityChannel().isInitialized()) {
                        continue;
                    }
                    shouldBeInChannel = network.getProximityChannel().getChannel();
                } else {
                    shouldBeInChannel = mainVoiceChannel;
                }

                Pair<String, CompletableFuture<Void>> awaitingMove = UpdateNetworksTask.awaitingMoves.get(member.getId());
                if (awaitingMove == null
                        || !awaitingMove.getLeft().equals(shouldBeInChannel.getId())
                        && awaitingMove.getRight().cancel(false)) {
                    GuildVoiceState voiceState = member.getVoiceState();
                    if (voiceState != null && voiceState.getChannel() != shouldBeInChannel) {
                        UpdateNetworksTask.awaitingMoves.put(member.getId(), Pair.of(
                                shouldBeInChannel.getId(),
                                this.plugin.getBot().getGuild().moveVoiceMember(member, shouldBeInChannel)
                                        .submit().whenCompleteAsync((v, t) -> UpdateNetworksTask.awaitingMoves.remove(member.getId()))
                        ));
                    }
                }
            }

            Networks.clean();
            ProximityChannels.clean(connectedMembers.size());

        } finally {
            this.lock.unlock();
        }
    }

    private void manageConnectedPlayers() {
        LinkedPlayer.getOnlineLinkedPlayers().stream()
                .filter(LinkedPlayer::isInAnyNetwork)
                .forEach(p -> {
                    Network network = p.getNetwork();

                    if (!network.canPlayerStayConnected(p)) {
                        network.remove(p);

                    } else if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.DISCONNECTING_ALERT.toString())
                            && !network.canPlayerConnect(p)) {
                        p.sendDisconnectingAlert();
                    }
                });
    }

    private void splitSpreadNetworks() {
        Networks.getAll().forEach(Network::splitIfSpread);
    }

    private void manageIsolatedPlayers() {
        LinkedPlayer.getOnlineLinkedPlayers().stream()
                .filter(LinkedPlayer::isStateEligible)
                .filter(LinkedPlayer::isInMainVoiceChannel)
                .filter(p -> !p.isInAnyNetwork())
                .forEach(p -> {
                    Set<LinkedPlayer> playersWithinRange = p.getPlayersWithinRange();

                    if (!playersWithinRange.isEmpty()) {
                        playersWithinRange.stream()
                                .filter(LinkedPlayer::isInAnyNetwork)
                                .findFirst()
                                .ifPresent(playerInNearNetwork -> {
                                    playerInNearNetwork.getNetwork().add(p);

                                    if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.CONNECTING_ALERT.toString())) {
                                        p.sendConnectingAlert();
                                    }
                                });

                        if (!p.isInAnyNetwork()
                                && this.plugin.getConfigYamlFile().getCategory().getChannels().size() != 50) {
                            playersWithinRange.add(p);
                            new Network(this.plugin, playersWithinRange).build();

                            if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.CONNECTING_ALERT.toString())) {
                                playersWithinRange.forEach(LinkedPlayer::sendConnectingAlert);
                            }
                        }
                    }
                });
    }

    private void mergeNetworks() {
        Networks.getAll()
                .forEach(network -> LinkedPlayer.getOnlineLinkedPlayers().stream()
                        .filter(LinkedPlayer::isInAnyNetwork)
                        .filter(p -> !p.getNetwork().equals(network))
                        .filter(network::canPlayerConnect)
                        .forEach(p -> Networks.merge(network, p.getNetwork()))
                );
    }

    private void manageMoves() {
       try {
            if (Bukkit.isPrimaryThread()) {
                new IllegalStateException("This method should not be called from the main thread.").printStackTrace();
            }
        } catch (NullPointerException ignored) {
        }
        LinkedPlayer.getOnlineLinkedPlayers().forEach(p -> {
            if (!p.isInMainVoiceChannel() && !p.isInAnyProximityChannel()) {
                Pair<String, CompletableFuture<Void>> pair = UpdateNetworksTask.awaitingMoves.get(p.getDiscordId());
                if (pair != null) {
                    pair.getRight().cancel(false);
                }
            }
        });
    }
}
