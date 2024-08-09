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

package net.clementraynaud.skoice.api;

import net.clementraynaud.skoice.Skoice;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class SkoiceAPI {

    private final Skoice plugin;

    public SkoiceAPI(Skoice plugin) {
        this.plugin = plugin;
    }

    public Map<String, String> getLinkedAccounts() {
        return Collections.unmodifiableMap(this.plugin.getLinksYamlFile().getLinks());
    }

    public boolean linkUser(UUID minecraftId, String discordId) {
        if (this.plugin.getLinksYamlFile().getLinks().containsKey(minecraftId.toString())) {
            return false;
        }
        this.plugin.getLinksYamlFile().linkUserDirectly(minecraftId.toString(), discordId);
        return true;
    }

    public boolean unlinkUser(UUID minecraftId) {
        if (this.plugin.getLinksYamlFile().getLinks().get(minecraftId.toString()) == null) {
            return false;
        }
        this.plugin.getLinksYamlFile().unlinkUserDirectly(minecraftId.toString());
        return true;
    }
}
