/*
 * Copyright 2020, 2021, 2022, 2023, 2024 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.storage.config;

public enum ConfigField {

    SESSION_REPORTED,
    TOKEN,
    LANG,
    VOICE_CHANNEL_ID,
    HORIZONTAL_RADIUS,
    VERTICAL_RADIUS,
    LOGIN_NOTIFICATION,
    CONNECTING_ALERT,
    DISCONNECTING_ALERT,
    TOOLTIPS,
    PLAYERS_ON_DEATH_SCREEN_INCLUDED,
    SPECTATORS_INCLUDED,
    CHANNEL_VISIBILITY;

    @Override
    public String toString() {
        return this.name().toLowerCase().replace("_", "-");
    }

    public String toCamelCase() {
        StringBuilder builder = new StringBuilder();
        String[] words = this.name().split("_");
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
}
