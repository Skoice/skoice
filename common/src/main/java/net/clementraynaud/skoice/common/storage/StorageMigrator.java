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
import net.clementraynaud.skoice.common.storage.config.ConfigStore;
import org.h2.mvstore.MVMap;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class StorageMigrator {

    private final Skoice plugin;
    private final MvStore store;

    public StorageMigrator(Skoice plugin, MvStore store) {
        this.plugin = plugin;
        this.store = store;
    }

    public void run() {
        if (this.store.wasPreExisting()) {
            return;
        }

        List<Map.Entry<String, String>> flatTargets = new ArrayList<>();
        flatTargets.add(new AbstractMap.SimpleEntry<>("config.yml", ConfigStore.MAP_NAME));
        flatTargets.add(new AbstractMap.SimpleEntry<>("login-notification.yml", LoginNotificationStore.MAP_NAME));
        flatTargets.add(new AbstractMap.SimpleEntry<>("temp.yml", TempStore.MAP_NAME));

        boolean anythingMigrated = false;
        for (Map.Entry<String, String> target : flatTargets) {
            File file = new File(this.plugin.getDataFolder(), target.getKey());
            if (!file.exists()) {
                continue;
            }
            if (this.migrateFlat(file, target.getValue())) {
                anythingMigrated = true;
            }
        }

        File linksFile = new File(this.plugin.getDataFolder(), "links.yml");
        if (linksFile.exists() && this.migrateLinks(linksFile)) {
            anythingMigrated = true;
        }

        if (!anythingMigrated) {
            return;
        }

        this.store.commit();

        List<String> filesToDelete = new ArrayList<>();
        for (Map.Entry<String, String> target : flatTargets) {
            filesToDelete.add(target.getKey());
        }
        filesToDelete.add("links.yml");

        for (String fileName : filesToDelete) {
            File file = new File(this.plugin.getDataFolder(), fileName);
            if (!file.exists()) {
                continue;
            }
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                this.plugin.getLogger().log(Level.WARNING, "Failed to delete migrated file " + file.getName() + ": " + e.getMessage());
            }
        }
    }

    private boolean migrateFlat(File file, String mapName) {
        YamlConfiguration yaml = this.loadYaml(file);
        if (yaml == null) {
            return false;
        }

        MVMap<String, Object> map = this.store.openMap(mapName);
        for (Map.Entry<String, Object> entry : yaml.getValues(true).entrySet()) {
            Object value = entry.getValue();
            if (value == null || value instanceof ConfigurationSection) {
                continue;
            }
            if (value instanceof Collection) {
                map.put(entry.getKey(), new ArrayList<Object>((Collection<?>) value));
            } else {
                map.put(entry.getKey(), value);
            }
        }
        return true;
    }

    private boolean migrateLinks(File file) {
        YamlConfiguration yaml = this.loadYaml(file);
        if (yaml == null) {
            return false;
        }

        ConfigurationSection linksSection = yaml.getConfigurationSection(LinksStore.MAP_NAME);
        if (linksSection == null) {
            return true;
        }

        MVMap<String, Object> map = this.store.openMap(LinksStore.MAP_NAME);
        for (Map.Entry<String, Object> entry : linksSection.getValues(false).entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            map.put(entry.getKey(), entry.getValue().toString());
        }
        return true;
    }

    private YamlConfiguration loadYaml(File file) {
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(file);
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to read " + file.getName() + " for migration: " + e.getMessage());
            return null;
        }
        return yaml;
    }
}
