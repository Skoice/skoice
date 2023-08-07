/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

import net.clementraynaud.skoice.util.ConfigurationUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Arrays;

public class Lang {

    private static final String CHAT_PREFIX = ChatColor.LIGHT_PURPLE + "Skoice " + ChatColor.DARK_GRAY + "•" + ChatColor.GRAY;

    private YamlConfiguration englishMessages;
    private YamlConfiguration messages;

    public void load(LangInfo langInfo) {
        if (this.englishMessages == null) {
            this.englishMessages = ConfigurationUtil.loadResource(this.getClass().getName(), "lang/" + LangInfo.EN + ".yml");
        }
        this.messages = new YamlConfiguration();
        if (langInfo != LangInfo.EN) {
            this.messages = ConfigurationUtil.loadResource(this.getClass().getName(), "lang/" + langInfo + ".yml");
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

    public Component getComponentMessage(String path) {
        return Component.text(ChatColor.translateAlternateColorCodes('&', this.getMessage(path)));
    }

    public Component getComponentMessage(String path, String... args) {
        return Component.text(ChatColor.translateAlternateColorCodes('&', this.getMessage(path, args)));
    }

    public String getMessage(String path, String... args) {
        String message = this.messages.contains(path)
                ? this.messages.getString(path)
                : this.englishMessages.getString(path);
        if (message == null) {
            return null;
        }
        if (path.startsWith("minecraft.")) {
            args = Arrays.stream(args)
                    .map(arg -> arg.replace(String.valueOf(ChatColor.COLOR_CHAR), ""))
                    .toArray(String[]::new);
            if (path.startsWith("minecraft.chat.")) {
                String[] newArgs = new String[args.length + 1];
                newArgs[0] = Lang.CHAT_PREFIX;
                System.arraycopy(args, 0, newArgs, 1, args.length);
                return String.format(ChatColor.translateAlternateColorCodes('&', message), (Object[]) newArgs);
            } else if (path.startsWith("minecraft.interaction.")) {
                return String.format(ChatColor.translateAlternateColorCodes('&', message), (Object[]) args);
            }
        }
        return String.format(message, (Object[]) args);
    }

    public Component getMessage(String path, Component... components) {
        String[] strings = this.messages.contains(path)
                ? this.messages.getStringList(path).toArray(new String[0])
                : this.englishMessages.getStringList(path).toArray(new String[0]);
        TextComponent.Builder message = Component.text().content(ChatColor.translateAlternateColorCodes('&',
                String.format(strings[0], Lang.CHAT_PREFIX)));
        for (int i = 0; i < components.length; i++) {
            message.append(components[i])
                    .append(Component.text(ChatColor.translateAlternateColorCodes('&', strings[i + 1]))
                            .hoverEvent(null));
        }
        return message.build();
    }

    public boolean contains(String path) {
        return this.englishMessages.contains(path);
    }

    public int getAmountOfArgsRequired(String message) {
        return message.split("%s").length - 1;
    }
}
