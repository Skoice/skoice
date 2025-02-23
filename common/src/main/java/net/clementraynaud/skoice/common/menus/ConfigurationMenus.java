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

package net.clementraynaud.skoice.common.menus;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class ConfigurationMenus {

    private static final Set<ConfigurationMenu> menuSet = new HashSet<>();

    private ConfigurationMenus() {
    }

    public static boolean contains(String messageId) {
        return ConfigurationMenus.menuSet.stream()
                .anyMatch(menu -> messageId.equals(menu.messageId));
    }

    public static Optional<ConfigurationMenu> getFromMessageId(String messageId) {
        return ConfigurationMenus.menuSet.stream()
                .filter(menu -> messageId.equals(menu.messageId))
                .findFirst();
    }

    public static void refreshAll() {
        ConfigurationMenus.clean();
        ConfigurationMenus.menuSet.forEach(menu -> menu.refreshId().editFromHook());
    }

    private static void clean() {
        ConfigurationMenus.menuSet.removeIf(menu -> !menu.isHookValid());
    }

    public static Set<ConfigurationMenu> getMenuSet() {
        return ConfigurationMenus.menuSet;
    }
}
