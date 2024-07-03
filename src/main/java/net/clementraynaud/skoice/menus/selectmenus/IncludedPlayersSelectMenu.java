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
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IncludedPlayersSelectMenu extends SelectMenu {

    public IncludedPlayersSelectMenu(Skoice plugin) {
        super(plugin);
    }

    @Override
    public net.dv8tion.jda.api.interactions.components.selections.SelectMenu get() {
        List<SelectOption> options = new ArrayList<>(Arrays.asList(SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.included-players.select-menu.players-on-death-screen-included.label"), ConfigField.PLAYERS_ON_DEATH_SCREEN_INCLUDED.toString())
                        .withEmoji(MenuEmoji.SKULL.get()),
                SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.included-players.select-menu.spectators-included.label"), ConfigField.SPECTATORS_INCLUDED.toString())
                        .withEmoji(MenuEmoji.GHOST.get())));
        List<String> defaultValues = new ArrayList<>();
        if (super.plugin.getConfigYamlFile().getBoolean(ConfigField.PLAYERS_ON_DEATH_SCREEN_INCLUDED.toString())) {
            defaultValues.add(ConfigField.PLAYERS_ON_DEATH_SCREEN_INCLUDED.toString());
        }
        if (super.plugin.getConfigYamlFile().getBoolean(ConfigField.SPECTATORS_INCLUDED.toString())) {
            defaultValues.add(ConfigField.SPECTATORS_INCLUDED.toString());
        }
        return StringSelectMenu.create("included-players-selection")
                .setPlaceholder(super.plugin.getBot().getLang().getMessage("menu.included-players.select-menu.placeholder"))
                .addOptions(options)
                .setRequiredRange(0, options.size())
                .setDefaultValues(defaultValues).build();
    }
}
