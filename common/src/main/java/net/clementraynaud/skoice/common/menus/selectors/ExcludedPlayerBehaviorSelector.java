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
import net.dv8tion.jda.api.components.selections.SelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExcludedPlayerBehaviorSelector extends Selector {

    public static final String IN_RANGE = "in-range";
    public static final String SAME_WORLD = "same-world";
    public static final String DISABLED = "false";

    public ExcludedPlayerBehaviorSelector(Skoice plugin, String overrideId) {
        super(plugin, overrideId);
    }

    @Override
    public SelectMenu get() {
        List<SelectOption> options = new ArrayList<>(Arrays.asList(SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.excluded-players.excluded-player-behavior.select-menu.same-world.label"), ExcludedPlayerBehaviorSelector.SAME_WORLD)
                        .withEmoji(MenuEmoji.MAP.get())
                        .withDescription(super.plugin.getBot().getLang().getMessage("select-option.default.description")),
                SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.excluded-players.excluded-player-behavior.select-menu.in-range.label"), ExcludedPlayerBehaviorSelector.IN_RANGE)
                        .withEmoji(MenuEmoji.BUSTS_IN_SILHOUETTE.get()),
                SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.excluded-players.excluded-player-behavior.select-menu.disabled.label"), ExcludedPlayerBehaviorSelector.DISABLED)
                        .withEmoji(MenuEmoji.MUTED.get())));

        String defaultValue = super.getScope().getString(ConfigField.EXCLUDED_PLAYERS_COMMUNICATION.toString());

        return StringSelectMenu.create(super.scopeId("excluded-player-behavior-selection"))
                .addOptions(options)
                .setDefaultValues(defaultValue).build();
    }
}
