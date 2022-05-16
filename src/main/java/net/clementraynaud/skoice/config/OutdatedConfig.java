/*
 * Copyright 2020, 2021, 2022 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.config;

import net.clementraynaud.skoice.Skoice;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class OutdatedConfig {

    private final FileConfiguration oldData = new YamlConfiguration();

    private final Skoice plugin;
    private final Config config;

    public OutdatedConfig(Skoice plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void update() {
        File outdatedConfig = new File(this.plugin.getDataFolder() + File.separator + "data.yml");
        if (outdatedConfig.exists()) {
            try {
                this.oldData.load(outdatedConfig);
            } catch (IOException | InvalidConfigurationException e) {
                return;
            }
            this.convertOldToken();
            this.convertOldData("mainVoiceChannelID", ConfigField.LOBBY_ID.get());
            this.convertOldRadius();
            this.convertOldLinks();
            this.config.saveFile();
            try {
                Files.delete(outdatedConfig.toPath());
            } catch (IOException ignored) {
            }
        }
    }

    private void convertOldToken() {
        if (this.oldData.contains("token")
                && !this.oldData.getString("token").isEmpty()
                && !this.config.getFile().contains(ConfigField.TOKEN.get())) {
            this.config.getUpdater().setToken(this.oldData.getString("token"));
        }
    }

    private void convertOldRadius() {
        if (this.oldData.contains("distance.type")
                && "custom".equals(this.oldData.getString("distance.type"))) {
            this.convertOldData("distance.horizontalStrength", ConfigField.HORIZONTAL_RADIUS.get());
            this.convertOldData("distance.verticalStrength", ConfigField.VERTICAL_RADIUS.get());
        } else {
            if (!this.config.getFile().contains(ConfigField.HORIZONTAL_RADIUS.get())) {
                this.config.getFile().set(ConfigField.HORIZONTAL_RADIUS.get(), 80);
            }
            if (!this.config.getFile().contains(ConfigField.VERTICAL_RADIUS.get())) {
                this.config.getFile().set(ConfigField.VERTICAL_RADIUS.get(), 40);
            }
        }
    }

    private void convertOldLinks() {
        if (this.oldData.getConfigurationSection("Data") != null) {
            Map<String, String> links = new HashMap<>();
            Set<String> subkeys = this.oldData.getConfigurationSection("Data").getKeys(false);
            Iterator<String> iterator = subkeys.iterator();
            for (int i = 0; i < subkeys.size(); i += 2) {
                links.put(iterator.next(), iterator.next());
            }
            links.putAll(this.config.getReader().getLinks());
            this.config.getFile().set(ConfigField.LINKS.get(), links);
        }
    }

    private void convertOldData(String oldField, String newField) {
        if (this.oldData.contains(oldField)
                && !this.oldData.getString(oldField).isEmpty()
                && !this.config.getFile().contains(newField)) {
            this.config.getFile().set(newField, this.oldData.get(oldField));
        }
    }
}
