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
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.lang.Lang;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LobbySelectMenu extends SelectMenu {

    private static final String GENERATE_OPTION_ID = "generate";
    private static final String REFRESH_OPTION_ID = "refresh";

    private final Config config;
    private final Bot bot;

    public LobbySelectMenu(Lang lang, Config config, Bot bot) {
        super(lang, true);
        this.config = config;
        this.bot = bot;
    }

    @Override
    public SelectionMenu get() {
        List<VoiceChannel> voiceChannels = new ArrayList<>(this.bot.getJda().getVoiceChannels());
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
        options.add(SelectOption.of(super.lang.getMessage("discord.menu.lobby.select-menu.select-option.new-voice-channel.label"), LobbySelectMenu.GENERATE_OPTION_ID)
                .withDescription(super.lang.getMessage("discord.menu.lobby.select-menu.select-option.new-voice-channel.description"))
                .withEmoji(MenuEmoji.HEAVY_PLUS_SIGN.getEmojiFromUnicode()));
        if (options.size() == 23) {
            options.add(SelectOption.of(super.lang.getMessage("discord.select-option.too-many-options.label"), LobbySelectMenu.REFRESH_OPTION_ID)
                    .withDescription(super.lang.getMessage("discord.select-option.too-many-options.description"))
                    .withEmoji(MenuEmoji.WARNING.getEmojiFromUnicode()));
        }
        if (this.bot.isReady()) {
            return SelectionMenu.create("lobby-selection")
                    .addOptions(options)
                    .setDefaultValues(Collections.singleton(this.config.getFile().getString(ConfigField.LOBBY_ID.get()))).build();
        } else {
            return SelectionMenu.create("lobby-selection")
                    .setPlaceholder(super.lang.getMessage("discord.menu.lobby.select-menu.placeholder"))
                    .addOptions(options).build();
        }
    }
}
