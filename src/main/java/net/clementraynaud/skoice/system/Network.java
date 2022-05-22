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

package net.clementraynaud.skoice.system;

import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.util.DistanceUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Network {

    private static final double FALLOFF = 2.5;

    public static final Set<Network> networks = ConcurrentHashMap.newKeySet();

    private boolean initialized = false;

    private final Config config;
    private final Set<UUID> players;
    private String channel;

    public Network(Config config, String channel) {
        this.config = config;
        this.players = Collections.emptySet();
        this.channel = channel;
    }

    public Network(Config config, Set<UUID> players) {
        this.config = config;
        this.players = players;
    }

    public void build() {
        Guild guild = this.config.getGuild();
        List<Permission> deniedPermissions = this.config.getFile().getBoolean(ConfigField.CHANNEL_VISIBILITY.get())
                ? Arrays.asList(Permission.VOICE_CONNECT, Permission.VOICE_MOVE_OTHERS)
                : Arrays.asList(Permission.VIEW_CHANNEL, Permission.VOICE_MOVE_OTHERS);
        this.config.getCategory().createVoiceChannel(UUID.randomUUID().toString())
                .addPermissionOverride(guild.getPublicRole(),
                        Arrays.asList(Permission.VOICE_SPEAK, Permission.VOICE_USE_VAD),
                        deniedPermissions)
                .addPermissionOverride(guild.getSelfMember(),
                        Arrays.asList(Permission.VIEW_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_MOVE_OTHERS),
                        Collections.emptyList())
                .setBitrate(guild.getMaxBitrate())
                .queue(channel -> {
                    this.channel = channel.getId();
                    this.initialized = true;
                }, e -> Network.getNetworks().remove(this));
    }

    public boolean canPlayerBeAdded(Player player) {
        DistanceUtil distanceUtil = new DistanceUtil();
        return this.players.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(p -> !p.equals(player))
                .filter(p -> p.getWorld().getName().equals(player.getWorld().getName()))
                .anyMatch(p -> distanceUtil.getVerticalDistance(p.getLocation(), player.getLocation()) <= this.config.getFile()
                        .getInt(ConfigField.VERTICAL_RADIUS.get())
                        && distanceUtil.getHorizontalDistance(p.getLocation(), player.getLocation()) <= this.config.getFile()
                        .getInt(ConfigField.HORIZONTAL_RADIUS.get()));
    }

    public boolean canPlayerStayConnected(Player player) {
        DistanceUtil distanceUtil = new DistanceUtil();
        List<Player> matches = Arrays.asList(this.players.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(p -> p.getWorld().getName().equals(player.getWorld().getName()))
                .filter(p -> distanceUtil.getVerticalDistance(p.getLocation(), player.getLocation()) <= this.config.getFile()
                        .getInt(ConfigField.VERTICAL_RADIUS.get()) + Network.FALLOFF
                        && distanceUtil.getHorizontalDistance(p.getLocation(), player.getLocation()) <= this.config.getFile()
                        .getInt(ConfigField.HORIZONTAL_RADIUS.get()) + Network.FALLOFF)
                .toArray(Player[]::new));
        if (this.players.size() > matches.size()) {
            Player[] otherPlayers = this.players.stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .filter(p -> !matches.contains(p))
                    .toArray(Player[]::new);
            for (Player otherPlayer : otherPlayers) {
                if (matches.stream()
                        .anyMatch(p -> distanceUtil.getVerticalDistance(p.getLocation(), otherPlayer.getLocation()) <= this.config.getFile()
                                .getInt(ConfigField.VERTICAL_RADIUS.get()) + Network.FALLOFF
                                && distanceUtil.getHorizontalDistance(p.getLocation(), otherPlayer.getLocation()) <= this.config.getFile()
                                .getInt(ConfigField.HORIZONTAL_RADIUS.get()) + Network.FALLOFF)) {
                    return true;
                }
            }
            return false;
        }
        return matches.size() != 1;
    }

    public Network engulf(Network network) {
        this.players.addAll(network.players);
        network.players.clear();
        return this;
    }

    public void clear() {
        this.players.clear();
    }

    public void add(UUID uuid) {
        this.players.add(uuid);
    }

    public void remove(Player player) {
        this.players.remove(player.getUniqueId());
    }

    public void remove(UUID uuid) {
        this.players.remove(uuid);
    }

    public boolean contains(Player player) {
        return this.players.contains(player.getUniqueId());
    }

    public boolean contains(UUID uuid) {
        return this.players.contains(uuid);
    }

    public int size() {
        return this.players.size();
    }

    public boolean isEmpty() {
        return this.players.isEmpty();
    }

    public VoiceChannel getChannel() {
        if (this.channel == null || this.channel.isEmpty()) {
            return null;
        }
        Guild guild = this.config.getGuild();
        if (guild != null) {
            return guild.getVoiceChannelById(this.channel);
        }
        return null;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public static Set<Network> getNetworks() {
        return Network.networks;
    }
}
