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

package net.clementraynaud.skoice.configuration.discord;

import net.clementraynaud.skoice.lang.Discord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.util.DataGetters.getLanguage;

public class LanguageSelection {

    private LanguageSelection() {
    }

    public static Message getLanguageSelectionMessage() {
        EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: " + Discord.CONFIGURATION_EMBED_TITLE)
                .setColor(Color.ORANGE)
                .addField(":globe_with_meridians: " + Discord.LANGUAGE_EMBED_TITLE, Discord.LANGUAGE_EMBED_DESCRIPTION.toString(), false);
        List<SelectOption> options = new ArrayList<>();
        options.add(SelectOption.of("English", "EN")
                .withDescription(Discord.DEFAULT_SELECT_OPTION_DESCRIPTION.toString()).withEmoji(Emoji.fromUnicode("U+1F1ECU+1F1E7")));
        options.add(SelectOption.of("Français", "FR")
                .withEmoji(Emoji.fromUnicode("U+1F1EBU+1F1F7")));
        List<ActionRow> actionRows = new ArrayList<>();
        if (getPlugin().isBotReady()) {
            actionRows.add(ActionRow.of(SelectionMenu.create("languages")
                    .addOptions(options)
                    .setDefaultValues(Collections.singleton(getLanguage())).build()));
            actionRows.add(ActionRow.of(Button.secondary("settings", "← " + Discord.BACK_BUTTON_LABEL),
                    Button.danger("close", Discord.CLOSE_BUTTON_LABEL.toString()).withEmoji(Emoji.fromUnicode("U+2716"))));
        } else {
            actionRows.add(ActionRow.of(SelectionMenu.create("languages")
                    .setPlaceholder(Discord.LANGUAGE_SELECT_MENU_PLACEHOLDER.toString())
                    .addOptions(options).build()));
            actionRows.add(ActionRow.of(Button.secondary("close", Discord.CONFIGURE_LATER_BUTTON_LABEL.toString()).withEmoji(Emoji.fromUnicode("U+1F552"))));
        }
        return new MessageBuilder().setEmbeds(embed.build()).setActionRows(actionRows).build();
    }
}
