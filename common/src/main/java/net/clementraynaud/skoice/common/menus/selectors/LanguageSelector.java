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
import net.clementraynaud.skoice.common.lang.LangInfo;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.ArrayList;
import java.util.List;

public class LanguageSelector extends Selector {

    public LanguageSelector(Skoice plugin) {
        super(plugin);
    }

    @Override
    public SelectMenu get() {
        List<SelectOption> options = new ArrayList<>();
        for (LangInfo option : LangInfo.values()) {
            options.add(SelectOption.of(option.getFullName(), option.toString())
                    .withDescription(option.toString().equals(LangInfo.EN.toString())
                            ? super.plugin.getBot().getLang().getMessage("select-option.default.description")
                            : null)
                    .withEmoji(option.getEmoji()));
        }
        return StringSelectMenu.create("language-selection")
                .addOptions(options)
                .setDefaultValues(super.plugin.getConfigYamlFile()
                        .getString(ConfigField.LANG.toString())).build();
    }
}
