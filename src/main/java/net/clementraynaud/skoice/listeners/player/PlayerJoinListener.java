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

package net.clementraynaud.skoice.listeners.player;

import net.clementraynaud.skoice.lang.MinecraftLang;
import net.clementraynaud.skoice.util.MessageUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.bot.Bot.getJda;

public class PlayerJoinListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) {
            if (!getPlugin().isTokenSet() || getJda() == null) {
                try {
                    TextComponent configureCommand = new TextComponent("§bhere");
                    MessageUtil.setHoverEvent(configureCommand, "§8☀ §bExecute: §7/skoice configure");
                    configureCommand.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skoice configure"));
                    player.spigot().sendMessage(new ComponentBuilder("§dSkoice §8• §7Skoice is §cnot configured correctly§7. Click ")
                            .append(configureCommand)
                            .append(" §7to set it up.").event((HoverEvent) null).create());
                } catch (NoSuchMethodError e) {
                    player.sendMessage(MinecraftLang.INCOMPLETE_CONFIGURATION_OPERATOR_COMMAND.toString());
                }
            } else if (!getPlugin().isBotReady()) {
                player.sendMessage(MinecraftLang.INCOMPLETE_CONFIGURATION_OPERATOR_DISCORD.toString());
            }
        }
    }
}
