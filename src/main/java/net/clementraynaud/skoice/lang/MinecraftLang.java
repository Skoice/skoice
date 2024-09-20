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

import net.clementraynaud.skoice.util.MapUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.ChatColor;

import java.util.Map;

public class MinecraftLang extends Lang {

    private static final String CHAT_PREFIX = ChatColor.LIGHT_PURPLE + "Skoice "
            + ChatColor.DARK_GRAY + "• "
            + ChatColor.GRAY;

    @Override
    protected String getPath(LangInfo langInfo) {
        return "minecraft/lang/" + langInfo + ".yml";
    }

    @Override
    protected void loadFormatter() {
        super.formatter.set("title", ChatColor.WHITE.toString());
        super.formatter.set("dark", ChatColor.DARK_GRAY.toString());
        super.formatter.set("success", ChatColor.GREEN.toString());
        super.formatter.set("error", ChatColor.RED.toString());
        super.formatter.set("highlight", ChatColor.YELLOW.toString());
        super.formatter.set("interactive", ChatColor.AQUA.toString());
        super.formatter.set("default", ChatColor.GRAY.toString());

        super.formatter.set("configure-minecraft-command", "/skoice configure");
        super.formatter.set("token-minecraft-command", "/skoice token");
        super.formatter.set("language-minecraft-command", "/skoice language");
        super.formatter.set("tooltips-minecraft-command", "/skoice tooltips");
        super.formatter.set("link-minecraft-command", "/skoice link");
        super.formatter.set("unlink-minecraft-command", "/skoice unlink");

        super.formatter.set("configure-discord-command", "/configure");
        super.formatter.set("link-discord-command", "/link");
        super.formatter.set("unlink-discord-command", "/unlink");
        super.formatter.set("invite-discord-command", "/invite");

        super.formatter.set("spigotmc-url", "https://www.spigotmc.org/resources/skoice-proximity-voice-chat.82861");
        super.formatter.set("creation-guide-url", "https://github.com/Skoice/skoice/wiki/Creating-a-Discord-Bot-for-Skoice");
        super.formatter.set("migration-guide-url", "https://github.com/Skoice/skoice/wiki/Migrating-to-Skoice-3");

        super.formatter.set("lang-list", LangInfo.getJoinedList());
    }

    @Override
    public String getMessage(String path, Map<String, String> args) {
        String message = super.getRawMessage(path);

        message = message.replaceAll("\"(.+?)\"",
                        "\"" + super.formatter.get("highlight") + "$1" + super.formatter.get("default") + "\"")
                .replaceAll("\\{.+?-url}",
                        super.formatter.get("interactive") + "$0" + super.formatter.get("default"));

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
        TextComponent.Builder result = Component.text();

        for (String string : strings) {
            String[] parts = string.split(":");

            if (parts.length == 1
                    || !this.formatter.contains(parts[1])
                    || !"suggest".equals(parts[0]) && !"run".equals(parts[0]) && !"open".equals(parts[0])) {
                result.append(Component.text(this.formatter.get("default") + string).hoverEvent(null));
                continue;
            }

            String value = this.formatter.get(parts[1]);

            if ("suggest".equals(parts[0])) {
                result.append(Component.text(this.formatter.get("interactive") + this.getMessage("interaction.here"))
                        .hoverEvent(HoverEvent.showText(Component.text(this.getMessage("interaction.shortcut",
                                MapUtil.of("minecraft-command", value)))))
                        .clickEvent(ClickEvent.suggestCommand(value + " ")));
            } else if ("run".equals(parts[0])) {
                result.append(Component.text(this.formatter.get("interactive") + this.getMessage("interaction.here"))
                        .hoverEvent(HoverEvent.showText(Component.text(this.getMessage("interaction.execute",
                                MapUtil.of("minecraft-command", value)))))
                        .clickEvent(ClickEvent.runCommand(value)));
            } else if ("open".equals(parts[0])) {
                result.append(Component.text(this.formatter.get("interactive") + this.getMessage("interaction.this-page"))
                        .hoverEvent(HoverEvent.showText(Component.text(this.getMessage("interaction.link",
                                MapUtil.of("url", value)))))
                        .clickEvent(ClickEvent.openUrl(value)));
            }
        }

        return result.build();
    }
}
