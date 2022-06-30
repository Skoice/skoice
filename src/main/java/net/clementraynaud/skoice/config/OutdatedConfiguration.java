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
import net.clementraynaud.skoice.storage.LinksFileStorage;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class OutdatedConfiguration {

    private final FileConfiguration oldData = new YamlConfiguration();

    private final Skoice plugin;

    public OutdatedConfiguration(Skoice plugin) {
        this.plugin = plugin;
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
            this.convertOldData("mainVoiceChannelID", ConfigurationField.VOICE_CHANNEL_ID.toString());
            this.convertOldRadius();
            this.plugin.getConfiguration().saveFile();
            this.convertOldLinks();
            try {
                this.plugin.getLinksFileStorage().getFile().loadFromString(this.plugin.getLinksFileStorage().getFile().saveToString());
            } catch (InvalidConfigurationException ignored) {
            }
            try {
                Files.delete(outdatedConfig.toPath());
            } catch (IOException ignored) {
            }
            this.plugin.getLogger().info(this.plugin.getLang().getMessage("logger.info.plugin-updated"));
        }
    }

    private void convertOldToken() {
        if (this.oldData.contains("token")
                && !this.oldData.getString("token").isEmpty()
                && !this.plugin.getConfiguration().getFile().contains(ConfigurationField.TOKEN.toString())) {
            this.plugin.getConfiguration().setToken(this.oldData.getString("token"));
        }
    }

    private void convertOldRadius() {
        if (this.oldData.contains("distance.type")
                && "custom".equals(this.oldData.getString("distance.type"))) {
            this.convertOldData("distance.horizontalStrength", ConfigurationField.HORIZONTAL_RADIUS.toString());
            this.convertOldData("distance.verticalStrength", ConfigurationField.VERTICAL_RADIUS.toString());
        } else {
            if (!this.plugin.getConfiguration().getFile().contains(ConfigurationField.HORIZONTAL_RADIUS.toString())) {
                this.plugin.getConfiguration().getFile().set(ConfigurationField.HORIZONTAL_RADIUS.toString(), 80);
            }
            if (!this.plugin.getConfiguration().getFile().contains(ConfigurationField.VERTICAL_RADIUS.toString())) {
                this.plugin.getConfiguration().getFile().set(ConfigurationField.VERTICAL_RADIUS.toString(), 40);
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
            links.putAll(this.plugin.getLinksFileStorage().getLinks());
            this.plugin.getLinksFileStorage().getFile().set(LinksFileStorage.LINKS_FIELD, links);
            this.plugin.getLinksFileStorage().saveFile();
        }
    }

    private void convertOldData(String oldField, String newField) {
        if (this.oldData.contains(oldField)
                && !this.oldData.getString(oldField).isEmpty()
                && !this.plugin.getConfiguration().getFile().contains(newField)) {
            this.plugin.getConfiguration().getFile().set(newField, this.oldData.get(oldField));
        }
    }
}
