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

package net.clementraynaud.skoice.menus.selectmenus;

import net.clementraynaud.skoice.menus.MenuEmoji;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.Collections;

public class ToggleSelectMenu implements SelectMenu {

    private static final String ENABLED_OPTION_ID = "true";
    private static final String DISABLED_OPTION_ID = "false";

    private final String componentID;
    private final boolean selectedValue;
    private final boolean defaultValue;

    public ToggleSelectMenu(String componentID, boolean selectedValue, boolean defaultValue) {
        this.componentID = componentID;
        this.selectedValue = selectedValue;
        this.defaultValue = defaultValue;
    }

    @Override
    public SelectionMenu get() {
        return SelectionMenu.create(componentID)
                .addOptions(SelectOption.of(DiscordLang.ENABLED_SELECT_OPTION_LABEL.toString(), ENABLED_OPTION_ID)
                                .withDescription(defaultValue ? DiscordLang.DEFAULT_SELECT_OPTION_DESCRIPTION.toString() : null)
                                .withEmoji(MenuEmoji.HEAVY_CHECK_MARK.getEmojiFromUnicode()),
                        SelectOption.of(DiscordLang.DISABLED_SELECT_OPTION_LABEL.toString(), DISABLED_OPTION_ID)
                                .withDescription(!defaultValue ? DiscordLang.DEFAULT_SELECT_OPTION_DESCRIPTION.toString() : null)
                                .withEmoji(MenuEmoji.HEAVY_MULTIPLICATION_X.getEmojiFromUnicode()))
                .setDefaultValues(Collections.singleton(String.valueOf(selectedValue))).build();
    }
}
