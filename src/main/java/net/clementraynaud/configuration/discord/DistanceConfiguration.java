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

import java.awt.*;

import static net.clementraynaud.util.DataGetters.getHorizontalStrength;
import static net.clementraynaud.util.DataGetters.getVerticalStrength;

public class DistanceConfiguration {

    public static Message getHorizontalStrengthConfigurationMessage() {
        EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: Configuration")
                .setColor(Color.ORANGE)
                .addField(":left_right_arrow: Horizontal Radius", "This is the maximum horizontal distance between players.", false)
                .addField(":keyboard: Enter a Value", "This setting is currently set to " + (int) getHorizontalStrength() + " blocks.\nThe value must be between 1 and 1000.", false);
        return new MessageBuilder().setEmbeds(embed.build())
                .setActionRows(ActionRow.of(Button.secondary("mode", "← Back"),
                        Button.danger("close", "Close").withEmoji(Emoji.fromUnicode("U+2716")))).build();
    }

    public static Message getVerticalStrengthConfigurationMessage() {
        EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: Configuration")
                .setColor(Color.ORANGE)
                .addField(":arrow_up_down: Vertical Radius", "This is the maximum vertical distance between players.", false)
                .addField(":keyboard: Enter a Value", "This setting is currently set to " + (int) getVerticalStrength() + " blocks.\nThe value must be between 1 and 1000.", false);
        return new MessageBuilder().setEmbeds(embed.build())
                .setActionRows(ActionRow.of(Button.secondary("mode", "← Back"),
                        Button.danger("close", "Close").withEmoji(Emoji.fromUnicode("U+2716")))).build();
    }
}
