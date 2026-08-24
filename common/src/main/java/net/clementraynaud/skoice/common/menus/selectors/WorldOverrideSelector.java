/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025, 2026 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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
import net.clementraynaud.skoice.common.storage.config.WorldOverride;
import net.clementraynaud.skoice.common.storage.config.WorldOverrides;
import net.dv8tion.jda.api.components.selections.SelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorldOverrideSelector extends Selector {

    private static final int MAX_LABEL_LENGTH = 100;

    public WorldOverrideSelector(Skoice plugin) {
        super(plugin);
    }

    @Override
    public SelectMenu get() {
        WorldOverrides overrides = super.plugin.getConfigYamlFile().getWorldOverrides();
        if (overrides.isEmpty()) {
            return null;
        }

        List<SelectOption> options = new ArrayList<>();
        List<WorldOverride> all = overrides.getAll();
        for (int i = 0; i < all.size() && i < SelectMenu.OPTIONS_MAX_AMOUNT; i++) {
            WorldOverride override = all.get(i);
            String label = (i + 1) + ". " + override.getName();
            options.add(SelectOption.of(label.length() > WorldOverrideSelector.MAX_LABEL_LENGTH
                                    ? label.substring(0, WorldOverrideSelector.MAX_LABEL_LENGTH)
                                    : label,
                            override.getId())
                    .withEmoji(MenuEmoji.EARTH_AMERICAS.get()));
        }

        return StringSelectMenu.create(WorldOverrides.SELECT_ID)
                .setPlaceholder(super.plugin.getBot().getLang().getMessage("menu.world-overrides.select-menu.placeholder"))
                .addOptions(options)
                .setDefaultValues(Collections.emptyList()).build();
    }
}
