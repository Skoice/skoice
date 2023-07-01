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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.ArrayList;
import java.util.List;

public class ServerSelectMenu extends SelectMenu {

    public ServerSelectMenu(Skoice plugin) {
        super(plugin, true);
    }

    @Override
    public net.dv8tion.jda.api.interactions.components.selections.SelectMenu get() {
        List<Guild> servers = new ArrayList<>(super.plugin.getBot().getJDA().getGuilds());
        List<SelectOption> options = new ArrayList<>();
        int optionIndex = 0;
        while (optionIndex < 24 && servers.size() > optionIndex) {
            options.add(SelectOption.of(servers.get(optionIndex).getName(), servers.get(optionIndex).getId())
                    .withEmoji(MenuEmoji.FILE_CABINET.get()));
            optionIndex++;
        }
        if (options.size() == 24) {
            options.add(SelectOption.of(super.plugin.getLang().getMessage("discord.select-option.too-many-options.label"), "refresh")
                    .withDescription(super.plugin.getLang().getMessage("discord.select-option.too-many-options.description"))
                    .withEmoji(MenuEmoji.WARNING.get()));
        }
        return StringSelectMenu.create("server-selection")
                .setPlaceholder(super.plugin.getLang().getMessage("discord.menu.server.select-menu.placeholder"))
                .addOptions(options).build();
    }
}
