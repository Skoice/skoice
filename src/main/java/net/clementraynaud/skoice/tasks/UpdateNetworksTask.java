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

import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.lang.Lang;
import net.clementraynaud.skoice.system.EligiblePlayers;
import net.clementraynaud.skoice.system.Network;
import net.clementraynaud.skoice.util.DistanceUtil;
import net.clementraynaud.skoice.util.MapUtil;
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

public class UpdateNetworksTask implements Task {

    public static final Map<String, Pair<String, CompletableFuture<Void>>> awaitingMoves = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    private final Config config;
    private final Lang lang;
    private final EligiblePlayers eligiblePlayers;

    public UpdateNetworksTask(Config config, Lang lang, EligiblePlayers eligiblePlayers) {
        this.config = config;
        this.lang = lang;
        this.eligiblePlayers = eligiblePlayers;
    }

    @Override
    public void run() {
        if (!this.lock.tryLock()) {
            return;
        }
        try {
            VoiceChannel lobby = this.config.getReader().getLobby();
            if (lobby == null) {
                return;
            }
            this.muteMembers(lobby);
            Network.networks.removeIf(network -> network.getChannel() == null && network.isInitialized());
            Set<UUID> oldEligiblePlayers = this.eligiblePlayers.copy();
            this.eligiblePlayers.clear();
            for (UUID minecraftID : oldEligiblePlayers) {
                Player player = Bukkit.getPlayer(minecraftID);
                if (player != null) {
                    Member member = this.config.getReader().getMember(player.getUniqueId());
                    if (member != null && member.getVoiceState() != null && member.getVoiceState().getChannel() != null) {
                        VoiceChannel playerChannel = member.getVoiceState().getChannel();
                        boolean isLobby = playerChannel == this.config.getReader().getLobby();
                        if (!isLobby && (playerChannel.getParent() == null || playerChannel.getParent() != this.config.getReader().getCategory())) {
                            Pair<String, CompletableFuture<Void>> pair = UpdateNetworksTask.awaitingMoves.get(member.getId());
                            if (pair != null) {
                                pair.getRight().cancel(false);
                            }
                            continue;
                        }
                        this.updateNetworksAroundPlayer(player);
                        if (this.config.getFile().getBoolean(ConfigField.ACTION_BAR_ALERT.get())) {
                            this.sendActionBarAlert(player);
                        }
                        this.createNetworkIfNeeded(player);
                    }
                }
            }
            Set<Member> membersInLobby = new HashSet<>(lobby.getMembers());
            for (Network network : Network.getNetworks()) {
                VoiceChannel voiceChannel = network.getChannel();
                if (voiceChannel == null) {
                    continue;
                }
                membersInLobby.addAll(voiceChannel.getMembers());
            }
            Map<String, String> links = new HashMap<>(this.config.getReader().getLinks());
            for (Member member : membersInLobby) {
                String minecraftID = new MapUtil().getKeyFromValue(links, member.getId());
                VoiceChannel playerChannel = member.getVoiceState().getChannel();
                Network playerNetwork = minecraftID != null ? Network.networks.stream()
                        .filter(n -> n.contains(UUID.fromString(minecraftID)))
                        .findAny().orElse(null) : null;
                VoiceChannel shouldBeInChannel;
                if (playerNetwork != null) {
                    if (playerNetwork.getChannel() == null) {
                        continue;
                    }
                    shouldBeInChannel = playerNetwork.getChannel();
                } else {
                    shouldBeInChannel = lobby;
                }
                Pair<String, CompletableFuture<Void>> awaitingMove = UpdateNetworksTask.awaitingMoves.get(member.getId());
                if (awaitingMove != null && awaitingMove.getLeft().equals(shouldBeInChannel.getId())) {
                    continue;
                }
                if (awaitingMove != null && !awaitingMove.getLeft().equals(shouldBeInChannel.getId())
                        && !awaitingMove.getRight().cancel(false)) {
                    continue;
                }
                if (playerChannel != shouldBeInChannel) {
                    UpdateNetworksTask.awaitingMoves.put(member.getId(), Pair.of(
                            shouldBeInChannel.getId(),
                            this.config.getReader().getGuild().moveVoiceMember(member, shouldBeInChannel)
                                    .submit().whenCompleteAsync((v, t) -> UpdateNetworksTask.awaitingMoves.remove(member.getId()))
                    ));
                }
            }
            this.deleteEmptyNetworks();
        } finally {
            this.lock.unlock();
        }
    }

