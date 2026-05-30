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

package net.clementraynaud.skoice.common.system.team;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.model.minecraft.FullPlayer;
import net.clementraynaud.skoice.common.storage.config.ConfigField;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Resolves and exposes the team provider selected in the configuration.
 * <p>
 * If the configured provider's backend is unavailable, team features are
 * disabled ({@link #getTeam(FullPlayer)} returns {@code null} for everyone)
 * rather than falling back to another provider, and a warning is logged.
 */
public class TeamProviderManager {

    private final Skoice plugin;
    private final Map<String, TeamProvider> providers = new LinkedHashMap<>();
    private volatile TeamProvider active;

    public TeamProviderManager(Skoice plugin) {
        this.plugin = plugin;
        this.register(new VanillaTeamProvider());
        this.register(new PartiesTeamProvider());
    }

    private void register(TeamProvider provider) {
        this.providers.put(provider.getId(), provider);
    }

    public void resolve() {
        String configured = this.plugin.getConfigYamlFile().getString(ConfigField.TEAM_PROVIDER.toString());
        TeamProvider selected = configured == null ? null : this.providers.get(configured.toLowerCase());
        if (selected == null) {
            selected = this.providers.get(VanillaTeamProvider.ID);
        }

        if (selected.isAvailable()) {
            this.active = selected;
        } else {
            this.active = null;
            this.plugin.getLogger().warning("The \"" + selected.getId() + "\" team provider is unavailable. "
                    + "Team features are disabled until the corresponding plugin is installed and enabled.");
        }
    }

    public String getTeam(FullPlayer player) {
        TeamProvider provider = this.active;
        if (provider == null || player == null) {
            return null;
        }
        try {
            return provider.getTeam(player);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
