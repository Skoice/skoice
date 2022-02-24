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

package net.clementraynaud.skoice.commands.arguments;

import net.clementraynaud.skoice.lang.MinecraftLang;
import net.clementraynaud.skoice.util.MessageUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.bot.Bot.getJda;

public class ConfigureArgument {

    public void execute(CommandSender sender) {
        Player player = (Player) sender;
        if (getPlugin().isTokenSet() && getJda() != null) {
            if (getPlugin().isBotReady()) {
                player.sendMessage(MinecraftLang.ALREADY_CONFIGURED.toString());
            } else {
                player.sendMessage(MinecraftLang.INCOMPLETE_CONFIGURATION_OPERATOR_DISCORD.toString());
            }
        } else {
            try {
                TextComponent tutorialPage = new TextComponent("§bpage");
                MessageUtil.setHoverEvent(tutorialPage, "§8☀ §bOpen in web browser: §7https://github.com/carlodrift/skoice/wiki");
                tutorialPage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/carlodrift/skoice/wiki"));
                TextComponent tokenCommand = new TextComponent("§bhere");
                MessageUtil.setHoverEvent(tokenCommand,"§8☀ §bShortcut: §7/token");
                tokenCommand.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/token "));
                player.spigot().sendMessage(new ComponentBuilder("\n§dSkoice §8• §7Configuration (§fBot Creation§7)\n\n§8• §7First, you need to create a bot and invite it to your Discord server. Please follow the instructions on this ")
                        .append(tutorialPage)
                        .append("§7.\n§8• §7When you have retrieved its token, put it ").event((HoverEvent) null)
                        .append(tokenCommand)
                        .append("§7.\n§8• §7Once done, type \"§e/configure§7\" on your Discord server to go to the next step.\n").event((HoverEvent) null).create());
            } catch (NoSuchMethodError e) {
                player.sendMessage(MinecraftLang.BOT_CREATION_LINK.toString());
            }
        }
    }
}
