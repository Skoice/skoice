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

package net.clementraynaud.skoice.menus.selectors;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReleaseChannelSelector extends Selector {

    public static final String PRODUCTION = "production";
    public static final String BETA = "beta";

    public ReleaseChannelSelector(Skoice plugin) {
        super(plugin);
    }

    @Override
    public SelectMenu get() {
        List<SelectOption> options = new ArrayList<>(Arrays.asList(SelectOption.of(super.plugin.getBot().getLang().getMessage("field.production-channel.title"), ReleaseChannelSelector.PRODUCTION)
                        .withEmoji(MenuEmoji.PACKAGE.get())
                        .withDescription(super.plugin.getBot().getLang().getMessage("select-option.default.description")),
                SelectOption.of(super.plugin.getBot().getLang().getMessage("field.beta-channel.title"), ReleaseChannelSelector.BETA)
                        .withEmoji(MenuEmoji.TEST_TUBE.get())
                        .withDescription(super.plugin.getBot().getLang().getMessage("select-option.undesirable.description"))));

        String defaultValue = super.plugin.getConfigYamlFile().getString(ConfigField.RELEASE_CHANNEL.toString());

        return StringSelectMenu.create("release-channel-selection")
                .addOptions(options)
                .setDefaultValues(defaultValue).build();
    }
}
