/*
 * Copyright 2020, 2021, 2022 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.system.EligiblePlayers;
import net.clementraynaud.skoice.system.Network;
import net.clementraynaud.skoice.lang.MinecraftLang;
import net.clementraynaud.skoice.util.PlayerUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static net.clementraynaud.skoice.system.Network.*;
import static net.clementraynaud.skoice.util.DistanceUtil.getHorizontalDistance;
import static net.clementraynaud.skoice.util.DistanceUtil.getVerticalDistance;
import static net.clementraynaud.skoice.config.Config.*;

public class UpdateNetworksTask implements Task {

    public static final Map<String, Pair<String, CompletableFuture<Void>>> awaitingMoves = new ConcurrentHashMap<>();
    private static final ReentrantLock lock = new ReentrantLock();

    @Override
    public void run() {
        if (!lock.tryLock()) {
            return;
        }
        try {
            VoiceChannel lobby = getLobby();
            if (lobby == null)
                return;
            muteMembers(lobby);
            networks.removeIf(network -> network.getChannel() == null && network.isInitialized());
            EligiblePlayers eligiblePlayers = new EligiblePlayers();
            Set<UUID> oldEligiblePlayers = eligiblePlayers.get();
            eligiblePlayers.clear();
            for (UUID minecraftID : oldEligiblePlayers) {
                Player player = Bukkit.getPlayer(minecraftID);
                if (player != null) {
                    Member member = getMember(player.getUniqueId());
                    if (member != null && member.getVoiceState() != null && member.getVoiceState().getChannel() != null) {
                        VoiceChannel playerChannel = member.getVoiceState().getChannel();
                        boolean isLobby = playerChannel == getLobby();
                        if (!isLobby && (playerChannel.getParent() == null || playerChannel.getParent() != getCategory())) {
                            Pair<String, CompletableFuture<Void>> pair = awaitingMoves.get(member.getId());
                            if (pair != null)
                                pair.getRight().cancel(false);
                            continue;
                        }
                        updateNetworksAroundPlayer(player);
                        if (getActionBarAlert())
                            sendActionBarAlert(player);
                        createNetworkIfNeeded(player);
                    }
                }
            }
            Set<Member> membersInLobby = new HashSet<>(lobby.getMembers());
            for (Network network : getNetworks()) {
                VoiceChannel voiceChannel = network.getChannel();
                if (voiceChannel == null) continue;
                membersInLobby.addAll(voiceChannel.getMembers());
            }
            for (Member member : membersInLobby) {
                String minecraftID = getKeyFromValue(getLinkMap(), member.getId());
                VoiceChannel playerChannel = member.getVoiceState().getChannel();
                Network playerNetwork = minecraftID != null ? networks.stream()
                        .filter(n -> n.contains(UUID.fromString(minecraftID)))
                        .findAny().orElse(null) : null;
                VoiceChannel shouldBeInChannel;
                if (playerNetwork != null) {
                    if (playerNetwork.getChannel() == null)
                        continue;
                    shouldBeInChannel = playerNetwork.getChannel();
                } else {
                    shouldBeInChannel = lobby;
                }
                Pair<String, CompletableFuture<Void>> awaitingMove = awaitingMoves.get(member.getId());
                if (awaitingMove != null && awaitingMove.getLeft().equals(shouldBeInChannel.getId()))
                    continue;
                if (awaitingMove != null && !awaitingMove.getLeft().equals(shouldBeInChannel.getId())
                        && !awaitingMove.getRight().cancel(false))
                    continue;
                if (playerChannel != shouldBeInChannel) {
                    awaitingMoves.put(member.getId(), Pair.of(
                            shouldBeInChannel.getId(),
                            getGuild().moveVoiceMember(member, shouldBeInChannel)
                                    .submit().whenCompleteAsync((v, t) -> awaitingMoves.remove(member.getId()))
                    ));
                }
            }
            deleteEmptyNetworks();
        } finally {
            lock.unlock();
        }
    }

    private void muteMembers(VoiceChannel lobby) {
        Role publicRole = lobby.getGuild().getPublicRole();
        PermissionOverride lobbyPublicRoleOverride = lobby.getPermissionOverride(publicRole);
        if (lobbyPublicRoleOverride == null)
            lobby.createPermissionOverride(publicRole).deny(Permission.VOICE_SPEAK).queue();
        else if (!lobbyPublicRoleOverride.getDenied().contains(Permission.VOICE_SPEAK))
            lobbyPublicRoleOverride.getManager().deny(Permission.VOICE_SPEAK).queue();
    }

    private void updateNetworksAroundPlayer(Player player) {
        networks.stream()
                .filter(network -> network.canPlayerBeAdded(player))
                .reduce((network1, network2) -> network1.size() > network2.size()
                        ? network1.engulf(network2)
                        : network2.engulf(network1))
                .filter(network -> !network.contains(player.getUniqueId()))
                .ifPresent(network -> network.add(player.getUniqueId()));
        networks.stream()
                .filter(network -> network.contains(player.getUniqueId()))
                .filter(network -> !network.canPlayerStayConnected(player))
                .forEach(network -> {
                    network.remove(player.getUniqueId());
                    if (network.size() == 1)
                        network.clear();
                });
    }

    private void sendActionBarAlert(Player player) {
        try {
            networks.stream()
                    .filter(network -> network.contains(player.getUniqueId()))
                    .filter(network -> network.canPlayerStayConnected(player))
                    .filter(network -> !network.canPlayerBeAdded(player))
                    .forEach(network -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MinecraftLang.ACTION_BAR_ALERT.toString())));
        } catch (NoSuchMethodError ignored) {
        }
    }

    private void createNetworkIfNeeded(Player player) {
        Set<Player> alivePlayers = PlayerUtil.getOnlinePlayers().stream()
                .filter(p -> !p.isDead())
                .collect(Collectors.toSet());
        Category category = getCategory();
        Set<UUID> playersWithinRange = alivePlayers.stream()
                .filter(p -> networks.stream().noneMatch(network -> network.contains(p)))
                .filter(p -> !p.equals(player))
                .filter(p -> p.getWorld().getName().equals(player.getWorld().getName()))
                .filter(p -> getHorizontalDistance(p.getLocation(), player.getLocation()) <= getHorizontalRadius()
                        && getVerticalDistance(p.getLocation(), player.getLocation()) <= getVerticalRadius())
                .filter(p -> {
                    Member m = getMember(p.getUniqueId());
                    return m != null && m.getVoiceState() != null
                            && m.getVoiceState().getChannel() != null
                            && m.getVoiceState().getChannel().getParent() != null
                            && m.getVoiceState().getChannel().getParent().equals(category);
                })
                .map(Player::getUniqueId)
                .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet));
        if (!playersWithinRange.isEmpty() && category.getChannels().size() != 50) {
            playersWithinRange.add(player.getUniqueId());
            networks.add(new Network(playersWithinRange));
        }
    }

    private void deleteEmptyNetworks() {
        for (Network network : new HashSet<>(networks)) {
            if (network.isEmpty()) {
                VoiceChannel voiceChannel = network.getChannel();
                if (voiceChannel != null && voiceChannel.getMembers().isEmpty()) {
                    voiceChannel.delete().reason(DiscordLang.COMMUNICATION_LOST.toString()).queue();
                    networks.remove(network);
                }
            }
        }
    }
}
