// Copyright 2020, 2021 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
// Copyright 2016, 2017, 2018, 2019, 2020, 2021 Austin "Scarsz" Shapiro

// This file is part of Skoice.

// Skoice is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Skoice is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Skoice.  If not, see <https://www.gnu.org/licenses/>.


package net.clementraynaud.configuration.discord;

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

import static net.clementraynaud.Skoice.getPlugin;
import static net.clementraynaud.util.DataGetters.getHorizontalStrength;
import static net.clementraynaud.util.DataGetters.getVerticalStrength;

public class ModeSelection {

    public static Message getModeSelectionMessage(boolean customize) {
        EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: Configuration")
                .setColor(Color.ORANGE);
        List<Button> buttons = new ArrayList<>();
        if (getPlugin().isBotConfigured()) {
            embed.addField(":video_game: Mode", "Let us choose the best settings for your personal use of Skoice. You can also customize the distances.", false);
            buttons.add(Button.secondary("advanced-settings", "← Back"));
        } else {
            embed.addField(":video_game: Mode", "Let us choose the best settings for your personal use of Skoice. You will still be able to customize the distances later.", false);
        }
        embed.addField(":map: Vanilla Mode", "Choose this mode if you plan to play open world game modes (longer distances).", true)
                .addField(":crossed_swords: Minigame Mode", "Choose this mode if you plan to play game modes that only require a limited area (shorter distances).", true);
        List<SelectOption> modes = new ArrayList<>();
        modes.add(SelectOption.of("Vanilla Mode", "vanilla-mode")
                .withDescription("Horizontal Radius: 80 blocks — Vertical Radius: 40 blocks").withEmoji(Emoji.fromUnicode("U+1F5FA")));
        modes.add(SelectOption.of("Minigame Mode", "minigame-mode")
                .withDescription("Horizontal Radius: 40 blocks — Vertical Radius: 20 blocks").withEmoji(Emoji.fromUnicode("U+2694")));
        List<ActionRow> actionRows = new ArrayList<>();
        if (getPlugin().isBotConfigured()) {
            embed.addField(":pencil2: Customize", "Set distances according to your personal preferences.", true);
            String defaultValue;
            if (getPlugin().getPlayerData().getInt("distance.horizontalStrength") == 80
                    && getPlugin().getPlayerData().getInt("distance.verticalStrength") == 40
                    && !customize) {
                defaultValue = "vanilla-mode";
                modes.add(SelectOption.of("Customize", "customize")
                        .withDescription("Set distances from 1 to 1000 blocks.").withEmoji(Emoji.fromUnicode("U+270F")));
            } else if (getPlugin().getPlayerData().getInt("distance.horizontalStrength") == 40
                    && getPlugin().getPlayerData().getInt("distance.verticalStrength") == 20
                    && !customize) {
                defaultValue = "minigame-mode";
                modes.add(SelectOption.of("Customize", "customize")
                        .withDescription("Set distances from 1 to 1000 blocks.").withEmoji(Emoji.fromUnicode("U+270F")));
            } else {
                defaultValue = "customize";
                modes.add(SelectOption.of("Customize", "customize")
                        .withDescription("Horizontal Radius: " + (int) getHorizontalStrength() + " blocks — Vertical Radius: " + (int) getVerticalStrength() + " blocks").withEmoji(Emoji.fromUnicode("U+270F")));
                buttons.add(Button.primary("horizontal-radius", "Horizontal Radius").withEmoji(Emoji.fromUnicode("U+2194")));
                buttons.add(Button.primary("vertical-radius", "Vertical Radius").withEmoji(Emoji.fromUnicode("U+2195")));
            }
            buttons.add(Button.danger("close", "Close").withEmoji(Emoji.fromUnicode("U+2716")));
            actionRows.add(ActionRow.of(SelectionMenu.create("modes")
                    .setPlaceholder("Select a Mode")
                    .addOptions(modes)
                    .setDefaultValues(Collections.singleton(defaultValue)).build()));
        } else {
            buttons.add(Button.secondary("close", "Configure Later").withEmoji(Emoji.fromUnicode("U+1F552")));
            actionRows.add(ActionRow.of(SelectionMenu.create("modes")
                    .setPlaceholder("Select a Mode")
                    .addOptions(modes)
                    .build()));
        }
        actionRows.add(ActionRow.of(buttons));
        return new MessageBuilder().setEmbeds(embed.build())
                .setActionRows(actionRows).build();
    }
}
