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

package net.clementraynaud.skoice.common.storage.config;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.storage.LinksYamlFile;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.FileConfiguration;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class OutdatedConfig {

    private final Skoice plugin;

    public OutdatedConfig(Skoice plugin) {
        this.plugin = plugin;
    }

    public void update() {
        this.convertOldData(this.plugin.getConfigYamlFile(), "action-bar-alert", ConfigField.DISCONNECTING_ALERT, true);

        File outdatedConfig = new File(this.plugin.getDataFolder(), "data.yml");
        if (outdatedConfig.exists()) {
            FileConfiguration oldData = new YamlConfiguration();
            try {
                oldData.load(outdatedConfig);
            } catch (IOException e) {
                return;
            }

            this.convertOldToken(oldData);
            this.convertOldData(oldData, "mainVoiceChannelID", ConfigField.VOICE_CHANNEL_ID, false);
            this.convertOldRadius(oldData);
            this.convertOldLinks(oldData);

            try {
                this.plugin.getLinksYamlFile().loadFromString(this.plugin.getLinksYamlFile().saveToString());
            } catch (IOException ignored) {
            }

            try {
                Files.delete(outdatedConfig.toPath());
            } catch (IOException ignored) {
            }

            this.plugin.log(Level.INFO, "logger.info.skoice-3");
        }
    }

    private void convertOldToken(FileConfiguration file) {
        String oldToken = file.getString("token");
        if (oldToken != null
                && !oldToken.isEmpty()
                && !this.plugin.getConfigYamlFile().contains(ConfigField.TOKEN.toString())) {
            this.plugin.getConfigYamlFile().setToken(oldToken);
        }
    }

    private void convertOldRadius(FileConfiguration file) {
        if (file.contains("distance.type")
                && "custom".equals(file.getString("distance.type"))) {
            this.convertOldData(file, "distance.horizontalStrength", ConfigField.HORIZONTAL_RADIUS, false);
            this.convertOldData(file, "distance.verticalStrength", ConfigField.VERTICAL_RADIUS, false);
        } else {
            this.plugin.getConfigYamlFile().setDefault(ConfigField.HORIZONTAL_RADIUS.toString(), 80);
            this.plugin.getConfigYamlFile().setDefault(ConfigField.VERTICAL_RADIUS.toString(), 40);
        }
    }

    private void convertOldLinks(FileConfiguration file) {
        ConfigurationSection dataSection = file.getConfigurationSection("Data");
        if (dataSection != null) {
            Map<String, String> links = new HashMap<>();
            Set<String> subkeys = dataSection.getKeys(false);
            Iterator<String> iterator = subkeys.iterator();
            for (int i = 0; i < subkeys.size(); i += 2) {
                links.put(iterator.next(), iterator.next());
            }
            links.putAll(this.plugin.getLinksYamlFile().getLinks());
            this.plugin.getLinksYamlFile().set(LinksYamlFile.LINKS_FIELD, links);
        }
    }

    private void convertOldData(FileConfiguration file, String oldField, ConfigField newField, boolean forceReplace) {
        String oldFieldValue = file.getString(oldField);
        if (oldFieldValue != null
                && !oldFieldValue.isEmpty()) {
            if (forceReplace) {
                this.plugin.getConfigYamlFile().set(newField.toString(), file.get(oldField));
            } else {
                this.plugin.getConfigYamlFile().setDefault(newField.toString(), file.get(oldField));
            }
            file.set(oldField, null);
        }
    }
}
