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
import net.clementraynaud.skoice.common.system.team.PartiesTeamProvider;
import net.clementraynaud.skoice.common.system.team.VanillaTeamProvider;
import net.dv8tion.jda.api.components.selections.SelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;

import java.util.Arrays;
import java.util.List;

public class TeamProviderSelector extends Selector {

    public TeamProviderSelector(Skoice plugin) {
        super(plugin);
    }

    @Override
    public SelectMenu get() {
        List<SelectOption> options = Arrays.asList(
                SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.teams.team-provider.select-menu.select-option.vanilla.label"), VanillaTeamProvider.ID)
                        .withDescription(super.plugin.getBot().getLang().getMessage("select-option.default.description"))
                        .withEmoji(MenuEmoji.MINECRAFT.get()),
                SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.teams.team-provider.select-menu.select-option.parties.label"), PartiesTeamProvider.ID)
                        .withEmoji(MenuEmoji.PARTIES.get()));

        String defaultValue = super.plugin.getConfigYamlFile().getString(ConfigField.TEAM_PROVIDER.toString());

        return StringSelectMenu.create("team-provider-selection")
                .addOptions(options)
                .setDefaultValues(defaultValue).build();
    }
}
