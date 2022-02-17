/*
 * Copyright 2020, 2021 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

import static net.clementraynaud.skoice.util.DataGetters.getActionBarAlert;

public class ActionBarAlertConfiguration {

    private ActionBarAlertConfiguration() {
    }

    public static Message getActionBarAlertConfigurationMessage() {
        EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: " + Discord.CONFIGURATION_EMBED_TITLE)
                .setColor(Color.ORANGE)
                .addField(":exclamation: " + Discord.ACTION_BAR_ALERT_EMBED_TITLE, Discord.ACTION_BAR_ALERT_EMBED_DESCRIPTION.toString(), false);
        List<ActionRow> actionRows = new ArrayList<>();
        actionRows.add(ActionRow.of(SelectionMenu.create("action-bar-alert")
                .addOptions(SelectOption.of(Discord.ENABLED_SELECT_OPTION_LABEL.toString(), "true")
                                .withDescription(Discord.DEFAULT_SELECT_OPTION_DESCRIPTION.toString()).withEmoji(Emoji.fromUnicode("U+2714")),
                        SelectOption.of(Discord.DISABLED_SELECT_OPTION_LABEL.toString(), "false").withEmoji(Emoji.fromUnicode("U+2716")))
                .setDefaultValues(Collections.singleton(String.valueOf(getActionBarAlert()))).build()));
        actionRows.add(ActionRow.of(Button.secondary("advanced-settings", "← " + Discord.BACK_BUTTON_LABEL),
                Button.danger("close", Discord.CLOSE_BUTTON_LABEL.toString()).withEmoji(Emoji.fromUnicode("U+2716"))));
        return new MessageBuilder().setEmbeds(embed.build())
                .setActionRows(actionRows).build();
    }
}
