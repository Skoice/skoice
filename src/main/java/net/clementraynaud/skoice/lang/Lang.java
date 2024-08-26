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

package net.clementraynaud.skoice.lang;

import net.clementraynaud.skoice.util.ConfigurationUtil;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class Lang {

    protected YamlConfiguration english;
    protected YamlConfiguration active;

    public void load(LangInfo langInfo) {
        if (this.english == null) {
            this.english = ConfigurationUtil.loadResource(this.getClass().getName(), this.getPath(LangInfo.EN));
        }
        this.active = new YamlConfiguration();
        if (langInfo != LangInfo.EN) {
            this.active = ConfigurationUtil.loadResource(this.getClass().getName(), this.getPath(langInfo));
        }
    }

    protected abstract String getPath(LangInfo langInfo);

    public String getMessage(String path) {
        return (this.active != null && this.active.contains(path))
                ? this.active.getString(path)
                : this.english.getString(path);
    }

    public String getMessage(String path, String... args) {
        String message = this.active.contains(path)
                ? this.active.getString(path)
                : this.english.getString(path);
        if (message == null) {
            return null;
        }
        return String.format(message, (Object[]) args);
    }

    public boolean contains(String path) {
        return this.english.contains(path);
    }

    public int getAmountOfArgsRequired(String message) {
        int amount = message.split("%s").length - 1;
        if (message.startsWith("%s") || message.endsWith("%s")) {
            amount++;
        }
        return amount;
    }
}
