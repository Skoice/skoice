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

package net.clementraynaud.skoice.spigot;

import net.clementraynaud.skoice.common.bot.Bot;
import net.clementraynaud.skoice.spigot.api.events.player.PlayerProximityConnectEvent;
import net.clementraynaud.skoice.spigot.api.events.system.SystemReadyEvent;
import net.clementraynaud.skoice.spigot.tasks.SpigotInterruptSystemTask;

public class SpigotBot extends Bot {

    private final SkoiceSpigot plugin;

    public SpigotBot(SkoiceSpigot plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected void callPlayerProximityConnectEvent(String minecraftId, String memberId) {
        this.plugin.getScheduler().runTask(() -> {
            PlayerProximityConnectEvent event = new PlayerProximityConnectEvent(minecraftId, memberId);
            this.plugin.getPlugin().getServer().getPluginManager().callEvent(event);
        });
    }

    @Override
    protected void callSystemReadyEvent() {
        this.plugin.getScheduler().runTask(() -> {
            SystemReadyEvent event = new SystemReadyEvent();
            this.plugin.getPlugin().getServer().getPluginManager().callEvent(event);
        });
    }


    @Override
    public void runInterruptSystemTask() {
        new SpigotInterruptSystemTask(this.plugin).run();
    }
}
