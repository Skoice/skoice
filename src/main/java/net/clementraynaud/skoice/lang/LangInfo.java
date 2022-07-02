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

package net.clementraynaud.skoice.lang;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public enum LangInfo {

    EN("English", "U+1F1ECU+1F1E7"),
    FR("Français", "U+1F1EBU+1F1F7"),
    DE("Deutsch", "U+1F1E9U+1F1EA"),
    PL("Polski", "U+1F1F5U+1F1F1");

    private final String fullName;
    private final String unicode;

    LangInfo(String fullName, String unicode) {
        this.fullName = fullName;
        this.unicode = unicode;
    }

    public String getFullName() {
        return this.fullName;
    }

    public Emoji getEmoji() {
        return Emoji.fromUnicode(this.unicode);
    }
}
