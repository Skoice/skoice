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

package net.clementraynaud.skoice.spigot.hooks.discordsrv;

import github.scarsz.discordsrv.DiscordSRV;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.clementraynaud.skoice.spigot.SkoiceSpigot;

public class DiscordSRVHook {

    private final SkoiceSpigot plugin;
    private DiscordSRVHookImpl discordSRVHookImpl;

    public DiscordSRVHook(SkoiceSpigot plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (!this.plugin.getConfigYamlFile().getBoolean(ConfigField.DISCORDSRV_SYNCHRONIZATION.toString())) {
            return;
        }
        if (this.discordSRVHookImpl != null) {
            return;
        }
        try {
            DiscordSRV.api.hashCode();
            DiscordSRVHookImpl discordSRVHookImpl = new DiscordSRVHookImpl(this.plugin);
            discordSRVHookImpl.initialize();
            this.discordSRVHookImpl = discordSRVHookImpl;
        } catch (Throwable ignored) {
        }
    }

    public void linkUserDiscordSRV(String minecraftId, String discordId) {
        if (this.discordSRVHookImpl != null) {
            this.discordSRVHookImpl.linkUserDiscordSRV(minecraftId, discordId);
        }
    }

    public void unlinkUserDiscordSRV(String minecraftId) {
        if (this.discordSRVHookImpl != null) {
            this.discordSRVHookImpl.unlinkUserDiscordSRV(minecraftId);
        }
    }

    public void close() {
        if (this.discordSRVHookImpl != null) {
            this.discordSRVHookImpl.close();
        }
    }
}