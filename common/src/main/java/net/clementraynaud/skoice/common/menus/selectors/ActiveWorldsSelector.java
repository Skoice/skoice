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
import net.clementraynaud.skoice.common.menus.MenuEmoji;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.dv8tion.jda.api.components.selections.SelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;

import java.util.ArrayList;
import java.util.List;

public class ActiveWorldsSelector extends Selector {

    public ActiveWorldsSelector(Skoice plugin) {
        super(plugin);
    }

    @Override
    public SelectMenu get() {
        List<SelectOption> options = new ArrayList<>();
        List<String> disabledWorlds = super.plugin.getConfigYamlFile().getStringList(ConfigField.DISABLED_WORLDS.toString());
        List<String> defaultValues = new ArrayList<>();
        for (String world : super.plugin.getWorlds()) {
            options.add(SelectOption.of(world, world)
                    .withEmoji(MenuEmoji.MAP.get()));

            if (!disabledWorlds.contains(world)) {
                defaultValues.add(world);
            }
        }

        boolean disabled = false;
        if (options.isEmpty()) {
            options.add(SelectOption.of("Unavailable", "unavailable")
                    .withEmoji(MenuEmoji.X.get()));
            defaultValues.add("unavailable");
            disabled = true;
        }

        return StringSelectMenu.create("active-worlds-selection")
                .setPlaceholder(super.plugin.getBot().getLang().getMessage("menu.active-worlds.select-menu.placeholder"))
                .addOptions(options)
                .setRequiredRange(0, options.size())
                .setDisabled(disabled)
                .setDefaultValues(defaultValues).build();
    }
}
