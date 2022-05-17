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

import net.clementraynaud.skoice.lang.Lang;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;

public class ErrorEmbed {

    private final Lang lang;

    public ErrorEmbed(Lang lang) {
        this.lang = lang;
    }

    public MessageEmbed getAccessDeniedEmbed() {
        return new EmbedBuilder().setTitle(MenuEmoji.WARNING + this.lang.getMessage("discord.menu.error.title"))
                .addField(MenuEmoji.NO_ENTRY + this.lang.getMessage("discord.menu.error.field.access-denied.title"),
                        this.lang.getMessage("discord.menu.error.field.access-denied.description"), false)
                .setColor(Color.RED).build();
    }

    public MessageEmbed getTooManyInteractionsEmbed() {
        return new EmbedBuilder().setTitle(MenuEmoji.WARNING + this.lang.getMessage("discord.menu.error.title"))
                .addField(MenuEmoji.CHART_WITH_UPWARDS_TREND + this.lang.getMessage("discord.menu.error.field.too-many-interactions.title"),
                        this.lang.getMessage("discord.menu.error.field.too-many-interactions.description"), false)
                .setColor(Color.RED).build();
    }
}
