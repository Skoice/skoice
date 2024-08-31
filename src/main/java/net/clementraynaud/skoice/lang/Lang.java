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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Lang {

    protected Map<String, List<String>> english;
    protected Map<String, List<String>> active;

    public void load(LangInfo langInfo) {
        this.active = null;
        if (this.english == null) {
            YamlConfiguration english = ConfigurationUtil.loadResource(this.getClass().getName(), this.getPath(LangInfo.EN));
            if (english != null) {
                this.english = this.convertYamlToMap(english);
            } else {
                this.english = new HashMap<>();
            }
        }
        if (langInfo != LangInfo.EN) {
            YamlConfiguration active = ConfigurationUtil.loadResource(this.getClass().getName(), this.getPath(langInfo));
            if (active != null) {
                this.active = this.convertYamlToMap(active);
            }
        }
    }

    private Map<String, List<String>> convertYamlToMap(YamlConfiguration yamlConfig) {
        Map<String, List<String>> result = new HashMap<>();
        for (String key : yamlConfig.getKeys(true)) {
            Object value = yamlConfig.get(key);
            List<String> valuesList = new ArrayList<>();
            if (value instanceof List) {
                for (Object obj : (List<?>) value) {
                    if (obj instanceof String) {
                        valuesList.add((String) obj);
                    }
                }
            } else if (value instanceof String) {
                valuesList.add((String) value);
            }
            if (!valuesList.isEmpty()) {
                result.put(key, valuesList);
            }
        }
        return result;
    }

    protected abstract String getPath(LangInfo langInfo);

    public String getMessage(String path) {
        String message;
        if (this.active != null && this.active.containsKey(path)) {
            message = this.active.get(path).get(0);
        } else {
            message = (this.english.get(path) != null) ? this.english.get(path).get(0) : null;
        }
        if (message == null || message.trim().isEmpty()) {
            return String.format("!%s!", path);
        }
        return message;
    }

    public String getMessage(String path, String... args) {
        return String.format(this.getMessage(path), (Object[]) args);
    }

    public boolean contains(String path) {
        return this.english.containsKey(path);
    }

    public int getAmountOfArgsRequired(String message) {
        int amount = message.split("%s").length - 1;
        if (message.startsWith("%s") || message.endsWith("%s")) {
            amount++;
        }
        return amount;
    }
}
