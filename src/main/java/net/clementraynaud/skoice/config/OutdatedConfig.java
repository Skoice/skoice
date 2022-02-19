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

import net.clementraynaud.skoice.lang.Lang;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.config.Config.setToken;

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
            convertOldData("mainVoiceChannelID", "lobby-id");
            convertOldRadius();
            if (!getPlugin().getConfig().contains("lang"))
                getPlugin().getConfig().set("lang", Lang.EN.name());
            getPlugin().saveConfig();
        }
    }

    private void convertOldToken() {
        if (oldData.contains("token")
                && !oldData.getString("token").isEmpty()
                && !getPlugin().getConfig().contains("token")) {
            setToken(oldData.getString("token"));
        }
    }

    private void convertOldRadius() {
        if (oldData.contains("distance.type")
                && oldData.getString("distance.type").equals("custom")) {
            convertOldData("distance.horizontalStrength", "radius.horizontal");
            convertOldData("distance.verticalStrength", "radius.vertical");
        } else {
            if (!getPlugin().getConfig().contains("radius.horizontal")) {
                getPlugin().getConfig().set("radius.horizontal", 80);
            }
            if (!getPlugin().getConfig().contains("radius.vertical")) {
                getPlugin().getConfig().set("radius.vertical", 40);
            }
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
