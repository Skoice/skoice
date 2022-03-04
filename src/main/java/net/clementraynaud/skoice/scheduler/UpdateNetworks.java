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

package net.clementraynaud.skoice.scheduler;

import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.networks.NetworkManager;
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

import static net.clementraynaud.skoice.events.player.DirtyPlayerEvents.*;
import static net.clementraynaud.skoice.networks.NetworkManager.*;
import static net.clementraynaud.skoice.util.DistanceUtil.getHorizontalDistance;
import static net.clementraynaud.skoice.util.DistanceUtil.getVerticalDistance;
import static net.clementraynaud.skoice.config.Config.*;

public class UpdateNetworks {

    private static final List<Permission> LOBBY_REQUIRED_PERMISSIONS = Arrays.asList(Permission.VIEW_CHANNEL, Permission.VOICE_MOVE_OTHERS);
    private static final List<Permission> CATEGORY_REQUIRED_PERMISSIONS = Arrays.asList(Permission.VIEW_CHANNEL, Permission.VOICE_MOVE_OTHERS, Permission.MANAGE_PERMISSIONS, Permission.MANAGE_CHANNEL);
    public static final Map<String, Pair<String, CompletableFuture<Void>>> awaitingMoves = new ConcurrentHashMap<>();
    private static final ReentrantLock lock = new ReentrantLock();

