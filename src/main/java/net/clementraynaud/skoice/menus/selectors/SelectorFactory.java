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

package net.clementraynaud.skoice.menus.selectors;

import net.clementraynaud.skoice.Skoice;

public class SelectorFactory {

    public Selector getSelector(Skoice plugin, String menuId) {
        switch (menuId) {
            case "server":
                return new ServerSelector(plugin);
            case "voice-channel":
                return new VoiceChannelSelector(plugin);
            case "range":
                return new RangeSelector(plugin);
            case "language":
                return new LanguageSelector(plugin);
            case "login-notification":
                return new LoginNotificationSelector(plugin);
            case "included-players":
                return new IncludedPlayersSelector(plugin);
            case "action-bar-alerts":
                return new ActionBarAlertsSelector(plugin);
            case "active-worlds":
                return new ActiveWorldsSelector(plugin);
            case "chaining":
                return new ChainingSelector(plugin);
            case "release-channel":
                return new ReleaseChannelSelector(plugin);
            case "tooltips":
            case "separated-teams":
            case "text-chat":
            case "channel-visibility":
            case "discordsrv-synchronization":
                return new ToggleSelector(plugin, menuId);
            default:
                return null;
        }
    }
}
