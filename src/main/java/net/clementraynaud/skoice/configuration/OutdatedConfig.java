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

package net.clementraynaud.skoice.configuration;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

import static net.clementraynaud.skoice.Skoice.getPlugin;

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
            convertOldData("mainVoiceChannelID", "lobby-id");
            convertOldData("distance.horizontalStrength", "radius.horizontal");
            convertOldData("distance.verticalStrength", "radius.vertical");
            getPlugin().saveConfig();
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
