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

package net.clementraynaud.skoice.common.tasks;

import com.bugsnag.Severity;
import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.clementraynaud.skoice.common.system.ActionBarAlert;
import net.clementraynaud.skoice.common.system.LinkedPlayer;
import net.clementraynaud.skoice.common.system.Network;
import net.clementraynaud.skoice.common.system.Networks;
import net.clementraynaud.skoice.common.system.ProximityChannel;
import net.clementraynaud.skoice.common.system.ProximityChannels;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class UpdateNetworksTask {

    private final Skoice plugin;
    private final Map<String, Pair<String, CompletableFuture<Void>>> awaitingMoves = new ConcurrentHashMap<>();

    private final ReentrantLock lock = new ReentrantLock();
    private int taskId;

    public UpdateNetworksTask(Skoice plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Duration period = Duration.ofMillis(500);
        if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.LUDICROUS.toString())) {
            period = Duration.ofMillis(100);
        }
        this.taskId = this.plugin.getScheduler().runTaskTimerAsynchronously(
                this::run,
                Duration.ZERO,
                period
        );
    }

    public void interrupt() {
        this.plugin.getScheduler().cancelTask(this.taskId);

        for (Pair<String, CompletableFuture<Void>> value : this.awaitingMoves.values()) {
            value.getRight().cancel(true);
        }
    }

    private void run() {
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

            Set<Member> connectedMembers = ProximityChannels.getInitialized().stream()
                    .map(ProximityChannel::getChannel)
                    .filter(Objects::nonNull)
                    .flatMap(channel -> channel.getMembers().stream())
                    .collect(Collectors.toCollection(HashSet::new));
            connectedMembers.addAll(mainVoiceChannel.getMembers());

            for (Member member : connectedMembers) {
                Network network = null;

                LinkedPlayer linkedPlayer = LinkedPlayer.fromMemberId(member.getId());
                if (linkedPlayer != null) {
                    network = linkedPlayer.getNetwork();

                    GuildVoiceState voiceState = member.getVoiceState();
                    if (voiceState != null) {
                        if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.MUTED_ALERT.toString())
                                && voiceState.isMuted()
                                && !linkedPlayer.isInMainVoiceChannel()
                                && !UpdateVoiceStateTask.getMutedUsers().contains(member.getId())) {
                            linkedPlayer.addActionBarAlert(ActionBarAlert.MUTED);
                        }
                        if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.DEAFENED_ALERT.toString())
                                && voiceState.isDeafened()) {
                            linkedPlayer.addActionBarAlert(ActionBarAlert.DEAFENED);
                        }
                    }
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

                Pair<String, CompletableFuture<Void>> awaitingMove = this.awaitingMoves.get(member.getId());
                if (awaitingMove == null
                        || !awaitingMove.getLeft().equals(shouldBeInChannel.getId())
                        && awaitingMove.getRight().cancel(false)) {
                    GuildVoiceState voiceState = member.getVoiceState();
                    if (voiceState != null && voiceState.getChannel() != shouldBeInChannel) {
                        boolean sendConnectingAlert = this.plugin.getConfigYamlFile().getBoolean(ConfigField.CONNECTING_ALERT.toString())
                                && linkedPlayer != null
                                && linkedPlayer.isInMainVoiceChannel();
                        this.awaitingMoves.put(member.getId(), Pair.of(
                                shouldBeInChannel.getId(),
                                this.plugin.getBot().getGuild().moveVoiceMember(member, shouldBeInChannel)
                                        .submit().whenCompleteAsync((v, t) -> {
                                            this.awaitingMoves.remove(member.getId());
                                            if (sendConnectingAlert) {
                                                linkedPlayer.addActionBarAlert(ActionBarAlert.CONNECTING);
                                            }
                                        })
                        ));
                    }
                }
            }

            LinkedPlayer.sendActionBarAlerts();

            Networks.clean();

            int possibleUsers = (int) connectedMembers.stream()
                    .map(Member::getId)
                    .map(LinkedPlayer::fromMemberId)
                    .filter(Objects::nonNull)
                    .count();
            ProximityChannels.clean(possibleUsers);
        } catch (Throwable throwable) {
            Skoice.analyticManager().getBugsnag().notify(throwable, Severity.ERROR);
            throw throwable;
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
                        p.addActionBarAlert(ActionBarAlert.DISCONNECTING);
                    }
                });
    }

    private void splitSpreadNetworks() {
        Networks.getAll().forEach(Network::splitIfSpread);
    }

    private void manageIsolatedPlayers() {
        LinkedPlayer.getOnlineLinkedPlayers().stream()
                .filter(LinkedPlayer::isStateEligible)
                .filter(p -> p.isInMainVoiceChannel() || p.isInAnyProximityChannel())
                .filter(p -> !p.isInAnyNetwork())
                .forEach(p -> {
                    Set<LinkedPlayer> playersWithinRange = p.getPlayersWithinRange();

                    if (!playersWithinRange.isEmpty()) {
                        playersWithinRange.stream()
                                .filter(LinkedPlayer::isInAnyNetwork)
                                .findFirst()
                                .ifPresent(playerInNearNetwork -> playerInNearNetwork.getNetwork().add(p));

                        if (!p.isInAnyNetwork()
                                && this.plugin.getConfigYamlFile().getCategory().getChannels().size() != 50) {
                            playersWithinRange.add(p);
                            new Network(this.plugin, playersWithinRange).build();
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
        LinkedPlayer.getOnlineLinkedPlayers().stream()
                .filter(p -> !p.isInMainVoiceChannel() && !p.isInAnyProximityChannel())
                .map(p -> this.awaitingMoves.get(p.getDiscordId()))
                .filter(Objects::nonNull)
                .forEach(pair -> pair.getRight().cancel(false));
    }
}
