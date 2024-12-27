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

package net.clementraynaud.skoice.common.util;


import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConfigurationUtil {

    private ConfigurationUtil() {
    }

    public static YamlConfiguration loadResource(String className, String path) {
        InputStream inputStream;
        try {
            inputStream = Class.forName(className).getClassLoader().getResourceAsStream(path);
        } catch (ClassNotFoundException e) {
            return null;
        }
        if (inputStream == null) {
            return null;
        }
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        try {
            return YamlConfiguration.loadConfiguration(inputStreamReader);
        } catch (IOException e) {
            return null;
        }
    }

    public static Map<String, List<String>> convertLangYamlToMap(YamlConfiguration yamlConfig) {
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
}
