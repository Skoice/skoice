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

package net.clementraynaud.skoice.tasks;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.clementraynaud.skoice.system.LinkedPlayer;
import net.clementraynaud.skoice.system.Network;
import net.clementraynaud.skoice.system.Networks;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class UpdateNetworksTask {

    private static final Map<String, Pair<String, CompletableFuture<Void>>> awaitingMoves = new ConcurrentHashMap<>();

    private final ReentrantLock lock = new ReentrantLock();

    private final Skoice plugin;

    public UpdateNetworksTask(Skoice plugin) {
        this.plugin = plugin;
    }

    public static Map<String, Pair<String, CompletableFuture<Void>>> getAwaitingMoves() {
        return UpdateNetworksTask.awaitingMoves;
    }

    public void run() {
        if (!this.lock.tryLock()) {
            return;
        }

        try {
            VoiceChannel mainVoiceChannel = this.plugin.getConfigYamlFile().getVoiceChannel();
            if (mainVoiceChannel == null) {
                return;
            }

            this.manageNetworks();

            Set<Member> members = new HashSet<>(mainVoiceChannel.getMembers());
            for (Network network : Networks.getInitialized()) {
                members.addAll(network.getChannel().getMembers());
            }

            for (Member member : members) {
                Network playerNetwork = null;

                LinkedPlayer linkedPlayer = LinkedPlayer.getOnlineLinkedPlayers().stream()
                        .filter(p -> p.getMember().equals(member))
                        .findFirst().orElse(null);
                if (linkedPlayer != null) {
                    playerNetwork = Networks.getAll().stream()
                            .filter(network -> network.contains(linkedPlayer))
                            .findFirst().orElse(null);
                }

                VoiceChannel shouldBeInChannel;
                if (playerNetwork != null && playerNetwork.isInitialized()) {
                    shouldBeInChannel = playerNetwork.getChannel();
                } else {
                    shouldBeInChannel = mainVoiceChannel;
                }

                Pair<String, CompletableFuture<Void>> awaitingMove = UpdateNetworksTask.awaitingMoves.get(member.getId());
                if (awaitingMove == null
                        || !awaitingMove.getLeft().equals(shouldBeInChannel.getId()) && awaitingMove.getRight().cancel(false)) {
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

        } finally {
            this.lock.unlock();
        }
    }

    private void manageNetworks() {
        Networks.getInitialized().removeIf(network -> network.getChannel() == null);

        for (LinkedPlayer linkedPlayer : LinkedPlayer.getOnlineLinkedPlayers()) {
            if (linkedPlayer.isInMainVoiceChannel() || linkedPlayer.isInAnyNetworkChannel()) {
                linkedPlayer.updateNearNetworks();
                if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.ACTION_BAR_ALERT.toString())) {
                    linkedPlayer.sendActionBarAlert();
                }
                if (linkedPlayer.isStateEligible()
                        && this.plugin.getConfigYamlFile().getCategory().getChannels().size() != 50) {
                    Set<LinkedPlayer> playersWithinRange = linkedPlayer.getPlayersWithinRange();
                    if (!playersWithinRange.isEmpty()) {
                        playersWithinRange.add(linkedPlayer);
                        new Network(this.plugin, playersWithinRange).build();
                    }
                }

            } else {
                Pair<String, CompletableFuture<Void>> pair = UpdateNetworksTask.awaitingMoves.get(linkedPlayer.getMember().getId());
                if (pair != null) {
                    pair.getRight().cancel(false);
                }
            }
        }
    }
}
