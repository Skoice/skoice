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

package net.clementraynaud.skoice.spigot.jda;

import net.clementraynaud.skoice.common.listeners.guild.voice.GuildVoiceUpdateListener;
import net.clementraynaud.skoice.spigot.SkoiceSpigot;
import net.clementraynaud.skoice.spigot.api.events.player.PlayerProximityDisconnectEvent;

public class SpigotGuildVoiceUpdateListener extends GuildVoiceUpdateListener {

    private final SkoiceSpigot plugin;

    public SpigotGuildVoiceUpdateListener(SkoiceSpigot plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected void callPlayerProximityDisconnectEvent(String minecraftId) {
        this.plugin.getScheduler().runTask(() -> {
            PlayerProximityDisconnectEvent event = new PlayerProximityDisconnectEvent(minecraftId);
            this.plugin.getPlugin().getServer().getPluginManager().callEvent(event);
        });
    }
}
