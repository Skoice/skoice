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

package net.clementraynaud.skoice.common.handlers.player;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.api.events.player.PlayerProximityDisconnectEvent;
import net.clementraynaud.skoice.common.model.minecraft.BasePlayer;
import net.clementraynaud.skoice.common.system.LinkedPlayer;
import net.clementraynaud.skoice.common.system.Networks;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class PlayerQuitHandler {

    public CompletionStage<Void> onPlayerQuit(BasePlayer player) {
        if (Skoice.api().isLinked(player.getUniqueId()) && Skoice.api().isProximityConnected(player.getUniqueId())) {
            Skoice.eventBus().fireAsync(new PlayerProximityDisconnectEvent(player.getUniqueId().toString()));
        }

        return CompletableFuture.runAsync(() -> {
            LinkedPlayer.getOnlineLinkedPlayers().removeIf(p -> p.getFullPlayer().equals(player));
            Networks.getAll().stream()
                    .filter(network -> network.contains(player))
                    .forEach(network -> network.remove(player));
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }
}
