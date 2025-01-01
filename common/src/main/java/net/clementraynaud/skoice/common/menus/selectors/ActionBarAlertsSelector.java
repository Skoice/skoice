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
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActionBarAlertsSelector extends Selector {

    public static final String CONNECTING_ALERT = "connecting-alert";
    public static final String DISCONNECTING_ALERT = "disconnecting-alert";

    public ActionBarAlertsSelector(Skoice plugin) {
        super(plugin);
    }

    @Override
    public SelectMenu get() {
        List<SelectOption> options = new ArrayList<>(Arrays.asList(SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.action-bar-alerts.select-menu.connecting-alert.label"), ActionBarAlertsSelector.CONNECTING_ALERT)
                        .withEmoji(MenuEmoji.INBOX_TRAY.get()),
                SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.action-bar-alerts.select-menu.disconnecting-alert.label"), ActionBarAlertsSelector.DISCONNECTING_ALERT)
                        .withDescription(super.plugin.getBot().getLang().getMessage("select-option.default.description"))
                        .withEmoji(MenuEmoji.OUTBOX_TRAY.get())));
        List<String> defaultValues = new ArrayList<>();
        if (super.plugin.getConfigYamlFile().getBoolean(ConfigField.CONNECTING_ALERT.toString())) {
            defaultValues.add(ConfigField.CONNECTING_ALERT.toString());
        }
        if (super.plugin.getConfigYamlFile().getBoolean(ConfigField.DISCONNECTING_ALERT.toString())) {
            defaultValues.add(ConfigField.DISCONNECTING_ALERT.toString());
        }
        return StringSelectMenu.create("action-bar-alerts-selection")
                .setPlaceholder(super.plugin.getBot().getLang().getMessage("menu.action-bar-alerts.select-menu.placeholder"))
                .addOptions(options)
                .setRequiredRange(0, options.size())
                .setDefaultValues(defaultValues).build();
    }
}
