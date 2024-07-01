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

package net.clementraynaud.skoice.menus;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.menus.selectmenus.SelectMenuFactory;
import net.clementraynaud.skoice.util.ConfigurationUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MenuFactory {

    private final Map<String, Menu> menus = new LinkedHashMap<>();
    private final Map<String, MenuField> fields = new HashMap<>();
    private final SelectMenuFactory selectMenuFactory = new SelectMenuFactory();

    public void loadAll(Skoice plugin) {
        YamlConfiguration fieldsYaml = ConfigurationUtil.loadResource(this.getClass().getName(), "menus/fields.yml");
        if (fieldsYaml == null) {
            return;
        }
        for (String field : fieldsYaml.getKeys(false)) {
            ConfigurationSection fieldSection = fieldsYaml.getConfigurationSection(field);
            if (fieldSection != null) {
                this.fields.put(field, new MenuField(plugin, fieldSection));
            }
        }

        YamlConfiguration menusYaml = ConfigurationUtil.loadResource(this.getClass().getName(), "menus/menus.yml");
        if (menusYaml == null) {
            return;
        }
        for (String menu : menusYaml.getKeys(false)) {
            ConfigurationSection menuSection = menusYaml.getConfigurationSection(menu);
            if (menuSection != null) {
                if ("configuration".equals(menu) || "linking-process".equals(menu) || "error".equals(menu)) {
                    for (String subMenu : menuSection.getKeys(false)) {
                        if (!"emoji".equals(subMenu) && !"footer".equals(subMenu)) {
                            ConfigurationSection subMenuSection = menusYaml.getConfigurationSection(menu + "." + subMenu);
                            if (subMenuSection != null) {
                                this.menus.put(subMenu, new Menu(plugin, subMenuSection));
                            }
                        }
                    }
                } else {
                    this.menus.put(menu, new Menu(plugin, menuSection));
                }
            }
        }
    }

    public Map<String, Menu> getMenus() {
        return this.menus;
    }

    public Menu getMenu(String menuId) {
        return this.menus.get(menuId);
    }

    public MenuField getField(String fieldId) {
        return this.fields.get(fieldId);
    }

    public SelectMenuFactory getSelectMenuFactory() {
        return this.selectMenuFactory;
    }
}
