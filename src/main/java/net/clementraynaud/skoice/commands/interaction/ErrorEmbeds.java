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

package net.clementraynaud.skoice.commands.interaction;

import net.clementraynaud.skoice.lang.DiscordLang;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class ErrorEmbeds {

    private ErrorEmbeds() {
    }

    public static MessageEmbed getAccessDeniedEmbed() {
        return new EmbedBuilder().setTitle(":warning: " + DiscordLang.ERROR_EMBED_TITLE)
                .addField(":no_entry: " + DiscordLang.ACCESS_DENIED_FIELD_TITLE, DiscordLang.ACCESS_DENIED_FIELD_DESCRIPTION.toString(), false)
                .setColor(Color.RED).build();
    }

    public static MessageEmbed getTooManyInteractionsEmbed() {
        return new EmbedBuilder().setTitle(":warning: " + DiscordLang.ERROR_EMBED_TITLE)
                .addField(":chart_with_upwards_trend: " + DiscordLang.TOO_MANY_INTERACTIONS_FIELD_TITLE, DiscordLang.TOO_MANY_INTERACTIONS_FIELD_DESCRIPTION.toString(), false)
                .setColor(Color.RED).build();
    }
}
