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

package net.clementraynaud.skoice.spigot.system;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.handlers.player.PlayerJoinHandler;
import net.clementraynaud.skoice.common.listeners.guild.voice.GuildVoiceUpdateListener;
import net.clementraynaud.skoice.common.system.ListenerManager;
import net.clementraynaud.skoice.spigot.SkoiceSpigot;
import net.clementraynaud.skoice.spigot.handlers.SpigotPlayerJoinHandler;
import net.clementraynaud.skoice.spigot.jda.SpigotGuildVoiceUpdateListener;
import net.clementraynaud.skoice.spigot.listeners.PlayerJoinListener;
import net.clementraynaud.skoice.spigot.listeners.PlayerQuitListener;
import net.clementraynaud.skoice.spigot.listeners.ServerCommandListener;
import org.bukkit.event.HandlerList;

public class SpigotListenerManager extends ListenerManager {

    private final PlayerQuitListener playerQuitListener;
    private final SkoiceSpigot plugin;

    public SpigotListenerManager(SkoiceSpigot plugin) {
        super(plugin);
        this.plugin = plugin;
        this.playerQuitListener = new PlayerQuitListener(this);
    }

    @Override
    public void registerPermanentMinecraftListeners() {
        this.plugin.getPlugin().getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this.plugin.getPlugin());
        this.plugin.getPlugin().getServer().getPluginManager().registerEvents(new ServerCommandListener(this), this.plugin.getPlugin());
    }

    @Override
    public void registerMinecraftListeners() {
        this.plugin.getPlugin().getServer().getPluginManager().registerEvents(this.playerQuitListener, this.plugin.getPlugin());
    }

    @Override
    public void unregisterMinecraftListeners() {
        HandlerList.unregisterAll(this.playerQuitListener);
    }

    @Override
    protected GuildVoiceUpdateListener createGuildVoiceUpdate(Skoice plugin) {
        return new SpigotGuildVoiceUpdateListener((SkoiceSpigot) plugin);
    }

    @Override
    protected PlayerJoinHandler createPlayerJoinHandler(Skoice skoice) {
        return new SpigotPlayerJoinHandler((SkoiceSpigot) skoice);
    }

    public SkoiceSpigot getPlugin() {
        return this.plugin;
    }
}
