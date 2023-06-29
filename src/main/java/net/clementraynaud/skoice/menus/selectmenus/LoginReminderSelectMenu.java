/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LoginReminderSelectMenu extends SelectMenu {

    private static final int ALWAYS_REMIND = 2;
    private static final int REMIND_ONCE = 1;
    private static final int NEVER_REMIND = 0;

    public LoginReminderSelectMenu(Skoice plugin) {
        super(plugin, false);
    }

    @Override
    public net.dv8tion.jda.api.interactions.components.selections.SelectMenu get() {
        List<SelectOption> options = new ArrayList<>(Arrays.asList(SelectOption.of(super.plugin.getLang().getMessage("discord.menu.login-reminder.select-menu.always-remind.label"), String.valueOf(LoginReminderSelectMenu.ALWAYS_REMIND))
                        .withEmoji(MenuEmoji.REPEAT.get()),
                SelectOption.of(super.plugin.getLang().getMessage("discord.menu.login-reminder.select-menu.remind-once.label"), String.valueOf(LoginReminderSelectMenu.REMIND_ONCE))
                        .withDescription(super.plugin.getLang().getMessage("discord.select-option.default.description"))
                        .withEmoji(MenuEmoji.REPEAT_ONE.get()),
                SelectOption.of(super.plugin.getLang().getMessage("discord.menu.login-reminder.select-menu.never-remind.label"), String.valueOf(LoginReminderSelectMenu.NEVER_REMIND))
                        .withEmoji(MenuEmoji.MUTE.get())));
        String defaultValue = super.plugin.getConfigYamlFile().getString(ConfigField.LOGIN_REMINDER.toString());
        return net.dv8tion.jda.api.interactions.components.selections.SelectMenu.create("login-reminder-selection")
                .addOptions(options)
                .setDefaultValues(Collections.singleton(defaultValue)).build();
    }
}
