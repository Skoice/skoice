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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class WorldOverrideWorldsSelector extends Selector {

    public WorldOverrideWorldsSelector(Skoice plugin, String overrideId) {
        super(plugin, overrideId);
    }

    public static List<String> getSelectableWorlds(Skoice plugin, WorldOverride override) {
        Set<String> worlds = new LinkedHashSet<>(override == null ? new ArrayList<>() : override.getWorlds());
        worlds.addAll(plugin.getWorlds());

        List<String> selectable = new ArrayList<>(worlds);
        return selectable.size() > SelectMenu.OPTIONS_MAX_AMOUNT
                ? selectable.subList(0, SelectMenu.OPTIONS_MAX_AMOUNT)
                : selectable;
    }

    @Override
    public SelectMenu get() {
        WorldOverride override = super.plugin.getConfigYamlFile().getWorldOverrides().get(super.getOverrideId());
        List<String> selectableWorlds = WorldOverrideWorldsSelector.getSelectableWorlds(super.plugin, override);

        List<SelectOption> options = new ArrayList<>();
        List<String> defaultValues = new ArrayList<>();
        for (String world : selectableWorlds) {
            options.add(SelectOption.of(world, world)
                    .withEmoji(MenuEmoji.MAP.get()));

            if (override != null && override.getWorlds().contains(world)) {
                defaultValues.add(world);
            }
        }

        boolean disabled = false;
        if (options.isEmpty()) {
            options.add(SelectOption.of("Unavailable", "unavailable")
                    .withEmoji(MenuEmoji.X.get()));
            disabled = true;
        }

        return StringSelectMenu.create(super.scopeId(WorldOverrides.WORLDS_SELECT_ID))
                .setPlaceholder(super.plugin.getBot().getLang().getMessage("menu.world-override-worlds.select-menu.placeholder"))
                .addOptions(options)
                .setRequiredRange(0, options.size())
                .setDisabled(disabled)
                .setDefaultValues(defaultValues).build();
    }
}
