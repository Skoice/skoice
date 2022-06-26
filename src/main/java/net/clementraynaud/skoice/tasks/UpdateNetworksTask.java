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

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.config.ConfigurationField;
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

public class UpdateNetworksTask {

    private static final Set<UUID> eligiblePlayers = new HashSet<>();
    private static final Map<String, Pair<String, CompletableFuture<Void>>> awaitingMoves = new ConcurrentHashMap<>();

    private final ReentrantLock lock = new ReentrantLock();

    private final Skoice plugin;

    public UpdateNetworksTask(Skoice plugin) {
        this.plugin = plugin;
    }

    public void run() {
        if (!this.lock.tryLock()) {
            return;
        }
        try {
            VoiceChannel mainVoiceChannel = this.plugin.getConfiguration().getVoiceChannel();
            if (mainVoiceChannel == null) {
                return;
            }
            this.muteMembers(mainVoiceChannel);
            Network.getNetworks().removeIf(network -> network.getChannel() == null && network.isInitialized());
            Set<UUID> oldEligiblePlayers = new HashSet<>(UpdateNetworksTask.eligiblePlayers);
            UpdateNetworksTask.eligiblePlayers.clear();
            for (UUID minecraftId : oldEligiblePlayers) {
                Player player = Bukkit.getPlayer(minecraftId);
                if (player != null) {
                    Member member = this.plugin.getLinksFileStorage().getMember(player.getUniqueId());
                    if (member != null && member.getVoiceState() != null && member.getVoiceState().getChannel() != null) {
                        AudioChannel audioChannel = member.getVoiceState().getChannel();
                        if (audioChannel instanceof VoiceChannel) {
                            VoiceChannel voiceChannel = (VoiceChannel) audioChannel;
                            boolean isMainVoiceChannel = voiceChannel == mainVoiceChannel;
                            if (!isMainVoiceChannel && (voiceChannel.getParentCategory() == null
                                    || voiceChannel.getParentCategory() != this.plugin.getConfiguration().getCategory())) {
                                Pair<String, CompletableFuture<Void>> pair = UpdateNetworksTask.awaitingMoves.get(member.getId());
                                if (pair != null) {
                                    pair.getRight().cancel(false);
                                }
                                continue;
                            }
                        }
                        this.updateNetworksAroundPlayer(player);
                        if (this.plugin.getConfiguration().getFile().getBoolean(ConfigurationField.ACTION_BAR_ALERT.toString())) {
                            this.sendActionBarAlert(player);
                        }
                        this.createNetworkIfNeeded(player);
                    }
                }
            }
            Set<Member> members = new HashSet<>(mainVoiceChannel.getMembers());
            for (Network network : Network.getNetworks()) {
                VoiceChannel voiceChannel = network.getChannel();
                if (voiceChannel == null) {
                    continue;
                }
                members.addAll(voiceChannel.getMembers());
            }
            Map<String, String> links = new HashMap<>(this.plugin.getLinksFileStorage().getLinks());
            for (Member member : members) {
                String minecraftId = MapUtil.getKeyFromValue(links, member.getId());
                Network playerNetwork = minecraftId != null ? Network.getNetworks().stream()
                        .filter(n -> n.contains(UUID.fromString(minecraftId)))
                        .findAny().orElse(null) : null;
                VoiceChannel shouldBeInChannel;
                if (playerNetwork != null) {
                    if (playerNetwork.getChannel() == null) {
                        continue;
                    }
                    shouldBeInChannel = playerNetwork.getChannel();
                } else {
                    shouldBeInChannel = mainVoiceChannel;
                }
                Pair<String, CompletableFuture<Void>> awaitingMove = UpdateNetworksTask.awaitingMoves.get(member.getId());
                if (awaitingMove != null && awaitingMove.getLeft().equals(shouldBeInChannel.getId())) {
                    continue;
                }
                if (awaitingMove != null && !awaitingMove.getLeft().equals(shouldBeInChannel.getId())
                        && !awaitingMove.getRight().cancel(false)) {
                    continue;
                }
                if (member.getVoiceState().getChannel() != shouldBeInChannel) {
                    UpdateNetworksTask.awaitingMoves.put(member.getId(), Pair.of(
                            shouldBeInChannel.getId(),
                            this.plugin.getConfiguration().getGuild().moveVoiceMember(member, shouldBeInChannel)
                                    .submit().whenCompleteAsync((v, t) -> UpdateNetworksTask.awaitingMoves.remove(member.getId()))
                    ));
                }
            }
            this.deleteEmptyNetworks();
        } finally {
            this.lock.unlock();
        }
    }

