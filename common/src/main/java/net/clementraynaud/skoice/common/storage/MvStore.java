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
import org.h2.mvstore.MVStore;

import java.io.File;

public class MvStore {

    public static final String FILE_NAME = "skoice.mvstore";

    private final MVStore store;
    private final boolean preExisting;

    public MvStore(Skoice plugin) {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File file = new File(dataFolder, MvStore.FILE_NAME);
        this.preExisting = file.exists();
        this.store = new MVStore.Builder()
                .fileName(file.getAbsolutePath())
                .open();
    }

    public <V> MVMap<String, V> openMap(String name) {
        return this.store.openMap(name);
    }

    public boolean wasPreExisting() {
        return this.preExisting;
    }

    public void commit() {
        this.store.commit();
    }

    public void close() {
        this.store.close();
    }
}
