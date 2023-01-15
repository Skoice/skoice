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

package net.clementraynaud.skoice.menus.selectmenus;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RangeSelectMenu extends SelectMenu {

    private static final String LONG_RANGE_MODE_ID = "long-range-mode";
    private static final String SHORT_RANGE_MODE_ID = "short-range-mode";

    public RangeSelectMenu(Skoice plugin) {
        super(plugin, false);
    }

    @Override
    public net.dv8tion.jda.api.interactions.components.selections.SelectMenu get() {
        List<SelectOption> modes = new ArrayList<>(Arrays.asList(SelectOption.of(super.plugin.getLang().getMessage("discord.menu.range.select-menu.select-option.long-range-mode.label"), RangeSelectMenu.LONG_RANGE_MODE_ID)
                        .withDescription(super.plugin.getLang().getMessage("discord.menu.range.select-menu.select-option.long-range-mode.description"))
                        .withEmoji(MenuEmoji.LOUD_SOUND.get()),
                SelectOption.of(super.plugin.getLang().getMessage("discord.menu.range.select-menu.select-option.short-range-mode.label"), RangeSelectMenu.SHORT_RANGE_MODE_ID)
                        .withDescription(super.plugin.getLang().getMessage("discord.menu.range.select-menu.select-option.short-range-mode.description"))
                        .withEmoji(MenuEmoji.SOUND.get())));
        String defaultValue = null;
        if (super.plugin.getConfigYamlFile().getInt(ConfigField.HORIZONTAL_RADIUS.toString()) == 80
                && super.plugin.getConfigYamlFile().getInt(ConfigField.VERTICAL_RADIUS.toString()) == 40) {
            defaultValue = RangeSelectMenu.LONG_RANGE_MODE_ID;
        } else if (super.plugin.getConfigYamlFile().getInt(ConfigField.HORIZONTAL_RADIUS.toString()) == 40
                && super.plugin.getConfigYamlFile().getInt(ConfigField.VERTICAL_RADIUS.toString()) == 20) {
            defaultValue = RangeSelectMenu.SHORT_RANGE_MODE_ID;
        }
        return net.dv8tion.jda.api.interactions.components.selections.SelectMenu.create("mode-selection")
                .setPlaceholder(super.plugin.getBot().getStatus() != BotStatus.READY
                        ? super.plugin.getLang().getMessage("discord.menu.range.select-menu.placeholder")
                        : super.plugin.getLang().getMessage("discord.menu.range.select-menu.alternative-placeholder"))
                .addOptions(modes)
                .setDefaultValues(defaultValue != null ? Collections.singleton(defaultValue) : Collections.emptyList()).build();
    }
}
