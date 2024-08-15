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

package net.clementraynaud.skoice.menus.selectmenus;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RangeSelectMenu extends SelectMenu {

    private static final String LONG_RANGE_MODE_ID = "long-range-mode";
    private static final String SHORT_RANGE_MODE_ID = "short-range-mode";
    private static final String CUSTOMIZED_ID = "customized";

    public RangeSelectMenu(Skoice plugin) {
        super(plugin);
    }

    @Override
    public net.dv8tion.jda.api.interactions.components.selections.SelectMenu get() {
        List<SelectOption> options = new ArrayList<>(Arrays.asList(SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.range.select-menu.select-option.long-range-mode.label"), RangeSelectMenu.LONG_RANGE_MODE_ID)
                        .withDescription(super.plugin.getBot().getLang().getMessage("menu.range.select-menu.select-option.description", "80", "40"))
                        .withEmoji(MenuEmoji.LOUD_SOUND.get()),
                SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.range.select-menu.select-option.short-range-mode.label"), RangeSelectMenu.SHORT_RANGE_MODE_ID)
                        .withDescription(super.plugin.getBot().getLang().getMessage("menu.range.select-menu.select-option.description", "40", "20"))
                        .withEmoji(MenuEmoji.SOUND.get())
        ));

        SelectOption customizedOption  = SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.range.select-menu.select-option.customized.label"),
                        RangeSelectMenu.CUSTOMIZED_ID)
                .withEmoji(MenuEmoji.PENCIL2.get());

        String defaultValue = null;
        int horizontalRadius = super.plugin.getConfigYamlFile().getInt(ConfigField.HORIZONTAL_RADIUS.toString());
        int verticalRadius = super.plugin.getConfigYamlFile().getInt(ConfigField.VERTICAL_RADIUS.toString());
        if (horizontalRadius == 80 && verticalRadius == 40) {
            defaultValue = RangeSelectMenu.LONG_RANGE_MODE_ID;
        } else if (verticalRadius == 40 && horizontalRadius == 20) {
            defaultValue = RangeSelectMenu.SHORT_RANGE_MODE_ID;
        } else if (super.plugin.getBot().getStatus() != BotStatus.NO_RADIUS) {
            defaultValue = RangeSelectMenu.CUSTOMIZED_ID;
            customizedOption = customizedOption.withDescription(super.plugin.getBot().getLang().getMessage("menu.range.select-menu.select-option.description",
                            String.valueOf(horizontalRadius),
                            String.valueOf(verticalRadius)));
        }

        options.add(customizedOption);

        return StringSelectMenu.create("range-selection")
                .setPlaceholder(super.plugin.getBot().getLang().getMessage("menu.range.select-menu.placeholder"))
                .addOptions(options)
                .setDefaultValues(defaultValue != null ? Collections.singleton(defaultValue) : Collections.emptyList()).build();
    }
}
