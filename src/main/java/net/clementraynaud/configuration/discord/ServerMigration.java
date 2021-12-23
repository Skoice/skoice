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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static net.clementraynaud.Bot.getJda;
import static net.clementraynaud.util.DataGetters.getGuild;

public class ServerMigration {

    private ServerMigration() {
    }

    public static Message getServerMigrationMessage() {
        List<Guild> servers = new ArrayList<>(getJda().getGuilds());
        List<SelectOption> options = new ArrayList<>();
        int optionIndex = 0;
        while (optionIndex < 24 && servers.size() > optionIndex) {
            options.add(SelectOption.of(servers.get(optionIndex).getName(), servers.get(optionIndex).getId()));
            optionIndex++;
        }
        if (options.size() == 24) {
            options.add(SelectOption.of("Unable to load more options", "refresh"));
        }
        List<ActionRow> actionRows = new ArrayList<>();
        actionRows.add(ActionRow.of(SelectionMenu.create("servers")
                .setPlaceholder("Select a Server")
                .addOptions(options)
                .setMinValues(1).setMaxValues(1).build()));
        actionRows.add(ActionRow.of(Button.secondary("settings", "← Back"),
                Button.primary("server", "⟳ Refresh"),
                Button.danger("close", "Close").withEmoji(Emoji.fromUnicode("U+2716"))));
        EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: Configuration")
                .setColor(Color.ORANGE)
                .addField(":file_cabinet: Server Migration", "This is the Discord server where Skoice is active.\nSelected: " + getGuild().getName(), false);
        return new MessageBuilder().setEmbeds(embed.build()).setActionRows(actionRows).build();
    }
}
