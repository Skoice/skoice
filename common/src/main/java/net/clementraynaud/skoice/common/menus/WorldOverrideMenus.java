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

package net.clementraynaud.skoice.common.menus;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.storage.config.WorldOverride;
import net.clementraynaud.skoice.common.storage.config.WorldOverrides;
import net.dv8tion.jda.api.components.label.Label;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.modals.Modal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class WorldOverrideMenus {

    private WorldOverrideMenus() {
    }

    public static Map<String, String> enrich(Skoice plugin, Map<String, String> args) {
        String overrideId = args.get(WorldOverrides.ARG);
        if (overrideId == null) {
            return args;
        }

        WorldOverrides overrides = plugin.getConfigYamlFile().getWorldOverrides();
        WorldOverride override = overrides.get(overrideId);

        Map<String, String> enriched = new HashMap<>(args);
        enriched.put("priority", String.valueOf(overrides.getIndex(overrideId) + 1));
        enriched.put("total", String.valueOf(overrides.getAll().size()));
        if (override != null) {
            enriched.put("name", override.getName());
        }
        return enriched;
    }

    public static String getList(Skoice plugin) {
        WorldOverrides overrides = plugin.getConfigYamlFile().getWorldOverrides();
        if (overrides.isEmpty()) {
            return "-# " + plugin.getBot().getLang().getMessage("menu.world-overrides.empty");
        }

        List<WorldOverride> all = overrides.getAll();
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < all.size(); i++) {
            WorldOverride override = all.get(i);
            if (i > 0) {
                list.append('\n');
            }
            Map<String, String> args = new HashMap<>();
            args.put("priority", String.valueOf(i + 1));
            args.put("name", override.getName());
            list.append(plugin.getBot().getLang().getMessage("menu.world-overrides.entry", args));
            for (String line : WorldOverrideMenus.getCoverage(plugin, override)) {
                list.append("\n> ").append(line);
            }
        }
        return list.toString();
    }

    private static List<String> getCoverage(Skoice plugin, WorldOverride override) {
        List<String> lines = new ArrayList<>();

        if (!override.getWorlds().isEmpty()) {
            Map<String, String> args = new HashMap<>();
            args.put("worlds", WorldOverrideMenus.format(override.getWorlds()));
            lines.add(plugin.getBot().getLang().getMessage("menu.world-overrides.entry-worlds", args));
        }
        if (!override.getPatterns().isEmpty()) {
            Map<String, String> args = new HashMap<>();
            args.put("patterns", WorldOverrideMenus.format(override.getPatterns()));
            lines.add(plugin.getBot().getLang().getMessage("menu.world-overrides.entry-patterns", args));
        }
        if (lines.isEmpty()) {
            lines.add(plugin.getBot().getLang().getMessage("menu.world-overrides.entry-empty"));
        }
        return lines;
    }

    public static String getStatusNote(Skoice plugin, String overrideId) {
        WorldOverride override = plugin.getConfigYamlFile().getWorldOverrides().get(overrideId);
        return override != null && override.isEmpty()
                ? plugin.getBot().getLang().getMessage("menu.world-override.empty")
                : null;
    }

    public static String getPatternNote(Skoice plugin, String overrideId) {
        WorldOverride override = plugin.getConfigYamlFile().getWorldOverrides().get(overrideId);
        if (override == null || override.getPatterns().isEmpty()) {
            return plugin.getBot().getLang().getMessage("menu.world-override-worlds.no-pattern");
        }
        Map<String, String> args = new HashMap<>();
        args.put("patterns", WorldOverrideMenus.format(override.getPatterns()));
        return "-# " + plugin.getBot().getLang().getMessage("menu.world-override-worlds.patterns", args);
    }

    public static Modal getNameModal(Skoice plugin, WorldOverride override) {
        TextInput name = TextInput.create(WorldOverrides.NAME_INPUT_ID, TextInputStyle.SHORT)
                .setValue(override != null
                        ? override.getName()
                        : plugin.getBot().getLang().getMessage("text-input.override-name.default-value"))
                .setRequiredRange(1, 25)
                .build();

        return Modal.create(override == null
                                ? WorldOverrides.CREATE_BUTTON_ID
                                : WorldOverrides.scopeId(WorldOverrides.EDIT_BUTTON_ID, override.getId()),
                        plugin.getBot().getLang().getMessage(override == null
                                ? "menu.world-overrides.create-modal.title"
                                : "menu.world-overrides.rename-modal.title"))
                .addComponents(Label.of(plugin.getBot().getLang().getMessage("text-input.override-name.label"), name))
                .build();
    }

    public static Modal getPatternsModal(Skoice plugin, WorldOverride override) {
        TextInput patterns = TextInput.create(WorldOverrides.PATTERNS_INPUT_ID, TextInputStyle.PARAGRAPH)
                .setValue(override.getPatterns().isEmpty() ? null : String.join("\n", override.getPatterns()))
                .setPlaceholder(plugin.getBot().getLang().getMessage("text-input.override-patterns.placeholder"))
                .setRequired(false)
                .setRequiredRange(0, TextInput.MAX_VALUE_LENGTH)
                .build();

        return Modal.create(WorldOverrides.scopeId(WorldOverrides.PATTERNS_BUTTON_ID, override.getId()),
                        plugin.getBot().getLang().getMessage("menu.world-override-worlds.patterns-modal.title"))
                .addComponents(Label.of(plugin.getBot().getLang().getMessage("text-input.override-patterns.label"),
                        plugin.getBot().getLang().getMessage("text-input.override-patterns.description"),
                        patterns))
                .build();
    }

    public static List<String> readPatterns(String value) {
        List<String> patterns = new ArrayList<>();
        for (String line : value.split("\\R")) {
            String pattern = line.trim();
            if (!pattern.isEmpty() && !patterns.contains(pattern)) {
                patterns.add(pattern);
            }
            if (patterns.size() == WorldOverrides.MAX_PATTERN_AMOUNT) {
                break;
            }
        }
        return patterns;
    }

    public static String format(Collection<String> values) {
        return values.stream()
                .map(value -> "`" + value + "`")
                .collect(Collectors.joining(", "));
    }
}
