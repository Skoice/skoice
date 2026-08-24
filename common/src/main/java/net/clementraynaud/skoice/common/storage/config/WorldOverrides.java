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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class WorldOverrides {

    public static final String ORDER_FIELD = "world-overrides";
    public static final String KEY_PREFIX = "world-override.";
    public static final String NAME_FIELD = "name";
    public static final String WORLDS_FIELD = "worlds";
    public static final String PATTERNS_FIELD = "patterns";
    public static final String ACTIVE_FIELD = "active";

    public static final String ARG = "override";
    public static final String SCOPE_SEPARATOR = "@";

    public static final String LIST_MENU_ID = "world-overrides";
    public static final String OVERRIDE_MENU_ID = "world-override";
    public static final String DELETION_MENU_ID = "world-override-deletion";
    public static final String ACTIVE_WORLDS_MENU_ID = "active-worlds";
    public static final String WORLDS_MENU_ID = "world-override-worlds";

    public static final String SELECT_ID = "world-override-selection";
    public static final String WORLDS_SELECT_ID = "world-override-worlds-selection";
    public static final String PATTERNS_BUTTON_ID = "world-override-patterns";
    public static final String ACTIVE_SELECT_ID = "world-active";
    public static final String CREATE_BUTTON_ID = "world-override-create";
    public static final String EDIT_BUTTON_ID = "world-override-edit";
    public static final String MOVE_UP_BUTTON_ID = "world-override-up";
    public static final String MOVE_DOWN_BUTTON_ID = "world-override-down";
    public static final String DELETE_BUTTON_ID = "world-override-delete";
    public static final String DELETE_CONFIRM_BUTTON_ID = "world-override-delete-confirm";
    public static final String RESET_BUTTON_ID = "world-override-reset";
    public static final String NAME_INPUT_ID = "override-name";
    public static final String PATTERNS_INPUT_ID = "override-patterns";

    public static final int MAX_AMOUNT = 25;
    public static final int MAX_PATTERN_AMOUNT = 25;

    private static final Map<String, List<String>> FIELDS_BY_MENU = WorldOverrides.buildFieldsByMenu();

    private final ConfigStore store;
    private final ConfigScope globalScope;
    private final Map<String, ConfigScope> scopeByWorld = new ConcurrentHashMap<>();

    private volatile List<WorldOverride> overrides = Collections.emptyList();

    WorldOverrides(ConfigStore store) {
        this.store = store;
        this.globalScope = new ConfigScope(store, null);
    }

    private static Map<String, List<String>> buildFieldsByMenu() {
        Map<String, List<String>> fields = new LinkedHashMap<>();
        fields.put(WorldOverrides.ACTIVE_WORLDS_MENU_ID, Collections.singletonList(WorldOverrides.ACTIVE_FIELD));
        fields.put("range", Arrays.asList(ConfigField.HORIZONTAL_RADIUS.toString(),
                ConfigField.VERTICAL_RADIUS.toString()));
        fields.put("excluded-players", Arrays.asList(ConfigField.PLAYERS_ON_DEATH_SCREEN_EXCLUDED.toString(),
                ConfigField.SPECTATORS_EXCLUDED.toString(),
                ConfigField.EXCLUDED_PLAYERS_COMMUNICATION.toString()));
        fields.put("teams", Arrays.asList(ConfigField.TEAM_COMMUNICATION.toString(),
                ConfigField.SEPARATED_TEAMS.toString()));
        fields.put("action-bar-alerts", Arrays.asList(ConfigField.CONNECTING_ALERT.toString(),
                ConfigField.DISCONNECTING_ALERT.toString(),
                ConfigField.MUTED_ALERT.toString(),
                ConfigField.DEAFENED_ALERT.toString(),
                ConfigField.LINKING_SUGGESTION.toString()));
        return Collections.unmodifiableMap(fields);
    }

    public static List<String> getOverridableMenuIds() {
        return new ArrayList<>(WorldOverrides.FIELDS_BY_MENU.keySet());
    }

    public static boolean isOverridableMenu(String menuId) {
        return WorldOverrides.FIELDS_BY_MENU.containsKey(menuId);
    }

    public static boolean isScopedMenu(String menuId) {
        return WorldOverrides.isOverridableMenu(menuId) || WorldOverrides.WORLDS_MENU_ID.equals(menuId);
    }

    public static List<String> getFields(String menuId) {
        return WorldOverrides.FIELDS_BY_MENU.getOrDefault(menuId, Collections.emptyList());
    }

    public static String getFieldPath(String overrideId, String field) {
        return WorldOverrides.KEY_PREFIX + overrideId + "." + field;
    }

    public static String[] split(String componentId) {
        return componentId.split(WorldOverrides.SCOPE_SEPARATOR, -1);
    }

    public static String scopeId(String baseId, String overrideId) {
        return overrideId == null ? baseId : baseId + WorldOverrides.SCOPE_SEPARATOR + overrideId;
    }

    void load() {
        List<WorldOverride> loaded = new ArrayList<>();
        for (String id : this.store.getStringList(WorldOverrides.ORDER_FIELD)) {
            String name = this.store.getString(WorldOverrides.getFieldPath(id, WorldOverrides.NAME_FIELD));
            if (name == null) {
                continue;
            }
            loaded.add(new WorldOverride(this.store, id, name,
                    this.store.getStringList(WorldOverrides.getFieldPath(id, WorldOverrides.WORLDS_FIELD)),
                    this.store.getStringList(WorldOverrides.getFieldPath(id, WorldOverrides.PATTERNS_FIELD))));
        }
        this.overrides = Collections.unmodifiableList(loaded);
        this.scopeByWorld.clear();
    }

    public List<WorldOverride> getAll() {
        return this.overrides;
    }

    public boolean isEmpty() {
        return this.overrides.isEmpty();
    }

    public WorldOverride get(String id) {
        if (id == null) {
            return null;
        }
        return this.overrides.stream()
                .filter(override -> override.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public int getIndex(String id) {
        for (int i = 0; i < this.overrides.size(); i++) {
            if (this.overrides.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public WorldOverride resolve(String world) {
        if (world == null) {
            return null;
        }
        for (WorldOverride override : this.overrides) {
            if (override.matches(world)) {
                return override;
            }
        }
        return null;
    }

    public ConfigScope getScope(String world) {
        if (world == null || this.overrides.isEmpty()) {
            return this.globalScope;
        }
        return this.scopeByWorld.computeIfAbsent(world, name -> {
            WorldOverride override = this.resolve(name);
            return override == null ? this.globalScope : override.getScope();
        });
    }

    public WorldOverride getActivityController(String world) {
        WorldOverride override = this.resolve(world);
        return override != null && override.getScope().isOverridden(WorldOverrides.ACTIVE_FIELD)
                ? override
                : null;
    }

    public ConfigScope getScope(WorldOverride override) {
        return override == null ? this.globalScope : override.getScope();
    }

    public ConfigScope getGlobalScope() {
        return this.globalScope;
    }

    public boolean isEnabledAnywhere(String field) {
        if (this.globalScope.getBoolean(field)) {
            return true;
        }
        return this.overrides.stream().anyMatch(override -> override.getScope().getBoolean(field));
    }

    public int getHighestInt(String field) {
        int highest = this.globalScope.getInt(field);
        for (WorldOverride override : this.overrides) {
            highest = Math.max(highest, override.getScope().getInt(field));
        }
        return highest;
    }

    public WorldOverride create(String name) {
        String id = this.generateId();
        this.store.set(WorldOverrides.getFieldPath(id, WorldOverrides.NAME_FIELD), name);

        List<String> order = this.getOrder();
        order.add(id);
        this.saveOrder(order);

        return this.get(id);
    }

    public void rename(String id, String name) {
        this.setField(id, WorldOverrides.NAME_FIELD, name);
    }

    public void setWorlds(String id, List<String> worlds) {
        this.setField(id, WorldOverrides.WORLDS_FIELD, worlds);
    }

    public void setPatterns(String id, List<String> patterns) {
        this.setField(id, WorldOverrides.PATTERNS_FIELD, patterns);
    }

    private void setField(String id, String field, Object value) {
        if (this.get(id) == null) {
            return;
        }
        this.store.set(WorldOverrides.getFieldPath(id, field), value);
        this.load();
    }

    public void delete(String id) {
        List<String> order = this.getOrder();
        if (!order.remove(id)) {
            return;
        }
        this.store.removeKeysStartingWith(WorldOverrides.KEY_PREFIX + id + ".");
        this.saveOrder(order);
    }

    public boolean move(String id, int offset) {
        List<String> order = this.getOrder();
        int index = order.indexOf(id);
        int target = index + offset;
        if (index == -1 || target < 0 || target >= order.size()) {
            return false;
        }
        order.remove(index);
        order.add(target, id);
        this.saveOrder(order);
        return true;
    }

    private List<String> getOrder() {
        return new ArrayList<>(this.store.getStringList(WorldOverrides.ORDER_FIELD));
    }

    private void saveOrder(List<String> order) {
        this.store.set(WorldOverrides.ORDER_FIELD, order);
        this.load();
    }

    private String generateId() {
        Set<String> used = new LinkedHashSet<>(this.store.getStringList(WorldOverrides.ORDER_FIELD));
        int candidate = 1;
        while (used.contains(String.valueOf(candidate))) {
            candidate++;
        }
        return String.valueOf(candidate);
    }
}