    public void run() {
        if (!lock.tryLock()) {
            return;
        }
        try {
            VoiceChannel lobby = getLobby();
            if (lobby == null) {
                return;
            }
            Member selfMember = lobby.getGuild().getSelfMember();
            Role publicRole = lobby.getGuild().getPublicRole();
            for (Permission permission : LOBBY_REQUIRED_PERMISSIONS) {
                if (!selfMember.hasPermission(lobby, permission)) {
//                  "The bot doesn't have the \"" + permission.getName() + "\" permission in the voice lobby (" + lobby.getName() + ")"
                    return;
                }
            }
            Category category = getCategory();
            for (Permission permission : CATEGORY_REQUIRED_PERMISSIONS) {
                if (!selfMember.hasPermission(category, permission)) {
//                  "The bot doesn't have the \"" + permission.getName() + "\" permission in the voice category (" + category.getName() + ")"
                    return;
                }
            }
            PermissionOverride mainVoiceChannelPublicRoleOverride = lobby.getPermissionOverride(publicRole);
            if (mainVoiceChannelPublicRoleOverride == null) {
                lobby.createPermissionOverride(publicRole).deny(Permission.VOICE_SPEAK).queue();
            } else if (!mainVoiceChannelPublicRoleOverride.getDenied().contains(Permission.VOICE_SPEAK)) {
                mainVoiceChannelPublicRoleOverride.getManager().deny(Permission.VOICE_SPEAK).queue();
            }
            // remove networks that have no voice channel
            networks.removeIf(network -> network.getChannel() == null && network.isInitialized());
            Set<Player> alivePlayers = PlayerUtil.getOnlinePlayers().stream()
                    .filter(player -> !player.isDead())
                    .collect(Collectors.toSet());
            Set<UUID> oldDirtyPlayers = getDirtyPlayers();
            clearDirtyPlayers();
            for (UUID minecraftID : oldDirtyPlayers) {
                Player player = Bukkit.getPlayer(minecraftID);
                if (player == null) continue;
                Member member = getMember(player.getUniqueId());
                if (member == null) continue;
                if (member.getVoiceState() == null || member.getVoiceState().getChannel() == null) continue;
                VoiceChannel playerChannel = member.getVoiceState().getChannel();
                boolean isLobby = playerChannel.getId().equals(getLobby().getId());
                if (!isLobby && (playerChannel.getParent() == null || !playerChannel.getParent().getId().equals(getCategory().getId()))) {
//                    .debug(Debug.VOICE, "Player " + player.getName() + " was not in the voice lobby or category");
                    //member.mute(false).queue();
                    // cancel existing moves if they changed to a different channel
                    Pair<String, CompletableFuture<Void>> pair = awaitingMoves.get(member.getId());
                    if (pair != null) pair.getRight().cancel(false);
                    continue;
                }
                networks.stream()
                        .filter(network -> network.isPlayerInRangeToBeAdded(player))
                        .reduce((network1, network2) -> network1.size() > network2.size() ? network1.engulf(network2) : network2.engulf(network1))
                        .filter(network -> !network.contains(player.getUniqueId()))
                        .ifPresent(network -> network.add(player.getUniqueId()));
                networks.stream()
                        .filter(network -> network.contains(player.getUniqueId()))
                        .filter(network -> !network.isPlayerInRangeToStayConnected(player))
                        .forEach(network -> {
                            network.remove(player.getUniqueId());
                            if (network.size() == 1) network.clear();
                        });
                if (getActionBarAlert()) {
                    try {
                        networks.stream()
                                .filter(network -> network.contains(player.getUniqueId()))
                                .filter(network -> network.isPlayerInRangeToStayConnected(player))
                                .filter(network -> !network.isPlayerInRangeToBeAdded(player))
                                .forEach(network -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MinecraftLang.ACTION_BAR_ALERT.toString())));
                    } catch (NoSuchMethodError ignored) {
                    }
                }

                // create networks if two players are within activation distance
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
                if (!playersWithinRange.isEmpty()) {
                    if (category.getChannels().size() == 50) {
//                        .debug(Debug.VOICE, "Can't create new voice network because category " + category.getName() + " is full of channels");
                        continue;
                    }
                    playersWithinRange.add(minecraftID);
                    networks.add(new NetworkManager(playersWithinRange));
                }
            }
            // handle moving players between channels
            Set<Member> members = new HashSet<>(lobby.getMembers());
            for (NetworkManager network : getNetworks()) {
                VoiceChannel voiceChannel = network.getChannel();
                if (voiceChannel == null) continue;
                members.addAll(voiceChannel.getMembers());
            }
            for (Member member : members) {
                String minecraftID = getKeyFromValue(getLinkMap(), member.getId());
                VoiceChannel playerChannel = member.getVoiceState().getChannel();
                NetworkManager playerNetwork = minecraftID != null ? networks.stream()
                        .filter(n -> n.contains(UUID.fromString(minecraftID)))
                        .findAny().orElse(null) : null;
                VoiceChannel shouldBeInChannel;
                if (playerNetwork != null) {
                    if (playerNetwork.getChannel() == null) {
                        // isn't yet created, we can wait until next tick
                        continue;
                    }
                    shouldBeInChannel = playerNetwork.getChannel();
                } else {
                    shouldBeInChannel = lobby;
                }
                Pair<String, CompletableFuture<Void>> awaitingMove = awaitingMoves.get(member.getId());
                // they're already where they're suppose to be
                if (awaitingMove != null && awaitingMove.getLeft().equals(shouldBeInChannel.getId())) continue;
                // if the cancel succeeded we can move them
                if (awaitingMove != null && !awaitingMove.getLeft().equals(shouldBeInChannel.getId())
                        && !awaitingMove.getRight().cancel(false)) continue;
                // schedule a move to the channel they're suppose to be in, if they aren't there yet
                if (!playerChannel.getId().equals(shouldBeInChannel.getId())) {
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

    private void deleteEmptyNetworks() {
        for (NetworkManager network : new HashSet<>(networks)) {
            if (!network.isEmpty()) continue;
            VoiceChannel voiceChannel = network.getChannel();
            if (voiceChannel == null) continue;
            if (voiceChannel.getMembers().isEmpty()) {
                voiceChannel.delete().reason(DiscordLang.COMMUNICATION_LOST.toString()).queue();
                networks.remove(network);
            }
        }
    }
}
