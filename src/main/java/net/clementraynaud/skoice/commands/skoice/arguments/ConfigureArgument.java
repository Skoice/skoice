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

package net.clementraynaud.skoice.commands.skoice.arguments;

import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.lang.Lang;
import net.clementraynaud.skoice.util.MessageUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfigureArgument extends Argument {

    public ConfigureArgument(Config config, Lang lang, Bot bot, CommandSender sender) {
        super(config, lang, bot, sender, ArgumentName.CONFIGURE.isAllowedInConsole(), ArgumentName.CONFIGURE.isRestrictedToOperators());
    }

    @Override
    public void run() {
        if (!this.canExecuteCommand()) {
            return;
        }
        Player player = (Player) this.sender;
        if (this.config.getFile().contains(ConfigField.TOKEN.get()) && super.bot.getJda() != null) {
            if (super.bot.isReady()) {
                player.sendMessage(super.lang.getMessage("minecraft.chat.configuration.already-configured"));
            } else {
                player.sendMessage(super.lang.getMessage("minecraft.chat.configuration.incomplete-configuration-operator-discord"));
            }
        } else {
            try {
                TextComponent tutorialPage = new TextComponent("§bpage");
                MessageUtil.setHoverEvent(tutorialPage, "§8☀ §bOpen in web browser: §7https://github.com/carlodrift/skoice/wiki");
                tutorialPage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/carlodrift/skoice/wiki"));
                TextComponent tokenCommand = new TextComponent("§bhere");
                MessageUtil.setHoverEvent(tokenCommand, "§8☀ §bShortcut: §7/skoice token");
                tokenCommand.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/skoice token "));
                player.spigot().sendMessage(new ComponentBuilder("\n§dSkoice §8• §7Configuration (§fBot Creation§7)\n\n§8• §7First, you need to create a bot and invite it to your Discord server. Please follow the instructions on this ")
                        .append(tutorialPage)
                        .append("§7.\n§8• §7When you have retrieved its token, put it ").event((HoverEvent) null)
                        .append(tokenCommand)
                        .append("§7.\n§8• §7Once done, type \"§e/configure§7\" on your Discord server to go to the next step.\n").event((HoverEvent) null).create());
            } catch (NoSuchMethodError e) {
                player.sendMessage(super.lang.getMessage("minecraft.chat.configuration.bot-creation-link"));
            }
        }
    }
}
