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

package net.clementraynaud.skoice.common.tasks;

import com.bugsnag.Severity;
import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.model.minecraft.FullPlayer;
import net.clementraynaud.skoice.common.model.minecraft.SkoiceLocation;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.clementraynaud.skoice.common.system.ActionBarAlert;
import net.clementraynaud.skoice.common.system.LinkedPlayer;
import net.clementraynaud.skoice.common.system.Network;
import net.clementraynaud.skoice.common.system.Networks;
import net.clementraynaud.skoice.common.system.ProximityChannel;
import net.clementraynaud.skoice.common.system.ProximityChannels;
import net.clementraynaud.skoice.common.system.SpatialIndex;
import net.clementraynaud.skoice.common.util.DistanceUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
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
        this.taskId = this.plugin.getScheduler().runTaskTimerAsynchronously(
                this::run,
                Duration.ZERO,
                Duration.ofMillis(500)
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

            Set<String> membersInMainVoiceChannel = new HashSet<>();
            mainVoiceChannel.getMembers().forEach(member -> membersInMainVoiceChannel.add(member.getId()));

            Set<String> membersInProximityChannels = new HashSet<>();
            ProximityChannels.getInitialized().stream()
                    .map(ProximityChannel::getChannel)
                    .filter(Objects::nonNull)
                    .flatMap(channel -> channel.getMembers().stream())
                    .forEach(member -> membersInProximityChannels.add(member.getId()));

            Set<String> connectedMembers = new HashSet<>(membersInMainVoiceChannel);
            connectedMembers.addAll(membersInProximityChannels);

            SpatialIndex spatialIndex = this.buildSpatialIndex();

            this.manageConnectedPlayers();
            this.splitSpreadNetworks(connectedMembers, spatialIndex);
            this.manageIsolatedPlayers(connectedMembers, spatialIndex);
            this.mergeNetworks(spatialIndex);
            this.manageMoves(connectedMembers);

            Networks.clean();

            int userCount = 0;
            int maxIsolatedUsers = 0;

            Set<ProximityChannel> usedByNetworks = Networks.getProximityChannels();
            Map<String, ProximityChannel> isolationMap = ProximityChannels.getIsolationChannelMap();
            Set<ProximityChannel> reservedChannels = new HashSet<>(usedByNetworks);
            reservedChannels.addAll(isolationMap.values());
            List<ProximityChannel> availableChannels = ProximityChannels.getAll().stream()
                    .filter(channel -> !reservedChannels.contains(channel))
                    .sorted(Comparator.comparing(ProximityChannel::getChannelId))
                    .collect(Collectors.toCollection(ArrayList::new));

            for (String memberId : connectedMembers) {
                Member member = this.plugin.getBot().getGuild().getMemberById(memberId);
                if (member == null || member.getVoiceState() == null || member.getVoiceState().getChannel() == null) {
                    continue;
                }

                Network network = null;
                GuildVoiceState voiceState = member.getVoiceState();
                VoiceChannel currentChannel = voiceState.getChannel().asVoiceChannel();

                LinkedPlayer linkedPlayer = LinkedPlayer.fromMemberId(memberId);
                if (linkedPlayer != null) {
                    userCount++;
                    network = linkedPlayer.getNetwork();

                    if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.MUTED_ALERT.toString())
                            && voiceState.isMuted()
                            && !membersInMainVoiceChannel.contains(memberId)
                            && !linkedPlayer.isInAnyIsolationChannel()) {
                        linkedPlayer.addActionBarAlert(ActionBarAlert.MUTED);
                    }
                    if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.DEAFENED_ALERT.toString())
                            && voiceState.isDeafened()) {
                        linkedPlayer.addActionBarAlert(ActionBarAlert.DEAFENED);
                    }
                }

                boolean shouldBeIsolated = false;
                if (member.hasPermission(mainVoiceChannel, Permission.VOICE_SPEAK, Permission.VOICE_MUTE_OTHERS)
                        && !member.getUser().isBot()) {
                    shouldBeIsolated = true;
                    maxIsolatedUsers++;
                }

                VoiceChannel shouldBeInChannel;
                if (network != null) {
                    shouldBeInChannel = network.getProximityChannel().getChannel();
                    if (shouldBeInChannel == null) {
                        continue;
                    }
                    ProximityChannels.getIsolationChannelMap().remove(memberId);
                } else if (shouldBeIsolated) {
                    ProximityChannel proximityChannel = isolationMap.get(memberId);
                    if (proximityChannel == null) {
                        proximityChannel = availableChannels.stream()
                                .min(Comparator.comparing((ProximityChannel channel) ->
                                                !channel.getChannelId().equals(currentChannel.getId()))
                                        .thenComparing(ProximityChannel::getChannelId))
                                .orElse(null);
                        if (proximityChannel != null) {
                            availableChannels.remove(proximityChannel);
                        } else {
                            proximityChannel = new ProximityChannel(this.plugin, (Network) null);
                        }
                        isolationMap.put(memberId, proximityChannel);
                    }
                    shouldBeInChannel = proximityChannel.getChannel();
                    if (shouldBeInChannel == null) {
                        continue;
                    }
                } else {
                    ProximityChannels.getIsolationChannelMap().remove(memberId);
                    shouldBeInChannel = mainVoiceChannel;
                }

                Pair<String, CompletableFuture<Void>> awaitingMove = this.awaitingMoves.get(memberId);

                if (awaitingMove == null
                        || !awaitingMove.getLeft().equals(shouldBeInChannel.getId())
                        && awaitingMove.getRight().cancel(false)) {
                    if (currentChannel != shouldBeInChannel) {
                        boolean sendConnectingAlert = this.plugin.getConfigYamlFile().getBoolean(ConfigField.CONNECTING_ALERT.toString())
                                && linkedPlayer != null
                                && (membersInMainVoiceChannel.contains(memberId) || linkedPlayer.isInAnyIsolationChannel());
                        this.awaitingMoves.put(memberId, Pair.of(
                                shouldBeInChannel.getId(),
                                this.plugin.getBot().getGuild().moveVoiceMember(member, shouldBeInChannel)
                                        .submit().whenCompleteAsync((v, t) -> {
                                            this.awaitingMoves.remove(memberId);
                                            if (sendConnectingAlert) {
                                                linkedPlayer.addActionBarAlert(ActionBarAlert.CONNECTING);
                                            }
                                        })
                        ));
                    }
                }
            }

            LinkedPlayer.sendActionBarAlerts();
            this.sendLinkingSuggestion(connectedMembers);

            ProximityChannels.clean(userCount, maxIsolatedUsers);

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

    private void splitSpreadNetworks(Set<String> connectedMembers, SpatialIndex spatialIndex) {
        Networks.getAll().forEach(network -> network.splitIfSpread(connectedMembers, spatialIndex));
    }

    private void manageIsolatedPlayers(Set<String> connectedMembers, SpatialIndex spatialIndex) {
        LinkedPlayer.getOnlineLinkedPlayers().stream()
                .filter(LinkedPlayer::isStateEligible)
                .filter(p -> connectedMembers.contains(p.getDiscordId()))
                .filter(p -> !p.isInAnyNetwork())
                .forEach(p -> {
                    Set<LinkedPlayer> playersWithinRange = p.getPlayersWithinRange(connectedMembers, spatialIndex);

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

    private void mergeNetworks(SpatialIndex spatialIndex) {
        for (LinkedPlayer p : LinkedPlayer.getOnlineLinkedPlayers()) {
            Network myNetwork = p.getNetwork();
            if (myNetwork == null) {
                continue;
            }
            for (LinkedPlayer other : spatialIndex.candidatesFor(p)) {
                if (other.equals(p) || !other.isStateEligible() || !p.isStateEligible()) {
                    continue;
                }
                Network otherNetwork = other.getNetwork();
                Network currentMyNetwork = p.getNetwork();
                if (otherNetwork == null || currentMyNetwork == null || otherNetwork.equals(currentMyNetwork)) {
                    continue;
                }
                if (p.isCloseEnoughToPlayer(other, false)) {
                    Networks.merge(currentMyNetwork, otherNetwork);
                }
            }
        }
    }

    private SpatialIndex buildSpatialIndex() {
        int horizontalRadius = this.plugin.getConfigYamlFile().getInt(ConfigField.HORIZONTAL_RADIUS.toString());
        int verticalRadius = this.plugin.getConfigYamlFile().getInt(ConfigField.VERTICAL_RADIUS.toString());
        boolean teamCommunication = this.plugin.getConfigYamlFile().getBoolean(ConfigField.TEAM_COMMUNICATION.toString());
        SpatialIndex index = new SpatialIndex(horizontalRadius, verticalRadius, teamCommunication);
        for (LinkedPlayer player : LinkedPlayer.getOnlineLinkedPlayers()) {
            if (player.isStateEligible()) {
                index.add(player);
            }
        }
        return index;
    }

    private void manageMoves(Set<String> connectedMembers) {
        LinkedPlayer.getOnlineLinkedPlayers().stream()
                .filter(p -> !connectedMembers.contains(p.getDiscordId()))
                .map(p -> this.awaitingMoves.get(p.getDiscordId()))
                .filter(Objects::nonNull)
                .forEach(pair -> pair.getRight().cancel(false));
    }

    public Map<String, Pair<String, CompletableFuture<Void>>> getAwaitingMoves() {
        return this.awaitingMoves;
    }

    private void sendLinkingSuggestion(Set<String> connectedMembers) {
        if (!this.plugin.getConfigYamlFile().getBoolean(ConfigField.LINKING_SUGGESTION.toString())) {
            return;
        }

        List<FullPlayer> usingPlayers = LinkedPlayer.getOnlineLinkedPlayers().stream()
                .filter(p -> connectedMembers.contains(p.getDiscordId()))
                .filter(LinkedPlayer::isStateEligible)
                .map(LinkedPlayer::getFullPlayer)
                .collect(Collectors.toList());

        if (usingPlayers.isEmpty()) {
            return;
        }

        Set<UUID> usingPlayerIds = usingPlayers.stream()
                .map(FullPlayer::getUniqueId)
                .collect(Collectors.toSet());

        List<String> disabledWorlds = this.plugin.getConfigYamlFile().getStringList(ConfigField.DISABLED_WORLDS.toString());
        int horizontalRadius = this.plugin.getConfigYamlFile().getInt(ConfigField.HORIZONTAL_RADIUS.toString());
        int verticalRadius = this.plugin.getConfigYamlFile().getInt(ConfigField.VERTICAL_RADIUS.toString());

        for (FullPlayer player : this.plugin.getOnlinePlayers()) {
            if (usingPlayerIds.contains(player.getUniqueId())) {
                continue;
            }
            if (disabledWorlds.contains(player.getWorld())) {
                continue;
            }
            SkoiceLocation playerLocation = player.getLocation();
            if (playerLocation == null) {
                continue;
            }
            for (FullPlayer usingPlayer : usingPlayers) {
                if (!player.getWorld().equals(usingPlayer.getWorld())) {
                    continue;
                }
                SkoiceLocation usingLocation = usingPlayer.getLocation();
                if (usingLocation == null) {
                    continue;
                }
                if (DistanceUtil.getHorizontalDistance(playerLocation, usingLocation) <= horizontalRadius
                        && DistanceUtil.getVerticalDistance(playerLocation, usingLocation) <= verticalRadius) {
                    player.sendActionBar(this.plugin.getLang().getMessage("action-bar.linking-suggestion"));
                    break;
                }
            }
        }
    }
}
