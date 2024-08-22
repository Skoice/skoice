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

package net.clementraynaud.skoice.hooks;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.hooks.discordsrv.DiscordSRVHook;
import net.clementraynaud.skoice.hooks.essentialsx.EssentialsXHook;

public class HookManager {

    private final Skoice plugin;
    private DiscordSRVHook discordSRVHook;
    private EssentialsXHook essentialsXHook;

    public HookManager(Skoice plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        this.discordSRVHook = new DiscordSRVHook(this.plugin);
        this.discordSRVHook.initialize();
        this.essentialsXHook = new EssentialsXHook(this.plugin);
        this.essentialsXHook.initialize();
    }

    public void linkUser(String minecraftId, String discordId) {
        this.discordSRVHook.linkUserDiscordSRV(minecraftId, discordId);
        this.essentialsXHook.linkUserEssentialsX(minecraftId, discordId);
    }

    public void unlinkUser(String minecraftId) {
        this.discordSRVHook.unlinkUserDiscordSRV(minecraftId);
        this.essentialsXHook.unlinkUserEssentialsX(minecraftId);
    }

    public void close() {
        this.discordSRVHook.close();
    }
}
