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

package net.clementraynaud.skoice.common.lang;

import net.dv8tion.jda.api.entities.emoji.Emoji;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum LangInfo {

    EN("English", "U+1F1ECU+1F1E7"),
    DA("Dansk", "U+1F1E9U+1F1F0"),
    DE("Deutsch", "U+1F1E9U+1F1EA"),
    ES("Español", "U+1F1EAU+1F1F8"),
    FR("Français", "U+1F1EBU+1F1F7"),
    IT("Italiano", "U+1F1EEU+1F1F9"),
    JA("日本語", "U+1F1EFU+1F1F5"),
    NO("Norsk", "U+1F1F3U+1F1F4"),
    PL("Polski", "U+1F1F5U+1F1F1"),
    PT("Português", "U+1F1F5U+1F1F9"),
    RU("Русский", "U+1F1F7U+1F1FA"),
    TR("Türkçe", "U+1F1F9U+1F1F7");

    private static final Set<String> langList;
    private static final String joinedLangList;

    static {
        langList = Stream.of(LangInfo.values())
                .map(Enum::toString)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        joinedLangList = "<" + String.join("/", LangInfo.langList) + ">";
    }

    private final String fullName;
    private final String unicode;

    LangInfo(String fullName, String unicode) {
        this.fullName = fullName;
        this.unicode = unicode;
    }

    public static Set<String> getList() {
        return LangInfo.langList;
    }

    public static String getJoinedList() {
        return LangInfo.joinedLangList;
    }

    public String getFullName() {
        return this.fullName;
    }

    public Emoji getEmoji() {
        return Emoji.fromUnicode(this.unicode);
    }
}
