/*
 * Copyright 2020, 2021, 2022, 2023, 2024 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.ChatColor;

import java.util.Arrays;

public class SpigotLang extends Lang {

    private static final String CHAT_PREFIX = ChatColor.LIGHT_PURPLE + "Skoice " + ChatColor.DARK_GRAY + "•" + ChatColor.GRAY;

    @Override
    protected String getPath(LangInfo langInfo) {
        return "spigot/lang/" + langInfo + ".yml";
    }

    @Override
    public String getMessage(String path) {
        String message = super.getMessage(path);
        if (message == null) {
            return null;
        }
        if (path.startsWith("chat.")) {
            return ChatColor.translateAlternateColorCodes('&', String.format(message, SpigotLang.CHAT_PREFIX));
        } else if ((path.startsWith("action-bar.") || path.startsWith("interaction."))) {
            return ChatColor.translateAlternateColorCodes('&', message);
        }
        return message;
    }

    @Override
    public String getMessage(String path, String... args) {
        String message = this.active.contains(path)
                ? this.active.getString(path)
                : this.english.getString(path);
        if (message == null) {
            return null;
        }
        args = Arrays.stream(args)
                .map(arg -> arg.replace(String.valueOf(ChatColor.COLOR_CHAR), ""))
                .toArray(String[]::new);
        if (path.startsWith("chat.")) {
            String[] newArgs = new String[args.length + 1];
            newArgs[0] = SpigotLang.CHAT_PREFIX;
            System.arraycopy(args, 0, newArgs, 1, args.length);
            return String.format(ChatColor.translateAlternateColorCodes('&', message), (Object[]) newArgs);
        } else if (path.startsWith("interaction.")) {
            return String.format(ChatColor.translateAlternateColorCodes('&', message), (Object[]) args);
        }
        return String.format(message, (Object[]) args);
    }

    public Component getMessage(String path, Component... components) {
        String[] strings = super.active.contains(path)
                ? super.active.getStringList(path).toArray(new String[0])
                : super.english.getStringList(path).toArray(new String[0]);
        TextComponent.Builder message = Component.text().content(ChatColor.translateAlternateColorCodes('&',
                String.format(strings[0], SpigotLang.CHAT_PREFIX)));
        for (int i = 0; i < components.length; i++) {
            message.append(components[i])
                    .append(Component.text(ChatColor.translateAlternateColorCodes('&', strings[i + 1]))
                            .hoverEvent(null));
        }
        return message.build();
    }

    public Component getComponentMessage(String path) {
        return Component.text(ChatColor.translateAlternateColorCodes('&', this.getMessage(path)));
    }

    public Component getComponentMessage(String path, String... args) {
        return Component.text(ChatColor.translateAlternateColorCodes('&', this.getMessage(path, args)));
    }
}
