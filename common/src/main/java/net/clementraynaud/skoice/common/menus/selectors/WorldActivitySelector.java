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
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.clementraynaud.skoice.common.storage.config.WorldOverride;
import net.clementraynaud.skoice.common.storage.config.WorldOverrides;
import net.dv8tion.jda.api.components.selections.SelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;

import java.util.Collections;
import java.util.List;

public class WorldActivitySelector extends Selector {

    private static final String ENABLED_OPTION_ID = "true";
    private static final String DISABLED_OPTION_ID = "false";

    public WorldActivitySelector(Skoice plugin, String overrideId) {
        super(plugin, overrideId);
    }

    @Override
    public SelectMenu get() {
        boolean inheritedValue = this.isInheritedActive();
        boolean active = super.getScope().isOverridden(WorldOverrides.ACTIVE_FIELD)
                ? super.getScope().getBoolean(WorldOverrides.ACTIVE_FIELD)
                : inheritedValue;
        String inheritedDescription = super.plugin.getBot().getLang().getMessage("select-option.inherited.description");

        return StringSelectMenu.create(super.scopeId(WorldOverrides.ACTIVE_SELECT_ID))
                .addOptions(SelectOption.of(super.plugin.getBot().getLang().getMessage("select-option.enabled.label"),
                                        WorldActivitySelector.ENABLED_OPTION_ID)
                                .withDescription(inheritedValue ? inheritedDescription : null)
                                .withEmoji(MenuEmoji.HEAVY_CHECK_MARK.get()),
                        SelectOption.of(super.plugin.getBot().getLang().getMessage("select-option.disabled.label"),
                                        WorldActivitySelector.DISABLED_OPTION_ID)
                                .withDescription(inheritedValue ? null : inheritedDescription)
                                .withEmoji(MenuEmoji.HEAVY_MULTIPLICATION_X.get()))
                .setDefaultValues(Collections.singleton(String.valueOf(active))).build();
    }

    private boolean isInheritedActive() {
        WorldOverride override = super.plugin.getConfigYamlFile().getWorldOverrides().get(super.getOverrideId());
        if (override == null || override.getWorlds().isEmpty()) {
            return true;
        }
        List<String> disabledWorlds = super.plugin.getConfigYamlFile()
                .getStringList(ConfigField.DISABLED_WORLDS.toString());
        return override.getWorlds().stream().anyMatch(world -> !disabledWorlds.contains(world));
    }
}
