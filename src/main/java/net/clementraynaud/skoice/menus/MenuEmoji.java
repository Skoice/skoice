/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

import net.dv8tion.jda.api.entities.emoji.Emoji;

public enum MenuEmoji {

    ARROW_FORWARD("U+25B6"),
    BELL("U+1F514"),
    BUST_IN_SILHOUETTE("U+1F464"),
    BUSTS_IN_SILHOUETTE("U+1F465"),
    CARD_BOX("U+1F5C3"),
    CHART_WITH_UPWARDS_TREND("U+1F4C8"),
    CLOCK3("U+1F552"),
    ENVELOPE("U+2709"),
    FILE_CABINET("U+1F5C4"),
    GEAR("U+2699"),
    GHOST("U+1F47B"),
    GLOBE_WITH_MERIDIANS("U+1F310"),
    GREEN_HEART("U+1F49A"),
    HAMMER("U+1F528"),
    HEAVY_CHECK_MARK("U+2714"),
    HEAVY_MULTIPLICATION_X("U+2716"),
    HEAVY_PLUS_SIGN("U+2795"),
    INBOX_TRAY("U+1F4E5"),
    KEY("U+1F511"),
    LINK("U+1F517"),
    LOUD_SOUND("U+1F50A"),
    MAG("U+1F50D"),
    MUTE("U+1F507"),
    NO_ENTRY("U+26D4"),
    PENCIL2("U+270F"),
    REPEAT("U+1F501"),
    REPEAT_ONE("U+1F502"),
    SCREWDRIVER("U+1FA9B"),
    SKULL("U+1F480"),
    SOUND("U+1F509"),
    SPEECH_BALLOON("U+1F4AC"),
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
