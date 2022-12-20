/*
 * Copyright 2020, 2021, 2022 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModeSelectMenu extends SelectMenu {

    private static final String VANILLA_MODE_ID = "vanilla-mode";
    private static final String MINIGAME_MODE_ID = "minigame-mode";

    public ModeSelectMenu(Skoice plugin) {
        super(plugin, false);
    }

    @Override
    public net.dv8tion.jda.api.interactions.components.selections.SelectMenu get() {
        List<SelectOption> modes = new ArrayList<>(Arrays.asList(SelectOption.of(super.plugin.getLang().getMessage("discord.menu.mode.select-menu.select-option.vanilla-mode.label"), ModeSelectMenu.VANILLA_MODE_ID)
                        .withDescription(super.plugin.getLang().getMessage("discord.menu.mode.select-menu.select-option.vanilla-mode.description"))
                        .withEmoji(MenuEmoji.MAP.get()),
                SelectOption.of(super.plugin.getLang().getMessage("discord.menu.mode.select-menu.select-option.minigame-mode.label"), ModeSelectMenu.MINIGAME_MODE_ID)
                        .withDescription(super.plugin.getLang().getMessage("discord.menu.mode.select-menu.select-option.minigame-mode.description"))
                        .withEmoji(MenuEmoji.CROSSED_SWORDS.get())));
        String defaultValue = null;
        if (super.plugin.getConfigYamlFile().getInt(ConfigField.HORIZONTAL_RADIUS.toString()) == 80
                && super.plugin.getConfigYamlFile().getInt(ConfigField.VERTICAL_RADIUS.toString()) == 40) {
            defaultValue = ModeSelectMenu.VANILLA_MODE_ID;
        } else if (super.plugin.getConfigYamlFile().getInt(ConfigField.HORIZONTAL_RADIUS.toString()) == 40
                && super.plugin.getConfigYamlFile().getInt(ConfigField.VERTICAL_RADIUS.toString()) == 20) {
            defaultValue = ModeSelectMenu.MINIGAME_MODE_ID;
        }
        return net.dv8tion.jda.api.interactions.components.selections.SelectMenu.create("mode-selection")
                .setPlaceholder(super.plugin.getBot().getStatus() != BotStatus.READY
                        ? super.plugin.getLang().getMessage("discord.menu.mode.select-menu.placeholder")
                        : super.plugin.getLang().getMessage("discord.menu.mode.select-menu.alternative-placeholder"))
                .addOptions(modes)
                .setDefaultValues(defaultValue != null ? Collections.singleton(defaultValue) : Collections.emptyList()).build();
    }
}
