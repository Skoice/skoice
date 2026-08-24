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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class WorldOverride {

    private final String id;
    private final String name;
    private final List<String> worlds;
    private final List<String> patterns;
    private final List<Pattern> compiledPatterns;
    private final ConfigScope scope;

    WorldOverride(ConfigStore store, String id, String name, List<String> worlds, List<String> patterns) {
        this.id = id;
        this.name = name;
        this.worlds = Collections.unmodifiableList(new ArrayList<>(worlds));
        this.patterns = Collections.unmodifiableList(new ArrayList<>(patterns));
        this.compiledPatterns = WorldOverride.compile(patterns);
        this.scope = new ConfigScope(store, id);
    }

    private static List<Pattern> compile(List<String> patterns) {
        List<Pattern> compiledPatterns = new ArrayList<>();
        for (String pattern : patterns) {
            try {
                compiledPatterns.add(Pattern.compile(pattern));
            } catch (PatternSyntaxException ignored) {
            }
        }
        return Collections.unmodifiableList(compiledPatterns);
    }

    public static boolean isValidPattern(String pattern) {
        try {
            Pattern.compile(pattern);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    public boolean matches(String world) {
        if (world == null) {
            return false;
        }
        if (this.worlds.contains(world) || this.patterns.contains(world)) {
            return true;
        }
        for (Pattern pattern : this.compiledPatterns) {
            if (pattern.matcher(world).matches()) {
                return true;
            }
        }
        return false;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getWorlds() {
        return this.worlds;
    }

    public List<String> getPatterns() {
        return this.patterns;
    }

    public boolean isEmpty() {
        return this.worlds.isEmpty() && this.patterns.isEmpty();
    }

    public ConfigScope getScope() {
        return this.scope;
    }
}