    private void muteMembers(VoiceChannel lobby) {
        Role publicRole = lobby.getGuild().getPublicRole();
        PermissionOverride lobbyPublicRoleOverride = lobby.getPermissionOverride(publicRole);
        if (lobbyPublicRoleOverride == null) {
            lobby.createPermissionOverride(publicRole).deny(Permission.VOICE_SPEAK).queue();
        } else if (!lobbyPublicRoleOverride.getDenied().contains(Permission.VOICE_SPEAK)) {
            lobbyPublicRoleOverride.getManager().deny(Permission.VOICE_SPEAK).queue();
        }
    }

    private void updateNetworksAroundPlayer(Player player) {
        Network.networks.stream()
                .filter(network -> network.canPlayerBeAdded(player))
                .reduce((network1, network2) -> network1.size() > network2.size()
                        ? network1.engulf(network2)
                        : network2.engulf(network1))
                .filter(network -> !network.contains(player.getUniqueId()))
                .ifPresent(network -> network.add(player.getUniqueId()));
        Network.networks.stream()
                .filter(network -> network.contains(player.getUniqueId()))
                .filter(network -> !network.canPlayerStayConnected(player))
                .forEach(network -> {
                    network.remove(player.getUniqueId());
                    if (network.size() == 1) {
                        network.clear();
                    }
                });
    }

    private void sendActionBarAlert(Player player) {
        try {
            Network.networks.stream()
                    .filter(network -> network.contains(player.getUniqueId()))
                    .filter(network -> network.canPlayerStayConnected(player))
                    .filter(network -> !network.canPlayerBeAdded(player))
                    .forEach(network -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(this.lang.getMessage("minecraft.action-bar.alert"))));
        } catch (NoSuchMethodError ignored) {
        }
    }

    private void createNetworkIfNeeded(Player player) {
        DistanceUtil distanceUtil = new DistanceUtil();
        Set<Player> alivePlayers = new PlayerUtil().getOnlinePlayers().stream()
                .filter(p -> !p.isDead())
                .collect(Collectors.toSet());
        Category category = this.config.getReader().getCategory();
        Set<UUID> playersWithinRange = alivePlayers.stream()
                .filter(p -> Network.networks.stream().noneMatch(network -> network.contains(p)))
                .filter(p -> !p.equals(player))
                .filter(p -> p.getWorld().getName().equals(player.getWorld().getName()))
                .filter(p -> distanceUtil.getHorizontalDistance(p.getLocation(), player.getLocation()) <= this.config.getFile().getInt(ConfigField.HORIZONTAL_RADIUS.get())
                        && distanceUtil.getVerticalDistance(p.getLocation(), player.getLocation()) <= this.config.getFile().getInt(ConfigField.VERTICAL_RADIUS.get()))
                .filter(p -> {
                    Member m = this.config.getReader().getMember(p.getUniqueId());
                    return m != null && m.getVoiceState() != null
                            && m.getVoiceState().getChannel() != null
                            && m.getVoiceState().getChannel().getParent() != null
                            && m.getVoiceState().getChannel().getParent().equals(category);
                })
                .map(Player::getUniqueId)
                .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet));
        if (!playersWithinRange.isEmpty() && category.getChannels().size() != 50) {
            playersWithinRange.add(player.getUniqueId());
            Network network = new Network(this.config, playersWithinRange);
            network.build();
            Network.networks.add(network);
        }
    }

    private void deleteEmptyNetworks() {
        for (Network network : new HashSet<>(Network.networks)) {
            if (network.isEmpty()) {
                VoiceChannel voiceChannel = network.getChannel();
                if (voiceChannel != null && voiceChannel.getMembers().isEmpty()) {
                    voiceChannel.delete().reason(this.lang.getMessage("discord.communication-lost")).queue();
                    Network.networks.remove(network);
                }
            }
        }
    }
}
