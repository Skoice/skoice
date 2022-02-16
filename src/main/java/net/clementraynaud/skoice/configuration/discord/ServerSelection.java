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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static net.clementraynaud.skoice.bot.Connection.getJda;

public class ServerSelection {

    private ServerSelection() {
    }

    public static Message getServerSelectionMessage() {
        List<Guild> servers = new ArrayList<>(getJda().getGuilds());
        List<SelectOption> options = new ArrayList<>();
        int optionIndex = 0;
        while (optionIndex < 24 && servers.size() > optionIndex) {
            options.add(SelectOption.of(servers.get(optionIndex).getName(), servers.get(optionIndex).getId())
                    .withEmoji(Emoji.fromUnicode("U+1F5C4")));
            optionIndex++;
        }
        if (options.size() == 24) {
            options.add(SelectOption.of(Discord.TOO_MANY_OPTIONS_SELECT_OPTION_LABEL.toString(), "refresh")
                    .withDescription(Discord.TOO_MANY_OPTIONS_SELECT_OPTION_DESCRIPTION.toString()).withEmoji(Emoji.fromUnicode("U+26A0")));
        }
        List<ActionRow> actionRows = new ArrayList<>();
        actionRows.add(ActionRow.of(SelectionMenu.create("servers")
                .setPlaceholder(Discord.SERVER_SELECT_MENU_PLACEHOLDER.toString())
                .addOptions(options)
                .build()));
        actionRows.add(ActionRow.of(Button.primary("settings", "⟳ " + Discord.REFRESH_BUTTON_LABEL.toString()),
                Button.secondary("close", Discord.CONFIGURE_LATER_BUTTON_LABEL.toString()).withEmoji(Emoji.fromUnicode("U+1F552"))));
        EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: " + Discord.CONFIGURATION_EMBED_TITLE.toString())
                .setColor(Color.ORANGE)
                .addField(":file_cabinet: " + Discord.SERVER_EMBED_TITLE.toString(), Discord.SERVER_EMBED_DESCRIPTION.toString(), false);
        return new MessageBuilder().setEmbeds(embed.build()).setActionRows(actionRows).build();
    }
}
