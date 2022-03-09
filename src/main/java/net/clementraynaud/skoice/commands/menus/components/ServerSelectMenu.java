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

package net.clementraynaud.skoice.commands.menus.components;

import net.clementraynaud.skoice.commands.menus.Menu;
import net.clementraynaud.skoice.commands.menus.MenuEmoji;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.ArrayList;
import java.util.List;

import static net.clementraynaud.skoice.bot.Bot.getJda;

public class ServerSelectMenu {

    public SelectionMenu getComponent() {
        List<Guild> servers = new ArrayList<>(getJda().getGuilds());
        List<SelectOption> options = new ArrayList<>();
        int optionIndex = 0;
        while (optionIndex < 24 && servers.size() > optionIndex) {
            options.add(SelectOption.of(servers.get(optionIndex).getName(), servers.get(optionIndex).getId())
                    .withEmoji(MenuEmoji.FILE_CABINET.getEmojifromUnicode()));
            optionIndex++;
        }
        if (options.size() == 24) {
            options.add(SelectOption.of(DiscordLang.TOO_MANY_OPTIONS_SELECT_OPTION_LABEL.toString(), "refresh")
                    .withDescription(DiscordLang.TOO_MANY_OPTIONS_SELECT_OPTION_DESCRIPTION.toString())
                    .withEmoji(MenuEmoji.WARNING_SIGN.getEmojifromUnicode()));
        }
        return SelectionMenu.create(Menu.SERVER.name() + "_SELECTION")
                .setPlaceholder(DiscordLang.SERVER_SELECT_MENU_PLACEHOLDER.toString())
                .addOptions(options).build();
    }
}
