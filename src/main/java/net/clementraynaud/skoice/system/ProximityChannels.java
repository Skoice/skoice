/*
 * Copyright 2020, 2021, 2022, 2023, 2024 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

import org.bukkit.Bukkit;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class ProximityChannels {

    private static final int EXTRA_CHANNELS = 1;
    private static final Set<ProximityChannel> proximityChannelSet = ConcurrentHashMap.newKeySet();

    private ProximityChannels() {
    }

    public static Set<ProximityChannel> getAll() {
        try {
            if (Bukkit.isPrimaryThread()) {
                new IllegalStateException("This method should not be called from the main thread.").printStackTrace();
            }
        } catch (NullPointerException ignored) {
        }
        return ProximityChannels.proximityChannelSet;
    }

    public static Set<ProximityChannel> getInitialized() {
        try {
            if (Bukkit.isPrimaryThread() && Bukkit.getPluginManager().isPluginEnabled("Skoice")) {
                new IllegalStateException("This method should not be called from the main thread.").printStackTrace();
            }
        } catch (NullPointerException ignored) {
        }
        return ProximityChannels.proximityChannelSet.stream()
                .filter(ProximityChannel::isInitialized)
                .collect(Collectors.toSet());
    }

    public static void add(ProximityChannel proximityChannel) {
        try {
            if (Bukkit.isPrimaryThread()) {
                new IllegalStateException("This method should not be called from the main thread.").printStackTrace();
            }
        } catch (NullPointerException ignored) {
        }
        ProximityChannels.proximityChannelSet.add(proximityChannel);
    }

    public static void remove(ProximityChannel proximityChannel) {
        try {
            if (Bukkit.isPrimaryThread()) {
                new IllegalStateException("This method should not be called from the main thread.").printStackTrace();
            }
        } catch (NullPointerException ignored) {
        }
        ProximityChannels.proximityChannelSet.remove(proximityChannel);
    }

    public static void remove(String channelId) {
        try {
            if (Bukkit.isPrimaryThread()) {
                new IllegalStateException("This method should not be called from the main thread.").printStackTrace();
            }
        } catch (NullPointerException ignored) {
        }
        ProximityChannels.proximityChannelSet.removeIf(proximityChannel ->
                proximityChannel.getChannelId().equals(channelId));
    }

    public static void clean(int possibleUsers) {
        try {
            if (Bukkit.isPrimaryThread()) {
                new IllegalStateException("This method should not be called from the main thread.").printStackTrace();
            }
        } catch (NullPointerException ignored) {
        }
        int possibleNetworks = possibleUsers / 2;

        ProximityChannels.proximityChannelSet.stream()
                .filter(proximityChannel -> proximityChannel.getChannel() != null
                        && proximityChannel.getChannel().getMembers().isEmpty())
                .forEach(proximityChannel -> {
                    if (ProximityChannels.getAll().size() > possibleNetworks + ProximityChannels.EXTRA_CHANNELS
                            || possibleUsers == 0) {
                        proximityChannel.delete();
                    }
                });
    }

    public static boolean isProximityChannel(String voiceChannelId) {
        return ProximityChannels.getInitialized().stream()
                .map(ProximityChannel::getChannelId)
                .anyMatch(channelId -> channelId.equals(voiceChannelId));
    }
}
