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

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.config.Config.*;

public class OutdatedConfig {

    private final FileConfiguration oldData = new YamlConfiguration();

    public void update() {
        File outdatedConfig = new File(getPlugin().getDataFolder() + File.separator + "data.yml");
        if (outdatedConfig.exists()) {
            try {
                oldData.load(outdatedConfig);
            } catch (IOException | InvalidConfigurationException e) {
                return;
            }
            convertOldToken();
            convertOldData("mainVoiceChannelID", LOBBY_ID_FIELD);
            convertOldRadius();
            convertOldLinks();
            getPlugin().saveConfig();
            try {
                Files.delete(outdatedConfig.toPath());
            } catch (IOException ignored) {
            }
        }
    }

    private void convertOldToken() {
        if (oldData.contains("token")
                && !oldData.getString("token").isEmpty()
                && !getPlugin().getConfig().contains(TOKEN_FIELD)) {
            setToken(oldData.getString("token"));
        }
    }

    private void convertOldRadius() {
        if (oldData.contains("distance.type")
                && oldData.getString("distance.type").equals("custom")) {
            convertOldData("distance.horizontalStrength", HORIZONTAL_RADIUS_FIELD);
            convertOldData("distance.verticalStrength", VERTICAL_RADIUS_FIELD);
        } else {
            if (!getPlugin().getConfig().contains(HORIZONTAL_RADIUS_FIELD)) {
                getPlugin().getConfig().set(HORIZONTAL_RADIUS_FIELD, 80);
            }
            if (!getPlugin().getConfig().contains(VERTICAL_RADIUS_FIELD)) {
                getPlugin().getConfig().set(VERTICAL_RADIUS_FIELD, 40);
            }
        }
    }

    private void convertOldLinks() {
        if (oldData.getConfigurationSection("Data") != null) {
            Map<String, String> linkMap = new HashMap<>();
            Set<String> subkeys = oldData.getConfigurationSection("Data").getKeys(false);
            Iterator<String> iterator = subkeys.iterator();
            for (int i = 0; i < subkeys.size(); i+=2)
                linkMap.put(iterator.next(), iterator.next());
            linkMap.putAll(getLinkMap());
            getPlugin().getConfig().set(LINK_MAP_FIELD, linkMap);
        }
    }

    private void convertOldData(String oldField, String newField) {
        if (oldData.contains(oldField)
                && !oldData.getString(oldField).isEmpty()
                && !getPlugin().getConfig().contains(newField)) {
            getPlugin().getConfig().set(newField, oldData.get(oldField));
        }
    }
}
