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
import net.clementraynaud.skoice.commands.menus.MenuUnicode;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.bot.Bot.getJda;
import static net.clementraynaud.skoice.commands.menus.Menu.REFRESH_OPTION_ID;
import static net.clementraynaud.skoice.config.Config.LOBBY_ID_FIELD;

public class LobbySelectMenu {

    private static final String GENERATE_OPTION_ID = "GENERATE";

    public SelectionMenu getComponent() {
        List<VoiceChannel> voiceChannels = new ArrayList<>(getJda().getVoiceChannels());
        List<Category> categories = new ArrayList<>();
        for (VoiceChannel voiceChannel : voiceChannels) {
            categories.add(voiceChannel.getParent());
        }
        List<SelectOption> options = new ArrayList<>();
        int optionIndex = 0;
        while (optionIndex < 23 && voiceChannels.size() > optionIndex) {
            if (voiceChannels.get(optionIndex).getParent() != null) {
                options.add(SelectOption.of(voiceChannels.get(optionIndex).getName(), voiceChannels.get(optionIndex).getId())
                        .withDescription(categories.get(optionIndex).getName())
                        .withEmoji(MenuUnicode.SOUND.getEmoji()));
            }
            optionIndex++;
        }
        options.add(SelectOption.of(DiscordLang.NEW_VOICE_CHANNEL_SELECT_OPTION_LABEL.toString(), GENERATE_OPTION_ID)
                .withDescription(DiscordLang.NEW_VOICE_CHANNEL_SELECT_OPTION_DESCRIPTION.toString())
                .withEmoji(MenuUnicode.HEAVY_PLUS_SIGN.getEmoji()));
        if (options.size() == 23) {
            options.add(SelectOption.of(DiscordLang.TOO_MANY_OPTIONS_SELECT_OPTION_LABEL.toString(), REFRESH_OPTION_ID)
                    .withDescription(DiscordLang.TOO_MANY_OPTIONS_SELECT_OPTION_DESCRIPTION.toString())
                    .withEmoji(MenuUnicode.WARNING_SIGN.getEmoji()));
        }
        if (getPlugin().isBotReady()) {
            return SelectionMenu.create(Menu.LOBBY.name())
                    .addOptions(options)
                    .setDefaultValues(Collections.singleton(getPlugin().getConfig().getString(LOBBY_ID_FIELD))).build();
        } else {
            return SelectionMenu.create(Menu.LOBBY.name())
                    .setPlaceholder(DiscordLang.LOBBY_SELECT_MENU_PLACEHOLDER.toString())
                    .addOptions(options).build();
        }
    }
}
