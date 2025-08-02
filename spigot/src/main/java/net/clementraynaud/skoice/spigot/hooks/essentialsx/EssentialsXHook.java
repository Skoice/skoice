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

package net.clementraynaud.skoice.spigot.hooks.essentialsx;

import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.clementraynaud.skoice.spigot.SkoiceSpigot;
import net.essentialsx.api.v2.services.discord.DiscordService;
import net.essentialsx.api.v2.services.discordlink.DiscordLinkService;

public class EssentialsXHook {

    private final SkoiceSpigot plugin;
    private EssentialsXHookImpl essentialsXHookImpl;

    public EssentialsXHook(SkoiceSpigot plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (!this.plugin.getConfigYamlFile().getBoolean(ConfigField.ESSENTIALSX_SYNCHRONIZATION.toString())) {
            return;
        }
        if (this.essentialsXHookImpl != null) {
            return;
        }
        try {
            this.plugin.getPlugin().getServer().getServicesManager().load(DiscordLinkService.class).hashCode();
            this.plugin.getPlugin().getServer().getServicesManager().load(DiscordService.class).hashCode();
            EssentialsXHookImpl newEssentialsXHookImpl = new EssentialsXHookImpl(this.plugin);
            newEssentialsXHookImpl.initialize();
            this.essentialsXHookImpl = newEssentialsXHookImpl;
        } catch (Throwable ignored) {
        }
    }

    public void linkUserEssentialsX(String minecraftId, String discordId) {
        if (this.essentialsXHookImpl != null) {
            this.essentialsXHookImpl.linkUserEssentialsX(minecraftId, discordId);
        }
    }

    public void unlinkUserEssentialsX(String minecraftId) {
        if (this.essentialsXHookImpl != null) {
            this.essentialsXHookImpl.unlinkUserEssentialsX(minecraftId);
        }
    }
}