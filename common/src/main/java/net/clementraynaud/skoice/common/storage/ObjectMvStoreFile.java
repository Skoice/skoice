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

package net.clementraynaud.skoice.common.storage;

import net.clementraynaud.skoice.common.Skoice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ObjectMvStoreFile extends MvStoreFile<Object> {

    protected ObjectMvStoreFile(Skoice plugin, MvStore store, String mapName) {
        super(plugin, store, mapName);
    }

    private static Object coerce(Object value) {
        if (value instanceof List) {
            return new ArrayList<Object>((List<?>) value);
        }
        if (value instanceof Collection) {
            return new ArrayList<Object>((Collection<?>) value);
        }
        return value;
    }

    public String getString(String path) {
        Object value = this.getMap().get(path);
        return value == null ? null : value.toString();
    }

    public boolean getBoolean(String path) {
        Object value = this.getMap().get(path);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return value != null && Boolean.parseBoolean(value.toString());
    }

    public int getInt(String path) {
        Object value = this.getMap().get(path);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public List<String> getStringList(String path) {
        Object value = this.getMap().get(path);
        if (!(value instanceof Collection)) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (Object element : (Collection<?>) value) {
            if (element != null) {
                result.add(element.toString());
            }
        }
        return result;
    }

    @Override
    public void set(String path, Object value) {
        super.set(path, ObjectMvStoreFile.coerce(value));
    }

    @Override
    public void setDefault(String path, Object value) {
        super.setDefault(path, ObjectMvStoreFile.coerce(value));
    }
}
