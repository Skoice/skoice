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

package net.clementraynaud.skoice.common.storage.config;

import java.util.Collection;
import java.util.List;

public final class ConfigScope {

    private final ConfigStore store;
    private final String overrideId;

    ConfigScope(ConfigStore store, String overrideId) {
        this.store = store;
        this.overrideId = overrideId;
    }

    public boolean isGlobal() {
        return this.overrideId == null;
    }

    public String getOverrideId() {
        return this.overrideId;
    }

    public String getPath(String field) {
        return this.overrideId == null
                ? field
                : WorldOverrides.getFieldPath(this.overrideId, field);
    }

    public boolean isOverridden(String field) {
        return this.overrideId != null && this.store.contains(this.getPath(field));
    }

    public boolean isAnyOverridden(Collection<String> fields) {
        return fields.stream().anyMatch(this::isOverridden);
    }

    public String getString(String field) {
        return this.store.getString(this.getEffectivePath(field));
    }

    public boolean getBoolean(String field) {
        return this.store.getBoolean(this.getEffectivePath(field));
    }

    public int getInt(String field) {
        return this.store.getInt(this.getEffectivePath(field));
    }

    public List<String> getStringList(String field) {
        return this.store.getStringList(this.getEffectivePath(field));
    }

    public void set(String field, Object value) {
        this.store.set(this.getPath(field), value);
    }

    public void reset(String field) {
        if (this.overrideId != null) {
            this.store.remove(this.getPath(field));
        }
    }

    public void reset(Collection<String> fields) {
        fields.forEach(this::reset);
    }

    private String getEffectivePath(String field) {
        return this.isOverridden(field) ? this.getPath(field) : field;
    }
}
