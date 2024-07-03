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
import net.clementraynaud.skoice.menus.selectmenus.LoginNotificationSelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.SelectMenuFactory;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.clementraynaud.skoice.util.ConfigurationUtil;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
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
        this.loadFields(plugin);

        YamlConfiguration menusYaml = ConfigurationUtil.loadResource(this.getClass().getName(), "discord/menus/menus.yml");
        if (menusYaml == null) {
            return;
        }
        for (String menu : menusYaml.getKeys(false)) {
            ConfigurationSection menuSection = menusYaml.getConfigurationSection(menu);
            if (menuSection == null) {
                continue;
            }
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

        this.loadButtons(plugin);
    }

    private void loadFields(Skoice plugin) {
        YamlConfiguration fieldsYaml = ConfigurationUtil.loadResource(this.getClass().getName(), "discord/menus/fields.yml");
        if (fieldsYaml == null) {
            return;
        }
        for (String field : fieldsYaml.getKeys(false)) {
            ConfigurationSection fieldSection = fieldsYaml.getConfigurationSection(field);
            if (fieldSection != null) {
                this.fields.put(field, new MenuField(plugin, fieldSection));
            }
        }
    }

    private void loadButtons(Skoice plugin) {
        for (Menu menu : this.menus.values()) {
            switch (menu.getId()) {
                case "incomplete-configuration-server-manager":
                    menu.setButtons(Button.primary("resume-configuration",
                                    plugin.getBot().getLang().getMessage("button-label.resume-configuration"))
                            .withEmoji(MenuEmoji.ARROW_FORWARD.get()));
                    break;
                case "permissions":
                    menu.setButtons(Button.link(plugin.getBot().getInviteUrl(),
                                    plugin.getBot().getLang().getMessage("button-label.update-permissions"))
                            .withEmoji(MenuEmoji.CARD_BOX.get()));
                    break;
                case "range":
                    menu.setButtons(Button.primary("customize",
                                    plugin.getBot().getLang().getMessage("field.customize.title"))
                            .withEmoji(MenuEmoji.PENCIL2.get()));
                    break;
                case "login-notification":
                    if (LoginNotificationSelectMenu.REMIND_ONCE.equals(plugin.getConfigYamlFile().getString(ConfigField.LOGIN_NOTIFICATION.toString()))) {
                        menu.setButtons(Button.danger("clear-notified-players",
                                        plugin.getBot().getLang().getMessage("button-label.clear-notified-players"))
                                .withEmoji(MenuEmoji.WASTEBASKET.get()));
                    }
                    break;
                case "verification-code":
                    menu.setButtons(Button.secondary(Menu.MESSAGE_NOT_SHOWING_UP,
                                    plugin.getBot().getLang().getMessage("button-label.message-not-showing-up"))
                            .withEmoji(MenuEmoji.QUESTION.get()));
                    break;
                default:
                    break;
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
