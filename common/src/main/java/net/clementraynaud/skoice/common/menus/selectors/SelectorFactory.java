/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025, 2026 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.common.menus.selectors;

import net.clementraynaud.skoice.common.Skoice;

import java.util.Collections;
import java.util.List;

public class SelectorFactory {

    public List<Selector> getSelectors(Skoice plugin, String menuId) {
        switch (menuId) {
            case "server":
                return Collections.singletonList(new ServerSelector(plugin));
            case "voice-channel":
                return Collections.singletonList(new VoiceChannelSelector(plugin));
            case "range":
                return Collections.singletonList(new RangeSelector(plugin));
            case "language":
                return Collections.singletonList(new LanguageSelector(plugin));
            case "login-notification":
                return Collections.singletonList(new LoginNotificationSelector(plugin));
            case "included-players":
                return Collections.singletonList(new IncludedPlayersSelector(plugin));
            case "excluded-players":
                return Collections.singletonList(new ExcludedPlayersSelector(plugin));
            case "teams":
                return Collections.singletonList(new TeamsSelector(plugin));
            case "team-provider":
                return Collections.singletonList(new TeamProviderSelector(plugin));
            case "action-bar-alerts":
                return Collections.singletonList(new ActionBarAlertsSelector(plugin));
            case "active-worlds":
                return Collections.singletonList(new ActiveWorldsSelector(plugin));
            case "chaining":
                return Collections.singletonList(new ChainingSelector(plugin));
            case "link-synchronization":
                return Collections.singletonList(new LinkSynchronizationSelector(plugin));
            case "release-channel":
                return Collections.singletonList(new ReleaseChannelSelector(plugin));
            case "tooltips":
            case "text-chat":
            case "channel-visibility":
            case "invite-link":
                return Collections.singletonList(new ToggleSelector(plugin, menuId));
            default:
                return Collections.emptyList();
        }
    }
}
