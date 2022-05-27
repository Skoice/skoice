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

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Lang {

    private static final String CHAT_PREFIX = ChatColor.LIGHT_PURPLE + "Skoice " + ChatColor.DARK_GRAY + "•" + ChatColor.GRAY;

    private YamlConfiguration englishMessages;
    private YamlConfiguration messages;

    public void load(LangInfo langInfo) {
        if (this.englishMessages == null) {
            InputStreamReader englishLangFile = new InputStreamReader(this.getClass().getClassLoader()
                    .getResourceAsStream("lang/" + LangInfo.EN + ".yml"), StandardCharsets.UTF_8);
            this.englishMessages = YamlConfiguration.loadConfiguration(englishLangFile);
        }
        this.messages = new YamlConfiguration();
        if (langInfo != LangInfo.EN) {
            InputStreamReader langFile = new InputStreamReader(this.getClass().getClassLoader()
                    .getResourceAsStream("lang/" + langInfo + ".yml"), StandardCharsets.UTF_8);
            this.messages = YamlConfiguration.loadConfiguration(langFile);
        }
    }

    public String getMessage(String path) {
        String message = this.messages.contains(path) ? this.messages.getString(path) : this.englishMessages.getString(path);
        if (path.startsWith("minecraft.chat.") && message != null) {
            return ChatColor.translateAlternateColorCodes('&', String.format(message, Lang.CHAT_PREFIX));
        } else if (path.startsWith("minecraft.action-bar.") && message != null) {
            return ChatColor.translateAlternateColorCodes('&', message);
        }
        return message;
    }

    public String getMessage(String path, String... args) {
        String message = this.messages.contains(path) ? this.messages.getString(path) : this.englishMessages.getString(path);
        if (message == null) {
            return null;
        }
        if (path.startsWith("minecraft.chat.")) {
            String[] newArgs = new String[args.length + 1];
            newArgs[0] = Lang.CHAT_PREFIX;
            System.arraycopy(args, 0, newArgs, 1, args.length);
            return ChatColor.translateAlternateColorCodes('&', String.format(message, (Object[]) newArgs));
        }
        return String.format(message, (Object[]) args);
    }

    public boolean contains(String path) {
        return this.englishMessages.contains(path);
    }
}
