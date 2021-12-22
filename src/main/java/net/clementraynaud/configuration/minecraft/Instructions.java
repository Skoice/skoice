// Copyright 2020, 2021 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
// Copyright 2016, 2017, 2018, 2019, 2020, 2021 Austin "Scarsz" Shapiro

// This file is part of Skoice.

// Skoice is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Skoice is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Skoice.  If not, see <https://www.gnu.org/licenses/>.


package net.clementraynaud.configuration.minecraft;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static net.clementraynaud.Bot.getJda;
import static net.clementraynaud.Skoice.getPlugin;

public class Instructions implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§dSkoice §8• §7This command is §conly executable §7by players.");
            return true;
        }
        Player player = (Player) sender;
        if (player.isOp()) {
            if (getPlugin().isTokenSet() && getJda() != null) {
                if (getPlugin().isBotConfigured()) {
                    player.sendMessage("§dSkoice §8• §7You have §calready configured Skoice§7. Type \"§e/configure§7\" on your Discord server to update the settings.");
                } else {
                    player.sendMessage("§dSkoice §8• §7Your bot is §cnot configured correctly§7. Type \"§e/configure§7\" on your Discord server to set it up.");
                }
            } else {
                try {
                    TextComponent tutorialPage = new TextComponent("§bpage");
                    tutorialPage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§8☀ §bOpen in web browser: §7https://github.com/carlodrift/skoice/wiki/Tutorial").create()));
                    tutorialPage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/carlodrift/skoice/wiki/Tutorial"));
                    TextComponent tokenCommand = new TextComponent("§bhere");
                    tokenCommand.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§8☀ §bShortcut: §7/token").create()));
                    tokenCommand.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/token "));
                    player.spigot().sendMessage(new ComponentBuilder("\n§dSkoice §8• §7Configuration (§fBot Creation§7)\n\n§8• §7First, you need to create a bot and invite it to your Discord server. Please follow the instructions on this ")
                            .append(tutorialPage)
                            .append("§7.\n§8• §7When you have retrieved its token, put it ").event((HoverEvent) null)
                            .append(tokenCommand)
                            .append("§7.\n§8• §7Once done, type \"§e/configure§7\" on your Discord server to go to the next step.\n").event((HoverEvent) null).create());
                } catch (NoSuchMethodError e) {
                    player.sendMessage(" \n§dSkoice §8• §7Configuration (§fBot Creation§7)\n \n§8• §7First, you need to create a bot and invite it to your Discord server. Please follow the instructions on this page: §bhttps://github.com/carlodrift/skoice/wiki/Tutorial§7.\n§8• §7When you have retrieved its token, type \"§e/token§7\" followed by the token.\n§8• §7Once done, type \"§e/configure§7\" on your Discord server to go to the next step.\n ");
                }
            }
        }
        return true;
    }
}