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

    ARROW_FORWARD("U+25B6"),
    CALENDAR_SPIRAL("U+1F5D3"),
    CARD_BOX("U+1F5C3"),
    CHART_WITH_UPWARDS_TREND("U+1F4C8"),
    CLIPBOARD("U+1F4CB"),
    CLOCK3("U+1F552"),
    CROSSED_SWORDS("U+2694"),
    ENVELOPE("U+2709"),
    EXCLAMATION("U+2757"),
    FILE_CABINET("U+1F5C4"),
    GEAR("U+2699"),
    GLOBE_WITH_MERIDIANS("U+1F310"),
    GREEN_HEART("U+1F49A"),
    HAMMER("U+1F528"),
    HEAVY_CHECK_MARK("U+2714"),
    HEAVY_MULTIPLICATION_X("U+2716"),
    HEAVY_PLUS_SIGN("U+2795"),
    INBOX_TRAY("U+1F4E5"),
    KEY("U+1F511"),
    LINK("U+1F517"),
    MAG("U+1F50D"),
    MAP("U+1F5FA"),
    NEWSPAPER("U+1F4F0"),
    NO_ENTRY("U+26D4"),
    PENCIL2("U+270F"),
    SCREWDRIVER("U+1FA9B"),
    SOUND("U+1F509"),
    VIDEO_GAME("U+1F3AE"),
    WARNING("U+26A0"),
    WRENCH("U+1F527"),
    X("U+274C");

    private final String unicode;

    MenuEmoji(String unicode) {
        this.unicode = unicode;
    }

    public Emoji get() {
        return Emoji.fromUnicode(this.unicode);
    }

    @Override
    public String toString() {
        return ":" + this.name().toLowerCase() + ": ";
    }
}
