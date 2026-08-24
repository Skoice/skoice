/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025, 2026 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.common.storage;

import net.clementraynaud.skoice.common.Skoice;
import org.h2.mvstore.MVMap;

import java.util.ArrayList;

public abstract class MvStoreFile<V> {

    protected final Skoice plugin;
    private final MVMap<String, V> map;

    protected MvStoreFile(Skoice plugin, MvStore store, String mapName) {
        this.plugin = plugin;
        this.map = store.openMap(mapName);
    }

    public boolean contains(String path) {
        return this.map.containsKey(path);
    }

    public void set(String path, V value) {
        if (value == null) {
            this.map.remove(path);
        } else {
            this.map.put(path, value);
        }
    }

    public void remove(String path) {
        this.map.remove(path);
    }

    public void removeKeysStartingWith(String prefix) {
        for (String key : new ArrayList<>(this.map.keySet())) {
            if (key.startsWith(prefix)) {
                this.map.remove(key);
            }
        }
    }

    public void setDefault(String path, V value) {
        if (value != null) {
            this.map.putIfAbsent(path, value);
        }
    }

    protected MVMap<String, V> getMap() {
        return this.map;
    }
}