    private void muteMembers(VoiceChannel voiceChannel) {
        Role publicRole = voiceChannel.getGuild().getPublicRole();
        PermissionOverride permissionOverride = voiceChannel.getPermissionOverride(publicRole);
        if (permissionOverride == null) {
            voiceChannel.upsertPermissionOverride(publicRole).deny(Permission.VOICE_SPEAK).queue();
        } else if (!permissionOverride.getDenied().contains(Permission.VOICE_SPEAK)) {
            permissionOverride.getManager().deny(Permission.VOICE_SPEAK).queue();
        }
    }

    private void updateNetworksAroundPlayer(Player player) {
        Network.getNetworks().stream()
                .filter(network -> network.canPlayerBeAdded(player))
                .reduce((network1, network2) -> network1.size() > network2.size()
                        ? network1.engulf(network2)
                        : network2.engulf(network1))
                .filter(network -> !network.contains(player.getUniqueId()))
                .ifPresent(network -> network.add(player.getUniqueId()));
        Network.getNetworks().stream()
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
            Network.getNetworks().stream()
                    .filter(network -> network.contains(player.getUniqueId()))
                    .filter(network -> network.canPlayerStayConnected(player))
                    .filter(network -> !network.canPlayerBeAdded(player))
                    .forEach(network -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent(this.plugin.getLang().getMessage("minecraft.action-bar.alert"))));
        } catch (NoSuchMethodError ignored) {
        }
    }

    private void createNetworkIfNeeded(Player player) {
        Set<Player> alivePlayers = PlayerUtil.getOnlinePlayers().stream()
                .filter(p -> !p.isDead())
                .collect(Collectors.toSet());
        Category category = this.plugin.getConfiguration().getCategory();
        Set<UUID> playersWithinRange = alivePlayers.stream()
                .filter(p -> Network.getNetworks().stream().noneMatch(network -> network.contains(p)))
                .filter(p -> !p.equals(player))
                .filter(p -> p.getWorld().getName().equals(player.getWorld().getName()))
                .filter(p -> DistanceUtil.getHorizontalDistance(p.getLocation(),
                        player.getLocation()) <= this.plugin.getConfiguration().getFile().getInt(ConfigurationField.HORIZONTAL_RADIUS.toString())
                        && DistanceUtil.getVerticalDistance(p.getLocation(),
                        player.getLocation()) <= this.plugin.getConfiguration().getFile().getInt(ConfigurationField.VERTICAL_RADIUS.toString()))
                .filter(p -> {
                    Member member = this.plugin.getLinksFileStorage().getMember(p.getUniqueId());
                    return member != null && member.getVoiceState() != null
                            && member.getVoiceState().getChannel() instanceof VoiceChannel
                            && ((VoiceChannel) member.getVoiceState().getChannel()).getParentCategory() != null
                            && ((VoiceChannel) member.getVoiceState().getChannel()).getParentCategory().equals(category);
                })
                .map(Player::getUniqueId)
                .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet));
        if (!playersWithinRange.isEmpty() && category.getChannels().size() != 50) {
            playersWithinRange.add(player.getUniqueId());
            Network network = new Network(this.plugin.getConfiguration(), playersWithinRange);
            network.build();
            Network.getNetworks().add(network);
        }
    }

    private void deleteEmptyNetworks() {
        for (Network network : new HashSet<>(Network.getNetworks())) {
            if (network.isEmpty()) {
                VoiceChannel voiceChannel = network.getChannel();
                if (voiceChannel != null && voiceChannel.getMembers().isEmpty()) {
                    voiceChannel.delete().reason(this.plugin.getLang().getMessage("discord.communication-lost")).queue();
                    Network.getNetworks().remove(network);
                }
            }
        }
    }

    public static Set<UUID> getEligiblePlayers() {
        return UpdateNetworksTask.eligiblePlayers;
    }

    public static Map<String, Pair<String, CompletableFuture<Void>>> getAwaitingMoves() {
        return UpdateNetworksTask.awaitingMoves;
    }
}
