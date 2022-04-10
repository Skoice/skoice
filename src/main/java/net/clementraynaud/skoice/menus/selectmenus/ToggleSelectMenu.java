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

import net.clementraynaud.skoice.lang.LangFile;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.Collections;

public class ToggleSelectMenu extends SelectMenu {

    private static final String ENABLED_OPTION_ID = "true";
    private static final String DISABLED_OPTION_ID = "false";

    private final LangFile lang;
    private final String componentID;
    private final boolean selectedValue;
    private final boolean defaultValue;

    public ToggleSelectMenu(LangFile lang, String componentID, boolean selectedValue, boolean defaultValue) {
        super(false);
        this.lang = lang;
        this.componentID = componentID;
        this.selectedValue = selectedValue;
        this.defaultValue = defaultValue;
    }

    @Override
    public SelectionMenu get() {
        return SelectionMenu.create(this.componentID)
                .addOptions(SelectOption.of(this.lang.getMessage("discord.select-option.enabled.label"), ToggleSelectMenu.ENABLED_OPTION_ID)
                                .withDescription(this.defaultValue ? this.lang.getMessage("discord.select-option.default.description") : null)
                                .withEmoji(MenuEmoji.HEAVY_CHECK_MARK.getEmojiFromUnicode()),
                        SelectOption.of(this.lang.getMessage("discord.select-option.disabled.label"), ToggleSelectMenu.DISABLED_OPTION_ID)
                                .withDescription(!this.defaultValue ? this.lang.getMessage("discord.select-option.default.description") : null)
                                .withEmoji(MenuEmoji.HEAVY_MULTIPLICATION_X.getEmojiFromUnicode()))
                .setDefaultValues(Collections.singleton(String.valueOf(this.selectedValue))).build();
    }
}
