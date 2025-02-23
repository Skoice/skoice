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

package net.clementraynaud.skoice.common.menus.selectors;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.bot.BotStatus;
import net.clementraynaud.skoice.common.menus.MenuEmoji;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.clementraynaud.skoice.common.util.MapUtil;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RangeSelector extends Selector {

    private static final String LONG_RANGE_MODE_ID = "long-range-mode";
    private static final String SHORT_RANGE_MODE_ID = "short-range-mode";
    private static final String CUSTOMIZED_ID = "customized";

    public RangeSelector(Skoice plugin) {
        super(plugin);
    }

    @Override
    public SelectMenu get() {
        List<SelectOption> options = new ArrayList<>(Arrays.asList(SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.range.select-menu.select-option.long-range-mode.label"), RangeSelector.LONG_RANGE_MODE_ID)
                        .withDescription(super.plugin.getBot().getLang().getMessage("menu.range.select-menu.select-option.description", MapUtil.of("horizontal-radius", "80", "vertical-radius", "40")))
                        .withEmoji(MenuEmoji.LOUD_SOUND.get()),
                SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.range.select-menu.select-option.short-range-mode.label"), RangeSelector.SHORT_RANGE_MODE_ID)
                        .withDescription(super.plugin.getBot().getLang().getMessage("menu.range.select-menu.select-option.description", MapUtil.of("horizontal-radius", "40", "vertical-radius", "20")))
                        .withEmoji(MenuEmoji.SOUND.get())
        ));

        SelectOption customizedOption = SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.range.select-menu.select-option.customized.label"),
                        RangeSelector.CUSTOMIZED_ID)
                .withEmoji(MenuEmoji.PENCIL2.get());

        String defaultValue = null;
        int horizontalRadius = super.plugin.getConfigYamlFile().getInt(ConfigField.HORIZONTAL_RADIUS.toString());
        int verticalRadius = super.plugin.getConfigYamlFile().getInt(ConfigField.VERTICAL_RADIUS.toString());
        if (horizontalRadius == 80 && verticalRadius == 40) {
            defaultValue = RangeSelector.LONG_RANGE_MODE_ID;
        } else if (horizontalRadius == 40 && verticalRadius == 20) {
            defaultValue = RangeSelector.SHORT_RANGE_MODE_ID;
        } else if (super.plugin.getBot().getStatus() != BotStatus.NO_RADIUS) {
            defaultValue = RangeSelector.CUSTOMIZED_ID;
            customizedOption = customizedOption.withDescription(super.plugin.getBot().getLang().getMessage("menu.range.select-menu.select-option.description",
                    MapUtil.of("horizontal-radius", String.valueOf(horizontalRadius),
                            "vertical-radius", String.valueOf(verticalRadius)
                    )
            ));
        }

        options.add(customizedOption);

        return StringSelectMenu.create("range-selection")
                .setPlaceholder(super.plugin.getBot().getLang().getMessage("menu.range.select-menu.placeholder"))
                .addOptions(options)
                .setDefaultValues(defaultValue != null ? Collections.singleton(defaultValue) : Collections.emptyList()).build();
    }
}
