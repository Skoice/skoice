/*
 * Copyright 2020, 2021 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
 * Copyright 2016, 2017, 2018, 2019, 2020, 2021 Austin "Scarsz" Shapiro
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

package net.clementraynaud.skoice.configuration.minecraft;

import net.clementraynaud.skoice.bot.Connection;
import net.clementraynaud.skoice.util.Lang;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static net.clementraynaud.skoice.Skoice.getPlugin;

public class IncorrectConfigurationAlert implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) {
            if (!getPlugin().isTokenSet() || Connection.getJda() == null) {
                try {
                    TextComponent configureCommand = new TextComponent("§bhere");
                    configureCommand.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§8☀ §bExecute: §7/configure")));
                    configureCommand.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/configure"));
                    player.spigot().sendMessage(new ComponentBuilder("§dSkoice §8• §7Skoice is §cnot configured correctly§7. Click ")
                            .append(configureCommand)
                            .append(" §7to set it up.").event((HoverEvent) null).create());
                } catch (NoSuchMethodError e) {
                    player.sendMessage(Lang.Minecraft.INCOMPLETE_CONFIGURATION_OPERATOR_COMMAND.print());
                }
            } else if (!getPlugin().isBotReady()) {
                player.sendMessage(Lang.Minecraft.INCOMPLETE_CONFIGURATION_OPERATOR_DISCORD.print());
            }
        }
    }
}
