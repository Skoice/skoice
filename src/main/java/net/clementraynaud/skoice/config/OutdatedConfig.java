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

    public void update() {
        File outdatedConfig = new File(Skoice.getPlugin().getDataFolder() + File.separator + "data.yml");
        if (outdatedConfig.exists()) {
            try {
                this.oldData.load(outdatedConfig);
            } catch (IOException | InvalidConfigurationException e) {
                return;
            }
            this.convertOldToken();
            this.convertOldData("mainVoiceChannelID", Config.LOBBY_ID_FIELD);
            this.convertOldRadius();
            this.convertOldLinks();
            Skoice.getPlugin().saveConfig();
            try {
                Files.delete(outdatedConfig.toPath());
            } catch (IOException ignored) {
            }
        }
    }

    private void convertOldToken() {
        if (this.oldData.contains("token")
                && !this.oldData.getString("token").isEmpty()
                && !Skoice.getPlugin().getConfig().contains(Config.TOKEN_FIELD)) {
            Config.setToken(this.oldData.getString("token"));
        }
    }

    private void convertOldRadius() {
        if (this.oldData.contains("distance.type")
                && "custom" .equals(this.oldData.getString("distance.type"))) {
            this.convertOldData("distance.horizontalStrength", Config.HORIZONTAL_RADIUS_FIELD);
            this.convertOldData("distance.verticalStrength", Config.VERTICAL_RADIUS_FIELD);
        } else {
            if (!Skoice.getPlugin().getConfig().contains(Config.HORIZONTAL_RADIUS_FIELD)) {
                Skoice.getPlugin().getConfig().set(Config.HORIZONTAL_RADIUS_FIELD, 80);
            }
            if (!Skoice.getPlugin().getConfig().contains(Config.VERTICAL_RADIUS_FIELD)) {
                Skoice.getPlugin().getConfig().set(Config.VERTICAL_RADIUS_FIELD, 40);
            }
        }
    }

    private void convertOldLinks() {
        if (this.oldData.getConfigurationSection("Data") != null) {
            Map<String, String> linkMap = new HashMap<>();
            Set<String> subkeys = this.oldData.getConfigurationSection("Data").getKeys(false);
            Iterator<String> iterator = subkeys.iterator();
            for (int i = 0; i < subkeys.size(); i += 2) {
                linkMap.put(iterator.next(), iterator.next());
            }
            linkMap.putAll(Config.getLinkMap());
            Skoice.getPlugin().getConfig().set(Config.LINK_MAP_FIELD, linkMap);
        }
    }

    private void convertOldData(String oldField, String newField) {
        if (this.oldData.contains(oldField)
                && !this.oldData.getString(oldField).isEmpty()
                && !Skoice.getPlugin().getConfig().contains(newField)) {
            Skoice.getPlugin().getConfig().set(newField, this.oldData.get(oldField));
        }
    }
}
