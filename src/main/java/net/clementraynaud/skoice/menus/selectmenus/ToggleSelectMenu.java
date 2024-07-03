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
import net.clementraynaud.skoice.util.ConfigurationUtil;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Collections;

public class ToggleSelectMenu extends SelectMenu {

    private static final String ENABLED_OPTION_ID = "true";
    private static final String DISABLED_OPTION_ID = "false";

    private final String componentId;

    public ToggleSelectMenu(Skoice plugin, String componentId) {
        super(plugin);
        this.componentId = componentId;
    }

    @Override
    public net.dv8tion.jda.api.interactions.components.selections.SelectMenu get() {
        boolean selectedValue = this.plugin.getConfigYamlFile().getBoolean(this.componentId);
        YamlConfiguration configuration = ConfigurationUtil.loadResource(this.getClass().getName(), "config.yml");
        if (configuration == null) {
            return null;
        }
        boolean defaultValue = configuration.getBoolean(this.componentId);
        return StringSelectMenu.create(this.componentId)
                .addOptions(SelectOption.of(super.plugin.getBot().getLang().getMessage("select-option.enabled.label"), ToggleSelectMenu.ENABLED_OPTION_ID)
                                .withDescription(defaultValue ? super.plugin.getBot().getLang().getMessage("select-option.default.description") : null)
                                .withEmoji(MenuEmoji.HEAVY_CHECK_MARK.get()),
                        SelectOption.of(super.plugin.getBot().getLang().getMessage("select-option.disabled.label"), ToggleSelectMenu.DISABLED_OPTION_ID)
                                .withDescription(!defaultValue ? super.plugin.getBot().getLang().getMessage("select-option.default.description") : null)
                                .withEmoji(MenuEmoji.HEAVY_MULTIPLICATION_X.get()))
                .setDefaultValues(Collections.singleton(String.valueOf(selectedValue))).build();
    }
}
