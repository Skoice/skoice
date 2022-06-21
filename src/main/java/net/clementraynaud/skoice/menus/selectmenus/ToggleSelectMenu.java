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

import net.clementraynaud.skoice.lang.Lang;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.Collections;

public class ToggleSelectMenu extends SelectMenu {

    private static final String ENABLED_OPTION_ID = "true";
    private static final String DISABLED_OPTION_ID = "false";

    private final String componentId;
    private final boolean selectedValue;
    private final boolean defaultValue;

    public ToggleSelectMenu(Lang lang, String componentId, boolean selectedValue, boolean defaultValue) {
        super(lang, false);
        this.componentId = componentId;
        this.selectedValue = selectedValue;
        this.defaultValue = defaultValue;
    }

    @Override
    public net.dv8tion.jda.api.interactions.components.selections.SelectMenu get() {
        return net.dv8tion.jda.api.interactions.components.selections.SelectMenu.create(this.componentId)
                .addOptions(SelectOption.of(super.lang.getMessage("discord.select-option.enabled.label"), ToggleSelectMenu.ENABLED_OPTION_ID)
                                .withDescription(this.defaultValue ? super.lang.getMessage("discord.select-option.default.description") : null)
                                .withEmoji(MenuEmoji.HEAVY_CHECK_MARK.get()),
                        SelectOption.of(super.lang.getMessage("discord.select-option.disabled.label"), ToggleSelectMenu.DISABLED_OPTION_ID)
                                .withDescription(!this.defaultValue ? super.lang.getMessage("discord.select-option.default.description") : null)
                                .withEmoji(MenuEmoji.HEAVY_MULTIPLICATION_X.get()))
                .setDefaultValues(Collections.singleton(String.valueOf(this.selectedValue))).build();
    }
}
