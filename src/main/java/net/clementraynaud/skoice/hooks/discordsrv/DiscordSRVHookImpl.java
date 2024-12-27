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

package net.clementraynaud.skoice.hooks.discordsrv;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.AccountLinkedEvent;
import github.scarsz.discordsrv.api.events.AccountUnlinkedEvent;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import net.clementraynaud.skoice.platforms.spigot.SkoiceSpigot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DiscordSRVHookImpl {

    private final SkoiceSpigot plugin;
    private boolean synchronizationComplete = false;

    public DiscordSRVHookImpl(SkoiceSpigot plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        DiscordSRV.api.subscribe(this);
        if (DiscordSRV.getPlugin().getAccountLinkManager() != null) {
            this.synchronizeAccountLinks();
        }
    }

    public void close() {
        try {
            DiscordSRV.api.unsubscribe(this);
        } catch (Throwable ignored) {
        }
    }

    public void linkUserDiscordSRV(String minecraftId, String discordId) {
        this.plugin.getPlugin().getServer().getScheduler().runTaskAsynchronously(this.plugin.getPlugin(), () -> {
            try {
                DiscordSRV.getPlugin().getAccountLinkManager().link(discordId, UUID.fromString(minecraftId));
            } catch (Throwable ignored) {
            }
        });
    }

    public void unlinkUserDiscordSRV(String minecraftId) {
        this.plugin.getPlugin().getServer().getScheduler().runTaskAsynchronously(this.plugin.getPlugin(), () -> {
            try {
                DiscordSRV.getPlugin().getAccountLinkManager().unlink(UUID.fromString(minecraftId));
            } catch (Throwable ignored) {
            }
        });
    }

    @Subscribe
    public void onAccountLinked(AccountLinkedEvent event) {
        if (this.synchronizationComplete && event.getUser() != null && event.getPlayer() != null) {
            SkoiceSpigot.api().linkUser(event.getPlayer().getUniqueId(), event.getUser().getId());
        }
    }

    @Subscribe
    public void onAccountUnlinked(AccountUnlinkedEvent event) {
        if (this.synchronizationComplete && event.getPlayer() != null) {
            SkoiceSpigot.api().unlinkUser(event.getPlayer().getUniqueId());
        }
    }

    @Subscribe
    public void onDiscordReady(DiscordReadyEvent event) {
        try {
            this.synchronizeAccountLinks();
        } catch (Throwable ignored) {
        }
    }

    private void synchronizeAccountLinks() {
        if (this.synchronizationComplete) {
            return;
        }

        Map<String, UUID> existingHookLinks = new HashMap<>(DiscordSRV.getPlugin().getAccountLinkManager().getLinkedAccounts());
        Map<String, String> existingSkoiceLinks = new HashMap<>(SkoiceSpigot.api().getLinkedAccounts());

        existingHookLinks.forEach((discordId, minecraftId) -> {
            if (!existingSkoiceLinks.containsValue(discordId)) {
                SkoiceSpigot.api().linkUser(minecraftId, discordId);
            }
        });

        existingSkoiceLinks.forEach((minecraftId, discordId) -> {
            try {
                UUID uuid = UUID.fromString(minecraftId);
                if (!existingHookLinks.containsValue(uuid)) {
                    this.linkUserDiscordSRV(minecraftId, discordId);
                }
            } catch (IllegalArgumentException ignored) {
            }
        });

        this.synchronizationComplete = true;
    }
}