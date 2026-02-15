/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025, 2026 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

public class SpigotLinksYamlFile extends LinksYamlFile {

    private final SkoiceSpigot spigotPlugin;

    public SpigotLinksYamlFile(SkoiceSpigot spigotPlugin) {
        super(spigotPlugin);
        this.spigotPlugin = spigotPlugin;
    }

    @Override
    protected void additionalLinkProcessing(String minecraftId, String discordId) {
        this.spigotPlugin.getHookManager().linkUser(minecraftId, discordId);
    }

    @Override
    protected void additionalUnlinkProcessing(String minecraftId) {
        this.spigotPlugin.getHookManager().unlinkUser(minecraftId);
    }
}
