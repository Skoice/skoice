/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.common.system;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class ProximityChannels {

    private static final int EXTRA_CHANNELS = 1;
    private static final Set<ProximityChannel> PROXIMITY_CHANNEL_SET = ConcurrentHashMap.newKeySet();
    private static final Map<String, ProximityChannel> ISOLATION_CHANNEL_MAP = new ConcurrentHashMap<>();

    private ProximityChannels() {
    }

    public static Set<ProximityChannel> getAll() {
        return ProximityChannels.PROXIMITY_CHANNEL_SET;
    }

    public static Set<ProximityChannel> getInitialized() {
        return ProximityChannels.PROXIMITY_CHANNEL_SET.stream()
                .filter(ProximityChannel::isInitialized)
                .collect(Collectors.toSet());
    }

    public static Map<String, ProximityChannel> getIsolationChannelMap() {
        return ProximityChannels.ISOLATION_CHANNEL_MAP;
    }

    public static void add(ProximityChannel proximityChannel) {
        ProximityChannels.PROXIMITY_CHANNEL_SET.add(proximityChannel);
    }

    public static void remove(ProximityChannel proximityChannel) {
        ProximityChannels.PROXIMITY_CHANNEL_SET.remove(proximityChannel);
    }

    public static void remove(String channelId) {
        ProximityChannels.PROXIMITY_CHANNEL_SET.removeIf(proximityChannel ->
                proximityChannel.getChannelId().equals(channelId));
    }

    public static void clean(int possibleUsers, int possibleIsolatedUsers) {
        int possibleNetworks = possibleIsolatedUsers + (possibleUsers - possibleIsolatedUsers) / 2;

        ProximityChannels.PROXIMITY_CHANNEL_SET.stream()
                .filter(proximityChannel -> proximityChannel.getChannel() != null
                        && proximityChannel.getTheoreticalSize() == 0)
                .forEach(proximityChannel -> {
                    if (ProximityChannels.getAll().size() > possibleNetworks + ProximityChannels.EXTRA_CHANNELS
                            || possibleUsers == 0 && possibleIsolatedUsers == 0) {
                        proximityChannel.delete();
                    }
                });
    }

    public static void clear() {
        ProximityChannels.PROXIMITY_CHANNEL_SET.clear();
    }

    public static boolean isProximityChannel(String voiceChannelId) {
        return ProximityChannels.getInitialized().stream()
                .map(ProximityChannel::getChannelId)
                .anyMatch(channelId -> channelId.equals(voiceChannelId));
    }
}
