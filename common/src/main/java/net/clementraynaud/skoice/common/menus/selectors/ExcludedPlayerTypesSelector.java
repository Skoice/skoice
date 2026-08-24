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

public class ExcludedPlayerTypesSelector extends Selector {

    public ExcludedPlayerTypesSelector(Skoice plugin, String overrideId) {
        super(plugin, overrideId);
    }

    @Override
    public SelectMenu get() {
        List<SelectOption> options = new ArrayList<>(Arrays.asList(SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.excluded-players.excluded-player-types.select-menu.players-on-death-screen-excluded.label"), ConfigField.PLAYERS_ON_DEATH_SCREEN_EXCLUDED.toString())
                        .withEmoji(MenuEmoji.SKULL.get()),
                SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.excluded-players.excluded-player-types.select-menu.spectators-excluded.label"), ConfigField.SPECTATORS_EXCLUDED.toString())
                        .withEmoji(MenuEmoji.GHOST.get())));
        List<String> defaultValues = new ArrayList<>();
        if (super.getScope().getBoolean(ConfigField.PLAYERS_ON_DEATH_SCREEN_EXCLUDED.toString())) {
            defaultValues.add(ConfigField.PLAYERS_ON_DEATH_SCREEN_EXCLUDED.toString());
        }
        if (super.getScope().getBoolean(ConfigField.SPECTATORS_EXCLUDED.toString())) {
            defaultValues.add(ConfigField.SPECTATORS_EXCLUDED.toString());
        }
        return StringSelectMenu.create(super.scopeId("excluded-player-types-selection"))
                .setPlaceholder(super.plugin.getBot().getLang().getMessage("menu.excluded-players.excluded-player-types.select-menu.placeholder"))
                .addOptions(options)
                .setRequiredRange(0, options.size())
                .setDefaultValues(defaultValues).build();
    }
}
