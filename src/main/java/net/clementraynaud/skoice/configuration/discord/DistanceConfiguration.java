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

import net.clementraynaud.skoice.util.Lang;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;

import static net.clementraynaud.skoice.util.DataGetters.*;

public class DistanceConfiguration {

    private DistanceConfiguration() {
    }

    public static Message getHorizontalRadiusConfigurationMessage() {
        EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: " + Lang.Discord.CONFIGURATION_EMBED_TITLE.print())
                .setColor(Color.ORANGE)
                .addField(":left_right_arrow: " + Lang.Discord.HORIZONTAL_RADIUS_EMBED_TITLE.print(), Lang.Discord.HORIZONTAL_RADIUS_EMBED_DESCRIPTION.print(), false)
                .addField(":keyboard: " + Lang.Discord.ENTER_A_VALUE_FIELD_TITLE.print(), Lang.Discord.ENTER_A_VALUE_FIELD_DESCRIPTION.print().replace("{value}", String.valueOf(getHorizontalRadius())), false);
        return new MessageBuilder().setEmbeds(embed.build())
                .setActionRows(ActionRow.of(Button.secondary("mode", "← " + Lang.Discord.BACK_BUTTON_LABEL.print()),
                        Button.danger("close", Lang.Discord.CLOSE_BUTTON_LABEL.print()).withEmoji(Emoji.fromUnicode("U+2716")))).build();
    }

    public static Message getVerticalRadiusConfigurationMessage() {
        EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: " + Lang.Discord.CONFIGURATION_EMBED_TITLE.print())
                .setColor(Color.ORANGE)
                .addField(":arrow_up_down: " + Lang.Discord.VERTICAL_RADIUS_EMBED_TITLE.print(), Lang.Discord.VERTICAL_RADIUS_EMBED_DESCRIPTION.print(), false)
                .addField(":keyboard: " + Lang.Discord.ENTER_A_VALUE_FIELD_TITLE.print(), Lang.Discord.ENTER_A_VALUE_FIELD_DESCRIPTION.print().replace("{value}", String.valueOf(getVerticalRadius())), false);
        return new MessageBuilder().setEmbeds(embed.build())
                .setActionRows(ActionRow.of(Button.secondary("mode", "← " + Lang.Discord.BACK_BUTTON_LABEL.print()),
                        Button.danger("close", Lang.Discord.CLOSE_BUTTON_LABEL.print()).withEmoji(Emoji.fromUnicode("U+2716")))).build();
    }
}
