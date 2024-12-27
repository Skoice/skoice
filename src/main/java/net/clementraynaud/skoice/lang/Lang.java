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
import net.clementraynaud.skoice.util.MapUtil;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

public abstract class Lang {

    protected final LangFormatter formatter = new LangFormatter();
    protected Map<String, String> english;
    protected Map<String, String> active;

    public void load(LangInfo langInfo) {
        this.active = null;
        if (this.english == null) {
            this.loadFormatter();
            YamlConfiguration english = ConfigurationUtil.loadResource(this.getClass().getName(),
                    this.getPath(LangInfo.EN));
            if (english != null) {
                this.english = ConfigurationUtil.convertYamlToStringMap(english);
            } else {
                this.english = new HashMap<>();
            }
        }

        if (langInfo != LangInfo.EN) {
            YamlConfiguration active = ConfigurationUtil.loadResource(this.getClass().getName(),
                    this.getPath(langInfo));
            if (active != null) {
                this.active = ConfigurationUtil.convertYamlToStringMap(active);
            }
        }
    }

    protected abstract String getPath(LangInfo langInfo);

    protected abstract void loadFormatter();

    protected String getRawMessage(String path) {
        if (this.active != null && this.active.containsKey(path)) {
            return this.active.get(path);
        } else {
            return this.english.getOrDefault(path, "");
        }
    }

    public String getMessage(String path, Map<String, String> args) {
        return this.formatter.format(this.getRawMessage(path), args);
    }

    public String getMessage(String path) {
        return this.getMessage(path, MapUtil.of());
    }

    public boolean contains(String path) {
        return this.english.containsKey(path);
    }

    public LangFormatter getFormatter() {
        return this.formatter;
    }
}
