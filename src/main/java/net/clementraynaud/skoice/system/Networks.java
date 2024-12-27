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


import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class Networks {

    private static final Set<Network> networkSet = ConcurrentHashMap.newKeySet();

    private Networks() {
    }

    public static Set<Network> getAll() {
        return Networks.networkSet;
    }

    public static Set<Network> getInitialized() {
        return Networks.networkSet.stream()
                .filter(network -> network.getProximityChannel().isInitialized())
                .collect(Collectors.toSet());
    }

    public static Set<ProximityChannel> getProximityChannels() {
        return Networks.networkSet.stream()
                .map(Network::getProximityChannel)
                .collect(Collectors.toSet());
    }

    public static void add(Network network) {
        Networks.networkSet.add(network);
    }

    public static void remove(Network network) {
        Networks.networkSet.remove(network);
    }

    public static void merge(Network network1, Network network2) {
        if (network1.size() > network2.size()) {
            network1.engulf(network2);
        } else {
            network2.engulf(network1);
        }
    }

    public static void clean() {
        Networks.networkSet.stream()
                .filter(Network::isEmpty)
                .forEach(Networks::remove);
    }

    public static void clear() {
        Networks.networkSet.clear();
    }
}
