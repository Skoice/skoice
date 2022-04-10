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

import net.clementraynaud.skoice.lang.LangFile;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.bukkit.configuration.ConfigurationSection;

public class MenuField {

    private final String name;
    private final MenuEmoji emoji;
    private final boolean inline;
    private final String value;

    public MenuField(ConfigurationSection field) {
        this.name = field.getName();
        this.emoji = MenuEmoji.valueOf(field.getString("emoji").toUpperCase());
        this.inline = field.getBoolean("inline");
        this.value = field.contains("value") ? field.getString("value") : null;
    }

    public MessageEmbed.Field toField(LangFile lang) {
        return new MessageEmbed.Field(this.emoji + this.getTitle(lang), this.getDescription(lang), this.inline);
    }

    private String getTitle(LangFile lang) {
        return lang.getMessage("discord.field." + this.name + ".title");
    }

    private String getDescription(LangFile lang) {
        if (this.value != null) {
            return lang.getMessage("discord.field." + this.name + ".description", this.value);
        }
        return lang.getMessage("discord.field." + this.name + ".description");
    }
}
