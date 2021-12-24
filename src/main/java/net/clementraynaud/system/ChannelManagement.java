/*
 * Copyright 2020, 2021 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.system;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static net.clementraynaud.Skoice.getPlugin;
import static net.clementraynaud.system.DistanceCalculation.horizontalDistance;
import static net.clementraynaud.system.DistanceCalculation.verticalDistance;
import static net.clementraynaud.system.MarkPlayersDirty.clearDirtyPlayers;
import static net.clementraynaud.system.MarkPlayersDirty.getDirtyPlayers;
import static net.clementraynaud.util.DataGetters.*;

public class ChannelManagement extends ListenerAdapter implements Listener {

    public static final Set<Network> networks = ConcurrentHashMap.newKeySet();
    public static final Map<String, Pair<String, CompletableFuture<Void>>> awaitingMoves = new ConcurrentHashMap<>();
    private static final List<Permission> LOBBY_REQUIRED_PERMISSIONS = Arrays.asList(Permission.VIEW_CHANNEL, Permission.VOICE_MOVE_OTHERS);
    private static final List<Permission> CATEGORY_REQUIRED_PERMISSIONS = Arrays.asList(Permission.VIEW_CHANNEL, Permission.VOICE_MOVE_OTHERS, Permission.MANAGE_PERMISSIONS, Permission.MANAGE_CHANNEL);
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Set<String> mutedUsers = ConcurrentHashMap.newKeySet();

    public static Set<Network> getNetworks() {
        return networks;
    }

    public static void tick() {
        if (!lock.tryLock()) {
//            debug(Debug.VOICE, "Skipping voice module tick, a tick is already in progress");
            return;
        }
        try {
            Category category = getDedicatedCategory();
            if (category == null) {
//                debug(Debug.VOICE, "Skipping voice module tick, category is null");
                return;
            }

            VoiceChannel lobby = getLobby();
            if (lobby == null) {
//                debug(Debug.VOICE, "Skipping voice module tick, lobby channel is null");
                return;
            }
            // check that the permissions are correct
            Member selfMember = lobby.getGuild().getSelfMember();
            Role publicRole = lobby.getGuild().getPublicRole();
            boolean stop = false;
            for (Permission permission : LOBBY_REQUIRED_PERMISSIONS) {
                if (!selfMember.hasPermission(lobby, permission)) {
//                    if (log) .error("The bot doesn't have the \"" + permission.getName() + "\" permission in the voice lobby (" + lobby.getName() + ")");
                    stop = true;
                }
            }
            for (Permission permission : CATEGORY_REQUIRED_PERMISSIONS) {
                if (!selfMember.hasPermission(category, permission)) {
//                    if (log) .error("The bot doesn't have the \"" + permission.getName() + "\" permission in the voice category (" + category.getName() + ")");
                    stop = true;
                }
            }
            // we can't function & would throw exceptions
            if (stop) {
                return;
            }
            PermissionOverride mainVoiceChannelPublicRoleOverride = lobby.getPermissionOverride(publicRole);
            if (mainVoiceChannelPublicRoleOverride == null) {
                lobby.createPermissionOverride(publicRole).deny(Permission.VOICE_SPEAK).queue();
            } else if (!mainVoiceChannelPublicRoleOverride.getDenied().contains(Permission.VOICE_SPEAK)) {
                mainVoiceChannelPublicRoleOverride.getManager().deny(Permission.VOICE_SPEAK).queue();
            }
            // remove networks that have no voice channel
            networks.removeIf(network -> network.getChannel() == null && network.isInitialized());
            Set<Player> alivePlayers = getOnlinePlayers().stream()
                    .filter(player -> !player.isDead())
                    .collect(Collectors.toSet());
            Set<UUID> oldDirtyPlayers = getDirtyPlayers();
            clearDirtyPlayers();
            for (UUID minecraftID : oldDirtyPlayers) {
                Player player = Bukkit.getPlayer(minecraftID);
                if (player == null) continue;
                Member member = getMember(player.getUniqueId());
                if (member == null) {
//                   debug(Debug.VOICE, "Player " + player.getName() + " isn't linked, skipping voice checks");
                    continue;
                }
                if (member.getVoiceState() == null || member.getVoiceState().getChannel() == null) {
//                    .debug(Debug.VOICE, "Player " + player.getName() + " is not connected to voice");
                    continue;
                }
                VoiceChannel playerChannel = member.getVoiceState().getChannel();
                boolean isLobby = playerChannel.getId().equals(getLobby().getId());
                if (!isLobby && (playerChannel.getParent() == null || !playerChannel.getParent().getId().equals(getDedicatedCategory().getId()))) {
//                    .debug(Debug.VOICE, "Player " + player.getName() + " was not in the voice lobby or category");
                    //member.mute(false).queue();
                    // cancel existing moves if they changed to a different channel
                    Pair<String, CompletableFuture<Void>> pair = awaitingMoves.get(member.getId());
                    if (pair != null) pair.getRight().cancel(false);
                    continue;
                }
                // add player to networks that they may have came into contact with
                // and combine multiple networks if the player is connecting them together
                networks.stream()
                        .filter(network -> network.isPlayerInRangeToBeAdded(player))
                        // combine multiple networks if player is bridging both of them together
                        .reduce((network1, network2) -> network1.size() > network2.size() ? network1.engulf(network2) : network2.engulf(network1))
                        // add the player to the network if they aren't in it already
                        .filter(network -> !network.contains(player.getUniqueId()))
                        .ifPresent(network -> {
//                            debug(Debug.VOICE, player.getName() + " has entered network " + network + "'s influence, connecting");
                            network.add(player.getUniqueId());
                        });
                // remove player from networks that they lost connection to
                networks.stream()
                        .filter(network -> network.contains(player.getUniqueId()))
                        .filter(network -> !network.isPlayerInRangeToStayConnected(player))
                        .forEach(network -> {
//                            .debug(Debug.VOICE, "Player " + player.getName() + " lost connection to " + network + ", disconnecting");
                            network.remove(player.getUniqueId());
                            if (network.size() == 1) network.clear();
                        });
                if (getActionBarAlert()) {
                    try {
                        networks.stream()
                                .filter(network -> network.contains(player.getUniqueId()))
                                .filter(network -> network.isPlayerInRangeToStayConnected(player))
                                .filter(network -> !network.isPlayerInRangeToBeAdded(player))
                                .forEach(network -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§c⚠ §7You are §cmoving away §7and are soon to be §cdisconnected §7from your current channel.")));
                    } catch (NoSuchMethodError ignored) {
                    }
                }

                // create networks if two players are within activation distance
                Set<UUID> playersWithinRange = alivePlayers.stream()
                        .filter(p -> networks.stream().noneMatch(network -> network.contains(p)))
                        .filter(p -> !p.equals(player))
                        .filter(p -> p.getWorld().getName().equals(player.getWorld().getName()))
                        .filter(p -> horizontalDistance(p.getLocation(), player.getLocation()) <= getHorizontalRadius()
                                && verticalDistance(p.getLocation(), player.getLocation()) <= getVerticalRadius())
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
                    networks.add(new Network(playersWithinRange));
                }
            }
            // handle moving players between channels
            Set<Member> members = new HashSet<>(lobby.getMembers());
            for (Network network : getNetworks()) {
                VoiceChannel voiceChannel = network.getChannel();
                if (voiceChannel == null) continue;
                members.addAll(voiceChannel.getMembers());
            }
            for (Member member : members) {
                UUID uuid = getMinecraftID(member);
                VoiceChannel playerChannel = member.getVoiceState().getChannel();
                Network playerNetwork = uuid != null ? networks.stream()
                        .filter(n -> n.contains(uuid))
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
            // delete empty networks
            for (Network network : new HashSet<>(networks)) {
                if (!network.isEmpty()) continue;
                VoiceChannel voiceChannel = network.getChannel();
                if (voiceChannel == null) continue;
                if (voiceChannel.getMembers().isEmpty()) {
                    voiceChannel.delete().reason("Lost communication").queue();
                    networks.remove(network);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public static void refreshMutedUsers(VoiceChannel channel, Member member) {
        if (channel == null || member.getVoiceState() == null || getLobby() == null || getDedicatedCategory() == null) {
            return;
        }
        boolean isLobby = channel.getId().equals(getLobby().getId());
        if (isLobby && !member.getVoiceState().isGuildMuted()) {
            PermissionOverride override = channel.getPermissionOverride(channel.getGuild().getPublicRole());
            if (override != null && override.getDenied().contains(Permission.VOICE_SPEAK)
                    && member.hasPermission(channel, Permission.VOICE_SPEAK, Permission.VOICE_MUTE_OTHERS)
                    && channel.getGuild().getSelfMember().hasPermission(channel, Permission.VOICE_MUTE_OTHERS)
                    && channel.getGuild().getSelfMember().hasPermission(getDedicatedCategory(), Permission.VOICE_MOVE_OTHERS)) {
                member.mute(true).queue();
                mutedUsers.add(member.getId());
            }
        } else if (!isLobby && mutedUsers.remove(member.getId())) {
            member.mute(false).queue();
        }
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        if (event.getChannelJoined().getParent() != null && !event.getChannelJoined().getParent().equals(getDedicatedCategory()) &&
                event.getChannelLeft().getParent() != null && event.getChannelLeft().getParent().equals(getDedicatedCategory())) {
            UUID minecraftID = getMinecraftID(event.getMember());
            if (minecraftID == null) return;
            OfflinePlayer player = Bukkit.getOfflinePlayer(minecraftID);
            if (player.isOnline()) {
                networks.stream()
                        .filter(network -> network.contains(player.getPlayer().getUniqueId()))
                        .forEach(network -> network.remove(player.getPlayer()));
            }
        }
        refreshMutedUsers(event.getChannelJoined(), event.getMember());
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        refreshMutedUsers(event.getChannelJoined(), event.getMember());
        if (event.getChannelLeft().getParent() == null || !event.getChannelLeft().getParent().equals(getDedicatedCategory()))
            return;
        UUID minecraftID = getMinecraftID(event.getMember());
        if (minecraftID == null) return;
        OfflinePlayer player = Bukkit.getOfflinePlayer(minecraftID);
        if (player.isOnline()) {
            networks.stream()
                    .filter(network -> network.contains(player.getPlayer()))
                    .forEach(network -> network.remove(player.getPlayer()));
            if (event.getChannelLeft().equals(getLobby()) || networks.stream().anyMatch(network -> network.getChannel().equals(event.getChannelLeft()))) {
                player.getPlayer().sendMessage("§dSkoice §8• §7You are §cnow disconnected §7from the proximity voice chat.");
            }
        }
    }

    @Override
    public void onVoiceChannelDelete(@NotNull VoiceChannelDeleteEvent event) {
        networks.removeIf(network -> network.getChannel() != null && event.getChannel().getId().equals(network.getChannel().getId()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> networks.stream()
                .filter(network -> network.contains(event.getPlayer().getUniqueId()))
                .forEach(network -> network.remove(event.getPlayer().getUniqueId())));
    }
}
