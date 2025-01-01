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

package net.clementraynaud.skoice.spigot.storage;


import net.clementraynaud.skoice.common.storage.LinksYamlFile;
import net.clementraynaud.skoice.spigot.SkoiceSpigot;
import net.clementraynaud.skoice.spigot.api.events.account.AccountLinkEvent;
import net.clementraynaud.skoice.spigot.api.events.account.AccountUnlinkEvent;
import net.clementraynaud.skoice.spigot.api.events.player.PlayerProximityConnectEvent;
import net.clementraynaud.skoice.spigot.api.events.player.PlayerProximityDisconnectEvent;

import java.util.UUID;

public class SpigotLinksYamlFile extends LinksYamlFile {

    private final SkoiceSpigot plugin;

    public SpigotLinksYamlFile(SkoiceSpigot plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    protected void additionalLinkProcessing(String minecraftId, String discordId) {
        this.plugin.getHookManager().linkUser(minecraftId, discordId);
        this.plugin.getScheduler().runTask(() -> {
            AccountLinkEvent event = new AccountLinkEvent(minecraftId, discordId);
            this.plugin.getPlugin().getServer().getPluginManager().callEvent(event);
        });
    }

    @Override
    protected void additionalUnlinkProcessing(String minecraftId) {
        this.plugin.getHookManager().unlinkUser(minecraftId);
        this.plugin.getScheduler().runTask(() -> {
            AccountUnlinkEvent event = new AccountUnlinkEvent(minecraftId);
            this.plugin.getPlugin().getServer().getPluginManager().callEvent(event);
        });
    }

    @Override
    protected void callPlayerProximityConnectEvent(String minecraftId, String memberId) {
        this.plugin.getScheduler().runTask(() -> {
            PlayerProximityConnectEvent event = new PlayerProximityConnectEvent(minecraftId, memberId);
            this.plugin.getPlugin().getServer().getPluginManager().callEvent(event);
        });
    }

    @Override
    protected void callPlayerProximityDisconnectEventIfConnected(String minecraftId) {
        if (SkoiceSpigot.api().isProximityConnected(UUID.fromString(minecraftId))) {
            this.plugin.getScheduler().runTask(() -> {
                PlayerProximityDisconnectEvent event = new PlayerProximityDisconnectEvent(minecraftId);
                this.plugin.getPlugin().getServer().getPluginManager().callEvent(event);
            });
        }
    }
}
