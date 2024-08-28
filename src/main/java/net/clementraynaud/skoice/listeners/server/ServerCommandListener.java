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

package net.clementraynaud.skoice.listeners.server;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;

public class ServerCommandListener implements Listener {

    private final Skoice plugin;

    public ServerCommandListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        String command = event.getCommand().toLowerCase().trim();
        if (command.startsWith("op ")) {
            String playerName = command.substring(3);
            if (!playerName.isEmpty()) {
                Player player = this.plugin.getServer().getPlayerExact(playerName);
                if (player != null
                        && !player.isOp()
                        && this.plugin.getBot().getStatus() != BotStatus.READY) {
                    this.plugin.getBot().sendIncompleteConfigurationAlert(player, false, true);
                }
            }
        }
    }

}
