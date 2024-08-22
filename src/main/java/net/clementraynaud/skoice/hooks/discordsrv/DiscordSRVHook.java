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
import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.storage.config.ConfigField;

public class DiscordSRVHook {

    private final Skoice plugin;
    private DiscordSRVHookImpl discordSRVHookImpl;

    public DiscordSRVHook(Skoice plugin) {
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
