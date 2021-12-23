// Copyright 2020, 2021 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
// Copyright 2016, 2017, 2018, 2019, 2020, 2021 Austin "Scarsz" Shapiro

// This file is part of Skoice.

// Skoice is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Skoice is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Skoice.  If not, see <https://www.gnu.org/licenses/>.


package net.clementraynaud.system;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

import static net.clementraynaud.Skoice.getPlugin;
import static net.clementraynaud.system.ChannelManagement.getNetworks;
import static net.clementraynaud.system.DistanceCalculation.horizontalDistance;
import static net.clementraynaud.system.DistanceCalculation.verticalDistance;
import static net.clementraynaud.util.DataGetters.*;

public class Network {

    private static final double FALLOFF = 2.5;

    private final Set<UUID> players;
    private String channel;
    private boolean initialized = false;

    public Network(String channel) {
        this.players = Collections.emptySet();
        this.channel = channel;
    }

    public Network(Set<UUID> players) {
        this.players = players;

//        debug(Debug.VOICE, "Network being made for " + players);

        List<Permission> allowedPermissions = isVoiceActivationAllowed()
                ? Arrays.asList(Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD)
                : Collections.singletonList(Permission.VOICE_SPEAK);
        List<Permission> publicRemovedPermissions = Arrays.asList(Permission.VIEW_CHANNEL, Permission.VOICE_MOVE_OTHERS);
        if (getPlugin().getConfig().getBoolean("show-channels")) {
            publicRemovedPermissions.remove(Permission.VIEW_CHANNEL);
        }
        getDedicatedCategory().createVoiceChannel(UUID.randomUUID().toString())
                .addPermissionOverride(
                        getGuild().getPublicRole(),
                        allowedPermissions,
                        publicRemovedPermissions
                )
                .addPermissionOverride(
                        getGuild().getSelfMember(),
                        Arrays.asList(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_MOVE_OTHERS),
                        Collections.emptyList()
                )
                .queue(channel -> {
                    this.channel = channel.getId();
                    initialized = true;
                }, e -> {
//                    error("Failed to create network for " + players + ": " + e.getMessage());
                    getNetworks().remove(this);
                });
    }

    public static boolean isVoiceActivationAllowed() {
        return true;
    }

    public Network engulf(Network network) {
//        debug(Debug.VOICE, "Network " + this + " is engulfing " + network);
        players.addAll(network.players);
        network.players.clear();
        return this;
    }


    /**
     * @return true if the player is within the network strength or falloff ranges
     */
    public boolean isPlayerInRangeToBeAdded(Player player) {
        return players.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(p -> !p.equals(player))
                .filter(p -> p.getWorld().getName().equals(player.getWorld().getName()))
                .anyMatch(p -> verticalDistance(p.getLocation(), player.getLocation()) <= getVerticalRadius()
                        && horizontalDistance(p.getLocation(), player.getLocation()) <= getHorizontalRadius());
    }

    /**
     * @return true if the player is within the network strength and should be connected
     */
    public boolean isPlayerInRangeToStayConnected(Player player) {
        return players.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(p -> !p.equals(player))
                .filter(p -> p.getWorld().getName().equals(player.getWorld().getName()))
                .anyMatch(p -> verticalDistance(p.getLocation(), player.getLocation()) <= getVerticalRadius() + FALLOFF
                        && horizontalDistance(p.getLocation(), player.getLocation()) <= getHorizontalRadius() + FALLOFF);
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
        return getGuild().getVoiceChannelById(channel);
    }

    public boolean isInitialized() {
        return initialized;
    }
}
