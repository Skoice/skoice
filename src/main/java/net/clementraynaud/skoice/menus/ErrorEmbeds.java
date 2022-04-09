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

package net.clementraynaud.skoice.menus;

import net.clementraynaud.skoice.lang.DiscordLang;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class ErrorEmbeds {

    private ErrorEmbeds() {
    }

    public static MessageEmbed getAccessDeniedEmbed() {
        return new EmbedBuilder().setTitle(MenuEmoji.WARNING + DiscordLang.ERROR_EMBED_TITLE.toString())
                .addField(MenuEmoji.NO_ENTRY + DiscordLang.ACCESS_DENIED_FIELD_TITLE.toString(),
                        DiscordLang.ACCESS_DENIED_FIELD_DESCRIPTION.toString(), false)
                .setColor(Color.RED).build();
    }

    public static MessageEmbed getTooManyInteractionsEmbed() {
        return new EmbedBuilder().setTitle(MenuEmoji.WARNING + DiscordLang.ERROR_EMBED_TITLE.toString())
                .addField(MenuEmoji.CHART_WITH_UPWARDS_TREND + DiscordLang.TOO_MANY_INTERACTIONS_FIELD_TITLE.toString(),
                        DiscordLang.TOO_MANY_INTERACTIONS_FIELD_DESCRIPTION.toString(), false)
                .setColor(Color.RED).build();
    }
}
