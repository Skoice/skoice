/*
 * Copyright 2020, 2021 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
 * Copyright 2016, 2017, 2018, 2019, 2020, 2021 Austin "Scarsz" Shapiro
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
import static net.clementraynaud.skoice.util.DataGetters.getHorizontalRadius;
import static net.clementraynaud.skoice.util.DataGetters.getVerticalRadius;

public class ModeSelection {

    private ModeSelection() {
    }

    public static Message getModeSelectionMessage(boolean customize) {
        EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: " + Discord.CONFIGURATION_EMBED_TITLE)
                .setColor(Color.ORANGE);
        List<Button> buttons = new ArrayList<>();
        if (getPlugin().isBotReady()) {
            embed.addField(":video_game: " + Discord.MODE_EMBED_TITLE, Discord.MODE_EMBED_DESCRIPTION.toString(), false);
            buttons.add(Button.secondary("settings", "← " + Discord.BACK_BUTTON_LABEL));
        } else {
            embed.addField(":video_game: " + Discord.MODE_EMBED_TITLE, Discord.MODE_EMBED_ALTERNATIVE_DESCRIPTION.toString(), false);
        }
        embed.addField(":map: " + Discord.VANILLA_MODE_FIELD_TITLE, Discord.VANILLA_MODE_FIELD_DESCRIPTION.toString(), true)
                .addField(":crossed_swords: " + Discord.MINIGAME_MODE_FIELD_TITLE, Discord.MINIGAME_MODE_FIELD_DESCRIPTION.toString(), true);
        List<SelectOption> modes = new ArrayList<>();
        modes.add(SelectOption.of(Discord.VANILLA_MODE_FIELD_TITLE.toString(), "vanilla-mode")
                .withDescription(Discord.VANILLA_MODE_FIELD_ALTERNATIVE_DESCRIPTION.toString()).withEmoji(Emoji.fromUnicode("U+1F5FA")));
        modes.add(SelectOption.of(Discord.MINIGAME_MODE_FIELD_TITLE.toString(), "minigame-mode")
                .withDescription(Discord.MINIGAME_MODE_FIELD_ALTERNATIVE_DESCRIPTION.toString()).withEmoji(Emoji.fromUnicode("U+2694")));
        List<ActionRow> actionRows = new ArrayList<>();
        if (getPlugin().isBotReady()) {
            embed.addField(":pencil2: " + Discord.CUSTOMIZE_FIELD_TITLE, Discord.CUSTOMIZE_FIELD_DESCRIPTION.toString(), true);
            String defaultValue;
            if (getHorizontalRadius() == 80
                    && getVerticalRadius() == 40
                    && !customize) {
                defaultValue = "vanilla-mode";
                modes.add(SelectOption.of(Discord.CUSTOMIZE_FIELD_TITLE.toString(), "customize")
                        .withDescription(Discord.CUSTOMIZE_SELECT_MENU_DESCRIPTION.toString()).withEmoji(Emoji.fromUnicode("U+270F")));
            } else if (getHorizontalRadius() == 40
                    && getVerticalRadius() == 20
                    && !customize) {
                defaultValue = "minigame-mode";
                modes.add(SelectOption.of("Customize", "customize")
                        .withDescription("Set distances from 1 to 1000 blocks.").withEmoji(Emoji.fromUnicode("U+270F")));
            } else {
                defaultValue = "customize";
                modes.add(SelectOption.of(Discord.CUSTOMIZE_FIELD_TITLE.toString(), "customize")
                        .withDescription(Discord.CUSTOMIZE_SELECT_MENU_ALTERNATIVE_DESCRIPTION.toString().replace("{horizontalRadius}", String.valueOf(getHorizontalRadius())).replace("{verticalRadius}", String.valueOf(getVerticalRadius()))).withEmoji(Emoji.fromUnicode("U+270F")));
                buttons.add(Button.primary("horizontal-radius", Discord.HORIZONTAL_RADIUS_EMBED_TITLE.toString()).withEmoji(Emoji.fromUnicode("U+2194")));
                buttons.add(Button.primary("vertical-radius", Discord.VERTICAL_RADIUS_EMBED_TITLE.toString()).withEmoji(Emoji.fromUnicode("U+2195")));
            }
            buttons.add(Button.danger("close", Discord.CLOSE_BUTTON_LABEL.toString()).withEmoji(Emoji.fromUnicode("U+2716")));
            actionRows.add(ActionRow.of(SelectionMenu.create("modes")
                    .setPlaceholder(Discord.MODE_SELECT_OPTION_PLACEHOLDER.toString())
                    .addOptions(modes)
                    .setDefaultValues(Collections.singleton(defaultValue)).build()));
        } else {
            buttons.add(Button.secondary("close", Discord.CONFIGURE_LATER_BUTTON_LABEL.toString()).withEmoji(Emoji.fromUnicode("U+1F552")));
            actionRows.add(ActionRow.of(SelectionMenu.create("modes")
                    .setPlaceholder(Discord.MODE_SELECT_OPTION_PLACEHOLDER.toString())
                    .addOptions(modes)
                    .build()));
        }
        actionRows.add(ActionRow.of(buttons));
        return new MessageBuilder().setEmbeds(embed.build())
                .setActionRows(actionRows).build();
    }
}
