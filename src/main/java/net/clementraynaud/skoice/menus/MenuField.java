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

import net.clementraynaud.skoice.lang.DiscordLang;
import net.dv8tion.jda.api.entities.MessageEmbed;

public enum MenuField {
    TROUBLESHOOTING(MenuEmoji.SCREWDRIVER, true),
    CONTRIBUTE(MenuEmoji.HAMMER, false),
    VANILLA_MODE(MenuEmoji.MAP, true),
    MINIGAME_MODE(MenuEmoji.CROSSED_SWORDS, true),
    CUSTOMIZE(MenuEmoji.PENCIL2, true),
    ENTER_A_VALUE(MenuEmoji.KEYBOARD, false),
    SKOICE_2_1(MenuEmoji.CLIPBOARD, false),
    UPCOMING_FEATURES(MenuEmoji.CALENDAR_SPIRAL, false);

    private final MenuEmoji unicode;
    private final boolean inline;

    MenuField(MenuEmoji unicode, boolean inline) {
        this.unicode = unicode;
        this.inline = inline;
    }

    public MessageEmbed.Field get() {
        return new MessageEmbed.Field(this.unicode + this.getTitle(), this.getDescription(), this.inline);
    }

    public MessageEmbed.Field get(int value) {
        return new MessageEmbed.Field(this.unicode + this.getTitle(), this.getDescription(value), this.inline);
    }

    private String getTitle() {
        return DiscordLang.valueOf(this.name() + "_FIELD_TITLE").toString();
    }

    private String getDescription() {
        return DiscordLang.valueOf(this.name() + "_FIELD_DESCRIPTION").toString();
    }

    private String getDescription(int value) {
        return String.format(DiscordLang.valueOf(this.name() + "_FIELD_DESCRIPTION").toString(), value);
    }
}
