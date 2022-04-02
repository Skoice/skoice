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

package net.clementraynaud.skoice.menus;

import net.dv8tion.jda.api.entities.Emoji;

public enum MenuEmoji {
    GEAR("U+2699"),
    CLOCK3("U+1F552"),
    HEAVY_CHECK_MARK("U+2714"),
    HEAVY_PLUS_SIGN("U+2795"),
    HEAVY_MULTIPLICATION_X("U+2716"),
    WARNING_SIGN("U+26A0"),
    FILE_CABINET("U+1F5C4"),
    SOUND("U+1F509"),
    VIDEO_GAME("U+1F3AE"),
    SCREWDRIVER("U+1FA9B"),
    MAP("U+1F5FA"),
    CROSSED_SWORDS("U+2694"),
    PENCIL2("U+270F"),
    LEFT_RIGHT_ARROW("U+2194"),
    UP_DOWN_ARROW("U+2195"),
    KEYBOARD("U+2328"),
    WRENCH("U+1F527"),
    GLOBE_WITH_MERIDIANS("U+1F310"),
    EXCLAMATION("U+2757"),
    MAG("U+1F50D");

    private final String unicode;

    MenuEmoji(String unicode) {
        this.unicode = unicode;
    }

    public Emoji getEmojiFromUnicode() {
        return Emoji.fromUnicode(unicode);
    }

    @Override
    public String toString() {
        return ":" + this.name().toLowerCase() + ":";
    }
}
