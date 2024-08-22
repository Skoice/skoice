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

package net.clementraynaud.skoice.hooks.essentialsx;

import net.clementraynaud.skoice.Skoice;
import net.essentialsx.api.v2.events.discordlink.DiscordLinkStatusChangeEvent;
import net.essentialsx.api.v2.services.discord.DiscordService;
import net.essentialsx.api.v2.services.discordlink.DiscordLinkService;
import net.essentialsx.discordlink.EssentialsDiscordLink;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EssentialsXHookImpl implements Listener {

    private final Skoice plugin;
    private DiscordLinkService essentialsLinkApi;
    private DiscordService essentialsDiscordApi;
    private Map<String, String> essentialsLinkedAccounts;
    private boolean synchronizationComplete = false;

    public EssentialsXHookImpl(Skoice plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void initialize() {
        this.essentialsLinkApi = this.plugin.getServer().getServicesManager().load(DiscordLinkService.class);
        this.essentialsDiscordApi = this.plugin.getServer().getServicesManager().load(DiscordService.class);
        EssentialsDiscordLink ess = (EssentialsDiscordLink) this.plugin.getServer().getPluginManager().getPlugin("EssentialsDiscordLink");
        if (ess != null && ess.getAccountStorage() != null) {
            this.essentialsLinkedAccounts = Collections.unmodifiableMap(ess.getAccountStorage().getRawStorageMap());
            this.synchronizeAccountLinks();
        }
    }

    public void linkUserEssentialsX(String minecraftId, String discordId) {
        try {
            this.essentialsDiscordApi.getMemberById(discordId).thenAccept(member -> this.essentialsLinkApi.linkAccount(UUID.fromString(minecraftId), member));
        } catch (Throwable ignored) {
        }
    }

    public void unlinkUserEssentialsX(String minecraftId) {
        try {
            this.essentialsLinkApi.unlinkAccount(UUID.fromString(minecraftId));
        } catch (Throwable ignored) {
        }
    }

    @EventHandler
    public void onDiscordLinkStatusChange(DiscordLinkStatusChangeEvent event) {
        if (!this.synchronizationComplete) {
            return;
        }
        if (event.isLinked()) {
            if (event.getMemberId() != null && event.getUser() != null && event.getUser().getUUID() != null) {
                Skoice.api().linkUser(event.getUser().getUUID(), event.getMemberId());
            }
        } else {
            if (event.getUser() != null && event.getUser().getUUID() != null) {
                Skoice.api().unlinkUser(event.getUser().getUUID());
            }
        }
    }

    private void synchronizeAccountLinks() {
        if (this.synchronizationComplete) {
            return;
        }

        Map<String, String> existingHookLinks = this.essentialsLinkedAccounts;
        Map<String, String> existingSkoiceLinks = new HashMap<>(Skoice.api().getLinkedAccounts());

        existingHookLinks.forEach((minecraftId, discordId) -> {
            if (!existingSkoiceLinks.containsValue(discordId)) {
                try {
                    Skoice.api().linkUser(UUID.fromString(minecraftId), discordId);
                } catch (IllegalArgumentException ignored) {
                }
            }
        });

        existingSkoiceLinks.forEach((minecraftId, discordId) -> {
            if (!existingHookLinks.containsKey(minecraftId)) {
                this.linkUserEssentialsX(minecraftId, discordId);
            }
        });

        this.synchronizationComplete = true;
    }
}
