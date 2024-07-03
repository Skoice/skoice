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

package net.clementraynaud.skoice.menus.selectmenus;

import net.clementraynaud.skoice.Skoice;

public class SelectMenuFactory {

    public SelectMenu getSelectMenu(Skoice plugin, String menuId) {
        switch (menuId) {
            case "server":
                return new ServerSelectMenu(plugin);
            case "voice-channel":
                return new VoiceChannelSelectMenu(plugin);
            case "range":
                return new RangeSelectMenu(plugin);
            case "language":
                return new LanguageSelectMenu(plugin);
            case "login-notification":
                return new LoginNotificationSelectMenu(plugin);
            case "included-players":
                return new IncludedPlayersSelectMenu(plugin);
            case "action-bar-alerts":
                return new ActionBarAlertsSelectMenu(plugin);
            case "active-worlds":
                return new ActiveWorldsSelectMenu(plugin);
            case "tooltips":
            case "channel-visibility":
                return new ToggleSelectMenu(plugin, menuId);
            default:
                return null;
        }
    }
}
