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
import net.clementraynaud.skoice.config.ConfigurationField;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModeSelectMenu extends SelectMenu {

    private static final String VANILLA_MODE_ID = "vanilla-mode";
    private static final String MINIGAME_MODE_ID = "minigame-mode";
    private static final String CUSTOMIZE_ID = "customize";

    private final Skoice plugin;

    private final boolean customizeRadius;

    public ModeSelectMenu(Skoice plugin, boolean customizeRadius) {
        super(plugin.getLang(), false);
        this.plugin = plugin;
        this.customizeRadius = customizeRadius;
    }

    @Override
    public net.dv8tion.jda.api.interactions.components.selections.SelectMenu get() {
        List<SelectOption> modes = new ArrayList<>(Arrays.asList(SelectOption.of(super.lang.getMessage("discord.menu.mode.select-menu.select-option.vanilla-mode.label"), ModeSelectMenu.VANILLA_MODE_ID)
                        .withDescription(super.lang.getMessage("discord.menu.mode.select-menu.select-option.vanilla-mode.description"))
                        .withEmoji(MenuEmoji.MAP.get()),
                SelectOption.of(super.lang.getMessage("discord.menu.mode.select-menu.select-option.minigame-mode.label"), ModeSelectMenu.MINIGAME_MODE_ID)
                        .withDescription(super.lang.getMessage("discord.menu.mode.select-menu.select-option.minigame-mode.description"))
                        .withEmoji(MenuEmoji.CROSSED_SWORDS.get())));
        if (this.plugin.getBot().isReady()) {
            String defaultValue;
            if (this.plugin.getConfiguration().getFile().getInt(ConfigurationField.HORIZONTAL_RADIUS.toString()) == 80
                    && this.plugin.getConfiguration().getFile().getInt(ConfigurationField.VERTICAL_RADIUS.toString()) == 40
                    && !this.customizeRadius) {
                defaultValue = ModeSelectMenu.VANILLA_MODE_ID;
                modes.add(SelectOption.of(this.lang.getMessage("discord.menu.mode.select-menu.select-option.customize.label"), ModeSelectMenu.CUSTOMIZE_ID)
                        .withDescription(this.lang.getMessage("discord.menu.mode.select-menu.select-option.customize.description"))
                        .withEmoji(MenuEmoji.PENCIL2.get()));
            } else if (this.plugin.getConfiguration().getFile().getInt(ConfigurationField.HORIZONTAL_RADIUS.toString()) == 40
                    && this.plugin.getConfiguration().getFile().getInt(ConfigurationField.VERTICAL_RADIUS.toString()) == 20
                    && !this.customizeRadius) {
                defaultValue = ModeSelectMenu.MINIGAME_MODE_ID;
                modes.add(SelectOption.of(super.lang.getMessage("discord.menu.mode.select-menu.select-option.customize.label"), ModeSelectMenu.CUSTOMIZE_ID)
                        .withDescription(super.lang.getMessage("discord.menu.mode.select-menu.select-option.customize.description"))
                        .withEmoji(MenuEmoji.PENCIL2.get()));
            } else {
                defaultValue = ModeSelectMenu.CUSTOMIZE_ID;
                modes.add(SelectOption.of(super.lang.getMessage("discord.menu.mode.select-menu.select-option.customize.label"), ModeSelectMenu.CUSTOMIZE_ID)
                        .withDescription(super.lang.getMessage("discord.menu.mode.select-menu.select-option.customize.alternative-description",
                                this.plugin.getConfiguration().getFile().getString(ConfigurationField.HORIZONTAL_RADIUS.toString()),
                                this.plugin.getConfiguration().getFile().getString(ConfigurationField.VERTICAL_RADIUS.toString())))
                        .withEmoji(MenuEmoji.PENCIL2.get()));
            }
            return net.dv8tion.jda.api.interactions.components.selections.SelectMenu.create("mode-selection")
                    .setPlaceholder(super.lang.getMessage("discord.menu.mode.select-menu.placeholder"))
                    .addOptions(modes)
                    .setDefaultValues(Collections.singleton(defaultValue)).build();
        } else {
            return net.dv8tion.jda.api.interactions.components.selections.SelectMenu.create("mode-selection")
                    .setPlaceholder(super.lang.getMessage("discord.menu.mode.select-menu.placeholder"))
                    .addOptions(modes).build();
        }
    }
}
