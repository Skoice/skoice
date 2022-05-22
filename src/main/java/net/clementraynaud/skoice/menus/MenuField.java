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
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.bukkit.configuration.ConfigurationSection;

public class MenuField {

    private final String name;
    private final MenuEmoji emoji;
    private final boolean inline;

    public MenuField(ConfigurationSection field) {
        this.name = field.getName();
        this.emoji = MenuEmoji.valueOf(field.getString("emoji").toUpperCase());
        this.inline = field.getBoolean("inline");
    }

    public MessageEmbed.Field toField(Lang lang, String value) {
        return new MessageEmbed.Field(this.emoji + this.getTitle(lang), this.getDescription(lang, value), this.inline);
    }

    public MessageEmbed.Field toField(Lang lang) {
        return this.toField(lang, null);
    }

    private String getTitle(Lang lang) {
        return lang.getMessage("discord.field." + this.name + ".title");
    }

    private String getDescription(Lang lang, String value) {
        if (value != null) {
            return lang.getMessage("discord.field." + this.name + ".description", value);
        }
        return lang.getMessage("discord.field." + this.name + ".description");
    }
}
