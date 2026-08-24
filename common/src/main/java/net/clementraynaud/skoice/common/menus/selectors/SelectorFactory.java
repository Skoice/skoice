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
import net.clementraynaud.skoice.common.storage.config.WorldOverrides;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SelectorFactory {

    public List<Selector> getSelectors(Skoice plugin, String menuId, Map<String, String> args) {
        String overrideId = args.get(WorldOverrides.ARG);
        if (overrideId != null && !WorldOverrides.isScopedMenu(menuId)) {
            overrideId = null;
        }

        switch (menuId) {
            case "server":
                return Collections.singletonList(new ServerSelector(plugin));
            case "voice-channel":
                return Collections.singletonList(new VoiceChannelSelector(plugin));
            case "range":
                return Collections.singletonList(new RangeSelector(plugin, overrideId));
            case "language":
                return Collections.singletonList(new LanguageSelector(plugin));
            case "login-notification":
                return Collections.singletonList(new LoginNotificationSelector(plugin));
            case "excluded-players":
                return Arrays.asList(
                        new ExcludedPlayerTypesSelector(plugin, overrideId),
                        new ExcludedPlayerBehaviorSelector(plugin, overrideId)
                );
            case "teams":
                return overrideId == null
                        ? Arrays.asList(new TeamProviderSelector(plugin), new TeamBehaviorsSelector(plugin, null))
                        : Collections.singletonList(new TeamBehaviorsSelector(plugin, overrideId));
            case "action-bar-alerts":
                return Collections.singletonList(new ActionBarAlertsSelector(plugin, overrideId));
            case "active-worlds":
                return overrideId == null
                        ? Collections.singletonList(new ActiveWorldsSelector(plugin))
                        : Collections.singletonList(new WorldActivitySelector(plugin, overrideId));
            case WorldOverrides.LIST_MENU_ID:
                return Collections.singletonList(new WorldOverrideSelector(plugin));
            case WorldOverrides.WORLDS_MENU_ID:
                return Collections.singletonList(new WorldOverrideWorldsSelector(plugin, overrideId));
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
