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

public class LoginNotificationSelector extends Selector {

    public static final String ALWAYS_REMIND = "always-remind";
    public static final String REMIND_ONCE = "remind-once";
    public static final String NEVER_REMIND = "never-remind";

    public LoginNotificationSelector(Skoice plugin) {
        super(plugin);
    }

    @Override
    public SelectMenu get() {
        List<SelectOption> options = new ArrayList<>(Arrays.asList(SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.login-notification.select-menu.always-remind.label"), LoginNotificationSelector.ALWAYS_REMIND)
                        .withEmoji(MenuEmoji.REPEAT.get()),
                SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.login-notification.select-menu.remind-once.label"), LoginNotificationSelector.REMIND_ONCE)
                        .withDescription(super.plugin.getBot().getLang().getMessage("select-option.default.description"))
                        .withEmoji(MenuEmoji.REPEAT_ONE.get()),
                SelectOption.of(super.plugin.getBot().getLang().getMessage("menu.login-notification.select-menu.never-remind.label"), LoginNotificationSelector.NEVER_REMIND)
                        .withEmoji(MenuEmoji.NO_BELL.get())));
        String defaultValue = super.plugin.getConfigYamlFile().getString(ConfigField.LOGIN_NOTIFICATION.toString());
        return StringSelectMenu.create("login-notification-selection")
                .addOptions(options)
                .setDefaultValues(defaultValue).build();
    }
}
