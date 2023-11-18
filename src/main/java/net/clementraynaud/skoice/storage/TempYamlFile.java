/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.storage;

import net.clementraynaud.skoice.Skoice;

public class TempYamlFile extends YamlFile {

    public static final String CONFIG_MENU_FIELD = "config-menu";
    public static final String GUILD_ID_FIELD = "guild-id";
    public static final String CHANNEL_ID_FIELD = "channel-id";
    public static final String MESSAGE_ID_FIELD = "message-id";
    public static final String MUTED_USERS_ID_FIELD = "muted-users-id";

    public TempYamlFile(Skoice plugin) {
        super(plugin, "temp");
    }
}
