/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.common.lang;

import net.clementraynaud.skoice.common.util.MapUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Map;

public class MinecraftLang extends Lang {

    private static final String CHAT_PREFIX = "<light_purple>" + "Skoice " + "<dark_gray>" + "• " + "<gray>";
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Override
    protected String getPath(LangInfo langInfo) {
        return "minecraft/lang/" + langInfo + ".yml";
    }

    protected void loadFormatter() {
        super.formatter.set("title-color", "<white>");
        super.formatter.set("dark-color", "<dark_gray>");
        super.formatter.set("success-color", "<green>");
        super.formatter.set("error-color", "<red>");
        super.formatter.set("highlight-color", "<yellow>");
        super.formatter.set("interactive-color", "<aqua>");
        super.formatter.set("default-color", "<gray>");

        super.formatter.set("skoice-minecraft-command", "/skoice");
        super.formatter.set("configure-minecraft-command", "/skoice configure");
        super.formatter.set("token-minecraft-command", "/skoice token");
        super.formatter.set("language-minecraft-command", "/skoice language");
        super.formatter.set("tooltips-minecraft-command", "/skoice tooltips");
        super.formatter.set("link-minecraft-command", "/skoice link");
        super.formatter.set("unlink-minecraft-command", "/skoice unlink");

        super.formatter.set("configure-discord-command", "/configure");
        super.formatter.set("link-discord-command", "/link");
        super.formatter.set("unlink-discord-command", "/unlink");

        super.formatter.set("spigotmc-url", "https://www.spigotmc.org/resources/skoice-proximity-voice-chat.82861");
        super.formatter.set("creation-guide-url", "https://github.com/Skoice/skoice/wiki/Creating-a-Discord-Bot-for-Skoice");
        super.formatter.set("migration-guide-url", "https://github.com/Skoice/skoice/wiki/Migrating-to-Skoice-3");

        super.formatter.set("lang-list", LangInfo.getJoinedList());
    }

    @Override
    public String getMessage(String path, Map<String, String> args) {
        String message = super.getRawMessage(path);

        if (message == null || message.trim().isEmpty()) {
            return String.format("!%s!", path);
        }

        message = message.replaceAll("[\"「](.+?)[\"」]",
                        "\"" + super.formatter.get("highlight-color") + "$1" + super.formatter.get("default-color") + "\"")
                .replaceAll("\\{([^{}]*?)-url}",
                        super.formatter.get("interactive-color") + "$0" + super.formatter.get("default-color"));

        message = this.formatter.format(message, args);

        if (path.startsWith("chat.")) {
            String lineBreak = " \n";
            if (message.startsWith(lineBreak)) {
                message = lineBreak + MinecraftLang.CHAT_PREFIX + message.substring(lineBreak.length());
            } else {
                message = MinecraftLang.CHAT_PREFIX + message;
            }
        }

        return message;
    }

    @Override
    public String getMessage(String path) {
        return this.getMessage(path, MapUtil.of());
    }

    public Component getInteractiveMessage(String path) {
        String message = this.getMessage(path);
        String[] strings = this.formatter.format(message, MapUtil.of()).split("\\{|}");
        Component result = Component.empty();

        for (String string : strings) {
            String[] parts = string.split(":");

            if (parts.length == 1
                    || !this.formatter.contains(parts[1])
                    || !"suggest".equals(parts[0]) && !"run".equals(parts[0]) && !"open".equals(parts[0])) {
                result = result.append(MinecraftLang.MINI_MESSAGE.deserialize(this.formatter.get("default-color") + string));
                continue;
            }

            String value = this.formatter.get(parts[1]);

            if ("suggest".equals(parts[0])) {
                Component hoverText = MinecraftLang.MINI_MESSAGE.deserialize(this.getMessage("interaction.shortcut",
                        MapUtil.of("minecraft-command", value)));
                Component clickableText = MinecraftLang.MINI_MESSAGE.deserialize(this.formatter.get("interactive-color") + this.getMessage("interaction.here"))
                        .hoverEvent(HoverEvent.showText(hoverText))
                        .clickEvent(ClickEvent.suggestCommand(value + " "));
                result = result.append(clickableText);
            } else if ("run".equals(parts[0])) {
                Component hoverText = MinecraftLang.MINI_MESSAGE.deserialize(this.getMessage("interaction.execute",
                        MapUtil.of("minecraft-command", value)));
                Component clickableText = MinecraftLang.MINI_MESSAGE.deserialize(this.formatter.get("interactive-color") + this.getMessage("interaction.here"))
                        .hoverEvent(HoverEvent.showText(hoverText))
                        .clickEvent(ClickEvent.runCommand(value));
                result = result.append(clickableText);
            } else if ("open".equals(parts[0])) {
                Component hoverText = MinecraftLang.MINI_MESSAGE.deserialize(this.getMessage("interaction.link",
                        MapUtil.of("url", value)));
                Component clickableText = MinecraftLang.MINI_MESSAGE.deserialize(this.formatter.get("interactive-color") + this.getMessage("interaction.this-page"))
                        .hoverEvent(HoverEvent.showText(hoverText))
                        .clickEvent(ClickEvent.openUrl(value));
                result = result.append(clickableText);
            }
        }

        return result;
    }

    public String getConsoleMessage(String path, Map<String, String> args) {
        String message = super.getRawMessage(path);

        if (message == null || message.trim().isEmpty()) {
            return String.format("!%s!", path);
        }

        String formattedMessage = this.formatter.format(message, args);
        return MinecraftLang.MINI_MESSAGE.stripTags(formattedMessage);
    }

    public String getConsoleMessage(String path) {
        return this.getConsoleMessage(path, MapUtil.of());
    }
}
