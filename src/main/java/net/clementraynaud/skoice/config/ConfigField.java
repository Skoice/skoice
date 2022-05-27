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

public enum ConfigField {
    TOKEN,
    LANG,
    LOBBY_ID,
    HORIZONTAL_RADIUS,
    VERTICAL_RADIUS,
    ACTION_BAR_ALERT,
    CHANNEL_VISIBILITY,
    LINKS,
    MUTED_USERS,
    CONFIG_MENU,
    MESSAGE_ID,
    TEXT_CHANNEL_ID,
    GUILD_ID;

    public String get() {
        return this.toString().toLowerCase().replace("_", "-");
    }
}
