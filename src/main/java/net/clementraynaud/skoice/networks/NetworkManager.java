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

package net.clementraynaud.skoice.networks;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.config.Config.*;
import static net.clementraynaud.skoice.util.DistanceUtil.*;

public class NetworkManager {

    private static final double FALLOFF = 2.5;

    public static final Set<NetworkManager> networks = ConcurrentHashMap.newKeySet();
    public static final Set<String> mutedUsers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> players;
    private String channel;
    private boolean initialized = false;

    public NetworkManager(String channel) {
        this.players = Collections.emptySet();
        this.channel = channel;
    }

    public static Set<NetworkManager> getNetworks() {
        return networks;
    }

    public NetworkManager(Set<UUID> players) {
        this.players = players;
        List<Permission> allowedPermissions = isVoiceActivationAllowed()
                ? Arrays.asList(Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD)
                : Collections.singletonList(Permission.VOICE_SPEAK);
        List<Permission> deniedPermissions = getPlugin().getConfig().getBoolean(CHANNEL_VISIBILITY_FIELD)
                ? Arrays.asList(Permission.VOICE_CONNECT, Permission.VOICE_MOVE_OTHERS)
                : Arrays.asList(Permission.VIEW_CHANNEL, Permission.VOICE_MOVE_OTHERS);
        getCategory().createVoiceChannel(UUID.randomUUID().toString())
                .addPermissionOverride(
                        getGuild().getPublicRole(),
                        allowedPermissions,
                        deniedPermissions
                )
                .addPermissionOverride(
                        getGuild().getSelfMember(),
                        Arrays.asList(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_MOVE_OTHERS),
                        Collections.emptyList()
                )
                .queue(channel -> {
                    this.channel = channel.getId();
                    initialized = true;
                }, e -> getNetworks().remove(this));
    }

    public static boolean isVoiceActivationAllowed() {
        return true;
    }

    public static void updateMutedUsers(VoiceChannel channel, Member member) {
        if (channel == null || member.getVoiceState() == null || getLobby() == null) {
            return;
        }
        boolean isLobby = channel.getId().equals(getLobby().getId());
        if (isLobby && !member.getVoiceState().isGuildMuted()) {
            PermissionOverride override = channel.getPermissionOverride(channel.getGuild().getPublicRole());
            if (override != null && override.getDenied().contains(Permission.VOICE_SPEAK)
                    && member.hasPermission(channel, Permission.VOICE_SPEAK, Permission.VOICE_MUTE_OTHERS)
                    && channel.getGuild().getSelfMember().hasPermission(channel, Permission.VOICE_MUTE_OTHERS)
                    && channel.getGuild().getSelfMember().hasPermission(getCategory(), Permission.VOICE_MOVE_OTHERS)) {
                member.mute(true).queue();
                mutedUsers.add(member.getId());
            }
        } else if (!isLobby && mutedUsers.remove(member.getId())) {
            member.mute(false).queue();
        }
    }

    public NetworkManager engulf(NetworkManager network) {
        players.addAll(network.players);
        network.players.clear();
        return this;
    }

    public boolean isPlayerInRangeToBeAdded(Player player) {
        return players.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(p -> !p.equals(player))
                .filter(p -> p.getWorld().getName().equals(player.getWorld().getName()))
                .anyMatch(p -> getVerticalDistance(p.getLocation(), player.getLocation()) <= getVerticalRadius()
                        && getHorizontalDistance(p.getLocation(), player.getLocation()) <= getHorizontalRadius());
    }

    public boolean isPlayerInRangeToStayConnected(Player player) {
        List<Player> matches = Arrays.asList(players.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(p -> p.getWorld().getName().equals(player.getWorld().getName()))
                .filter(p -> getVerticalDistance(p.getLocation(), player.getLocation()) <= getVerticalRadius() + FALLOFF
                        && getHorizontalDistance(p.getLocation(), player.getLocation()) <= getHorizontalRadius() + FALLOFF)
                .toArray(Player[]::new));
        if (players.size() > matches.size()) {
            Player[] otherPlayers = players.stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .filter(p -> !matches.contains(p))
                    .toArray(Player[]::new);
            for (Player otherPlayer : otherPlayers)
                if (matches.stream()
                        .anyMatch(p -> getVerticalDistance(p.getLocation(), otherPlayer.getLocation()) <= getVerticalRadius() + FALLOFF
                        && getHorizontalDistance(p.getLocation(), otherPlayer.getLocation()) <= getHorizontalRadius() + FALLOFF))
                    return true;
            return false;
        }
        return matches.size() != 1;
    }

    public void clear() {
        players.clear();
    }

    public void add(UUID uuid) {
        players.add(uuid);
    }

    public void remove(Player player) {
        players.remove(player.getUniqueId());
    }

    public void remove(UUID uuid) {
        players.remove(uuid);
    }

    public boolean contains(Player player) {
        return players.contains(player.getUniqueId());
    }

    public boolean contains(UUID uuid) {
        return players.contains(uuid);
    }

    public int size() {
        return players.size();
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    public VoiceChannel getChannel() {
        if (channel == null || channel.isEmpty()) return null;
        Guild guild = getGuild();
        if (guild != null) {
            return guild.getVoiceChannelById(channel);
        }
        return null;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
