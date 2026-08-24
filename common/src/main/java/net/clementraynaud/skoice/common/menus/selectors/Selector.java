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

package net.clementraynaud.skoice.common.menus.selectors;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.storage.config.ConfigScope;
import net.clementraynaud.skoice.common.storage.config.WorldOverrides;
import net.dv8tion.jda.api.components.selections.SelectMenu;

public abstract class Selector {

    protected final Skoice plugin;
    private final String overrideId;

    protected Selector(Skoice plugin) {
        this(plugin, null);
    }

    protected Selector(Skoice plugin, String overrideId) {
        this.plugin = plugin;
        this.overrideId = overrideId;
    }

    protected ConfigScope getScope() {
        return this.plugin.getConfigYamlFile().scope(this.overrideId);
    }

    protected String getOverrideId() {
        return this.overrideId;
    }

    protected String scopeId(String componentId) {
        return WorldOverrides.scopeId(componentId, this.overrideId);
    }

    public abstract SelectMenu get();
}
