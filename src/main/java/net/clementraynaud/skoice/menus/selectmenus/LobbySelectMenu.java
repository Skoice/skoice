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
import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LobbySelectMenu extends SelectMenu {

    private static final String GENERATE_OPTION_ID = "GENERATE";
    private static final String REFRESH_OPTION_ID = "REFRESH";

    public LobbySelectMenu() {
        super(true);
    }

    @Override
    public SelectionMenu get() {
        List<VoiceChannel> voiceChannels = new ArrayList<>(Bot.getJda().getVoiceChannels());
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
                        .withEmoji(MenuEmoji.SOUND.getEmojiFromUnicode()));
            }
            optionIndex++;
        }
        options.add(SelectOption.of(DiscordLang.NEW_VOICE_CHANNEL_SELECT_OPTION_LABEL.toString(), LobbySelectMenu.GENERATE_OPTION_ID)
                .withDescription(DiscordLang.NEW_VOICE_CHANNEL_SELECT_OPTION_DESCRIPTION.toString())
                .withEmoji(MenuEmoji.HEAVY_PLUS_SIGN.getEmojiFromUnicode()));
        if (options.size() == 23) {
            options.add(SelectOption.of(DiscordLang.TOO_MANY_OPTIONS_SELECT_OPTION_LABEL.toString(), LobbySelectMenu.REFRESH_OPTION_ID)
                    .withDescription(DiscordLang.TOO_MANY_OPTIONS_SELECT_OPTION_DESCRIPTION.toString())
                    .withEmoji(MenuEmoji.WARNING.getEmojiFromUnicode()));
        }
        if (Skoice.getPlugin().isBotReady()) {
            return SelectionMenu.create(Menu.LOBBY.name() + "_SELECTION")
                    .addOptions(options)
                    .setDefaultValues(Collections.singleton(Skoice.getPlugin().getConfig().getString(Config.LOBBY_ID_FIELD))).build();
        } else {
            return SelectionMenu.create(Menu.LOBBY.name() + "_SELECTION")
                    .setPlaceholder(DiscordLang.LOBBY_SELECT_MENU_PLACEHOLDER.toString())
                    .addOptions(options).build();
        }
    }
}
