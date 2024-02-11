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

package net.clementraynaud.skoice.commands.skoice.arguments;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ArgumentInfo {
    CONFIGURE(false, true, false),
    TOOLTIPS(false, true, false),
    TOKEN(true, true, false),
    LANGUAGE(true, true, false),
    LINK(false, false, false),
    UNLINK(false, false, false);

    private final boolean allowedInConsole;
    private final boolean permissionRequired;
    private final boolean hidden;

    ArgumentInfo(boolean allowedInConsole, boolean permissionRequired, boolean hidden) {
        this.allowedInConsole = allowedInConsole;
        this.permissionRequired = permissionRequired;
        this.hidden = hidden;
    }

    public static ArgumentInfo get(String option) {
        return Stream.of(ArgumentInfo.values())
                .filter(value -> value.toString().equalsIgnoreCase(option))
                .findFirst()
                .orElse(null);
    }

    public static Set<String> getList(boolean permissionRequired) {
        return Stream.of(ArgumentInfo.values())
                .filter(arg -> (arg.permissionRequired == permissionRequired || permissionRequired) && !arg.isHidden())
                .map(Enum::toString)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public static String getJoinedList(boolean permissionRequired) {
        Set<String> list = ArgumentInfo.getList(permissionRequired);
        return list.size() == 1
                ? String.join("/", list)
                : "<" + String.join("/", list) + ">";
    }

    private static Set<String> getConsoleAllowedList() {
        return Stream.of(ArgumentInfo.values())
                .filter(arg -> arg.allowedInConsole && !arg.isHidden())
                .map(Enum::toString)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public static String getJoinedConsoleAllowedList() {
        Set<String> consoleAllowedList = ArgumentInfo.getConsoleAllowedList();
        return consoleAllowedList.size() == 1
                ? String.join("/", consoleAllowedList)
                : "<" + String.join("/", consoleAllowedList) + ">";
    }

    public boolean isAllowedInConsole() {
        return this.allowedInConsole;
    }

    public boolean isPermissionRequired() {
        return this.permissionRequired;
    }

    public boolean isHidden() {
        return this.hidden;
    }
}
