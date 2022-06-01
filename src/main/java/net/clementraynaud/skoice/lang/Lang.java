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

package net.clementraynaud.skoice.lang;

import net.clementraynaud.skoice.util.ConfigurationUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public class Lang {

    private static final String CHAT_PREFIX = ChatColor.LIGHT_PURPLE + "Skoice " + ChatColor.DARK_GRAY + "•" + ChatColor.GRAY;

    private YamlConfiguration englishMessages;
    private YamlConfiguration messages;

    public void load(LangInfo langInfo) {
        if (this.englishMessages == null) {
            this.englishMessages = ConfigurationUtils.loadResource(this.getClass().getName(), "lang/" + LangInfo.EN + ".yml");
        }
        this.messages = new YamlConfiguration();
        if (langInfo != LangInfo.EN) {
            this.messages = ConfigurationUtils.loadResource(this.getClass().getName(), "lang/" + langInfo + ".yml");
        }
    }

    public String getMessage(String path) {
        String message = this.messages.contains(path)
                ? this.messages.getString(path)
                : this.englishMessages.getString(path);
        if (message == null) {
            return null;
        }
        if (path.startsWith("minecraft.chat.")) {
            return ChatColor.translateAlternateColorCodes('&', String.format(message, Lang.CHAT_PREFIX));
        } else if ((path.startsWith("minecraft.action-bar.") || path.startsWith("minecraft.interaction."))) {
            return ChatColor.translateAlternateColorCodes('&', message);
        }
        return message;
    }

    public String getMessage(String path, String... args) {
        String message = this.messages.contains(path)
                ? this.messages.getString(path)
                : this.englishMessages.getString(path);
        if (message == null) {
            return null;
        }
        if (path.startsWith("minecraft.chat.")) {
            String[] newArgs = new String[args.length + 1];
            newArgs[0] = Lang.CHAT_PREFIX;
            System.arraycopy(args, 0, newArgs, 1, args.length);
            return ChatColor.translateAlternateColorCodes('&', String.format(message, (Object[]) newArgs));
        } else if (path.startsWith("minecraft.interaction.")) {
            return ChatColor.translateAlternateColorCodes('&', String.format(message, (Object[]) args));
        }
        return String.format(message, (Object[]) args);
    }

    public BaseComponent[] getMessage(String path, TextComponent... components) {
        String[] strings = this.messages.contains(path)
                ? this.messages.getStringList(path).toArray(new String[0])
                : this.englishMessages.getStringList(path).toArray(new String[0]);
        ComponentBuilder message = new ComponentBuilder(ChatColor.translateAlternateColorCodes( '&',
                String.format(strings[0], Lang.CHAT_PREFIX)));
        for (int i = 0; i < components.length; i++) {
            message.append(components[i])
                    .append(ChatColor.translateAlternateColorCodes('&', strings[i + 1])).event((HoverEvent) null);
        }
        return message.create();
    }

    public boolean contains(String path) {
        return this.englishMessages.contains(path);
    }

    public int getAmountOfArgsRequired(String message) {
        return StringUtils.countMatches(message, "%s");
    }
}
