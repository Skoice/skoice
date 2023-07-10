/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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
                .filter(Network::isInitialized)
                .collect(Collectors.toSet());
    }

    public static void add(Network network) {
        Networks.networkSet.add(network);
    }

    public static void remove(Network network) {
        Networks.networkSet.remove(network);
    }

    public static void clean() {
        Networks.networkSet.stream()
                .filter(Network::isEmpty)
                .filter(network -> network.getChannel() != null && network.getChannel().getMembers().isEmpty())
                .forEach(network -> network.delete("communication-lost"));
    }

    public static void clear() {
        Networks.networkSet.clear();
    }
}
