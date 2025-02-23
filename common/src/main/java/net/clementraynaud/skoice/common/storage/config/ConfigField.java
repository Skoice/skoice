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

public enum ConfigField {
    TOKEN,
    LANG,
    VOICE_CHANNEL_ID,
    HORIZONTAL_RADIUS,
    VERTICAL_RADIUS,
    LOGIN_NOTIFICATION,
    CONNECTING_ALERT,
    DISCONNECTING_ALERT,
    MUTED_ALERT,
    DEAFENED_ALERT,
    TOOLTIPS,
    PLAYERS_ON_DEATH_SCREEN_INCLUDED,
    SPECTATORS_INCLUDED,
    DISABLED_WORLDS,
    SEPARATED_TEAMS,
    TEXT_CHAT,
    CHANNEL_VISIBILITY,
    CHAINING,
    DISCORDSRV_SYNCHRONIZATION,
    ESSENTIALSX_SYNCHRONIZATION,
    RELEASE_CHANNEL,
    UNREVIEWED_SETTINGS,
    SESSION_REPORTED,
    LUDICROUS;

    private final String lowerCaseName;
    private final String camelCaseName;

    ConfigField() {
        this.lowerCaseName = this.name().toLowerCase().replace("_", "-");
        this.camelCaseName = ConfigField.convertToCamelCase(this.name());
    }

    private static String convertToCamelCase(String name) {
        StringBuilder builder = new StringBuilder();
        String[] words = name.split("_");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (i == 0) {
                word = word.toLowerCase();
            } else {
                word = word.charAt(0) + word.substring(1).toLowerCase();
            }
            builder.append(word);
        }
        return builder.toString();
    }

    public String toCamelCase() {
        return this.camelCaseName;
    }

    @Override
    public String toString() {
        return this.lowerCaseName;
    }
}
