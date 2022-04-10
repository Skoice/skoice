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

import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.lang.LangFile;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.ArrayList;
import java.util.List;

public class ServerSelectMenu extends SelectMenu {

    private final LangFile lang;
    private final Bot bot;

    public ServerSelectMenu(LangFile lang, Bot bot) {
        super(true);
        this.lang = lang;
        this.bot = bot;
    }

    @Override
    public SelectionMenu get() {
        List<Guild> servers = new ArrayList<>(this.bot.getJda().getGuilds());
        List<SelectOption> options = new ArrayList<>();
        int optionIndex = 0;
        while (optionIndex < 24 && servers.size() > optionIndex) {
            options.add(SelectOption.of(servers.get(optionIndex).getName(), servers.get(optionIndex).getId())
                    .withEmoji(MenuEmoji.FILE_CABINET.getEmojiFromUnicode()));
            optionIndex++;
        }
        if (options.size() == 24) {
            options.add(SelectOption.of(this.lang.getMessage("discord.select-option.too-many-options.label"), "refresh")
                    .withDescription(this.lang.getMessage("discord.select-option.too-many-options.description"))
                    .withEmoji(MenuEmoji.WARNING.getEmojiFromUnicode()));
        }
        return SelectionMenu.create("server-selection")
                .setPlaceholder(this.lang.getMessage("discord.menu.server.select-menu.placeholder"))
                .addOptions(options).build();
    }
}
