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

import java.util.Base64;

public class ConfigUpdater {

    private final Config config;

    public ConfigUpdater(Config config) {
        this.config = config;
    }

    public void setToken(String token) {
        byte[] tokenBytes = token.getBytes();
        for (int i = 0; i < tokenBytes.length; i++) {
            tokenBytes[i]++;
        }
        this.config.getFile().set(ConfigField.TOKEN.get(), Base64.getEncoder().encodeToString(tokenBytes));
        this.config.saveFile();
    }

    public void linkUser(String minecraftID, String discordID) {
        this.config.getFile().set(ConfigField.LINK_MAP.get() + "." + minecraftID, discordID);
        this.config.saveFile();
    }

    public void unlinkUser(String minecraftID) {
        this.config.getFile().set(ConfigField.LINK_MAP.get() + "." + minecraftID, null);
        this.config.saveFile();
    }

}
