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
import net.clementraynaud.skoice.common.menus.selectors.LoginNotificationSelector;
import net.clementraynaud.skoice.common.menus.selectors.SelectorFactory;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.clementraynaud.skoice.common.storage.config.WorldOverrides;
import net.clementraynaud.skoice.common.util.ConfigurationUtil;
import net.dv8tion.jda.api.components.buttons.Button;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MenuFactory {

    private final Map<String, Menu> menus = new LinkedHashMap<>();
    private final Map<String, MenuField> fields = new HashMap<>();
    private final SelectorFactory selectorFactory = new SelectorFactory();

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

    public List<Button> getButtons(Skoice plugin, String menuId, Map<String, String> args) {
        List<Button> buttons = new ArrayList<>();
        String scopedOverrideId = args.get(WorldOverrides.ARG);

        if (scopedOverrideId != null && WorldOverrides.isOverridableMenu(menuId)
                && plugin.getConfigYamlFile().scope(scopedOverrideId).isAnyOverridden(WorldOverrides.getFields(menuId))) {
            buttons.add(Button.danger(WorldOverrides.scopeId(WorldOverrides.RESET_BUTTON_ID, scopedOverrideId)
                                    + WorldOverrides.SCOPE_SEPARATOR + menuId,
                            plugin.getBot().getLang().getMessage("button-label.reset-to-global"))
                    .withEmoji(MenuEmoji.ARROWS_COUNTERCLOCKWISE.get()));
            return buttons;
        }

        switch (menuId) {
            case "incomplete-configuration-server-manager":
                buttons.add(Button.primary("configure-now",
                                plugin.getBot().getLang().getMessage("button-label.configure-now"))
                        .withEmoji(MenuEmoji.GEAR.get()));
                break;

            case "two-factor-authentication":
                buttons.add(Button.primary("refresh",
                                plugin.getBot().getLang().getMessage("button-label.refresh"))
                        .withEmoji(MenuEmoji.ARROWS_COUNTERCLOCKWISE.get()));
                break;

            case "permissions":
                if ("true".equals(args.get("invoker-is-bot-owner")) && plugin.getBot().getInviteUrl() != null) {
                    buttons.add(Button.link(plugin.getBot().getInviteUrl(),
                                    plugin.getBot().getLang().getMessage("button-label.update-permissions"))
                            .withEmoji(MenuEmoji.CARD_BOX.get()));
                }
                break;

            case "login-notification":
                if (LoginNotificationSelector.REMIND_ONCE.equals(plugin.getConfigYamlFile().getString(ConfigField.LOGIN_NOTIFICATION.toString()))) {
                    buttons.add(Button.danger("clear-notified-players",
                                    plugin.getBot().getLang().getMessage("button-label.clear-notified-players"))
                            .withEmoji(MenuEmoji.WASTEBASKET.get()));
                }
                break;

            case WorldOverrides.LIST_MENU_ID:
                if (plugin.getConfigYamlFile().getWorldOverrides().getAll().size() < WorldOverrides.MAX_AMOUNT) {
                    buttons.add(Button.primary(WorldOverrides.CREATE_BUTTON_ID,
                                    plugin.getBot().getLang().getMessage("button-label.create-override"))
                            .withEmoji(MenuEmoji.HEAVY_PLUS_SIGN.get()));
                }
                break;

            case WorldOverrides.OVERRIDE_MENU_ID: {
                String overrideId = args.get(WorldOverrides.ARG);
                if (overrideId == null) {
                    break;
                }
                WorldOverrides overrides = plugin.getConfigYamlFile().getWorldOverrides();
                int index = overrides.getIndex(overrideId);
                int amount = overrides.getAll().size();

                buttons.add(Button.secondary(WorldOverrides.scopeId(WorldOverrides.EDIT_BUTTON_ID, overrideId),
                                plugin.getBot().getLang().getMessage("button-label.rename-override"))
                        .withEmoji(MenuEmoji.PENCIL2.get()));
                buttons.add(Button.secondary(WorldOverrides.scopeId(WorldOverrides.MOVE_UP_BUTTON_ID, overrideId),
                                plugin.getBot().getLang().getMessage("button-label.move-override-up"))
                        .withEmoji(MenuEmoji.ARROW_UP.get())
                        .withDisabled(index <= 0));
                buttons.add(Button.secondary(WorldOverrides.scopeId(WorldOverrides.MOVE_DOWN_BUTTON_ID, overrideId),
                                plugin.getBot().getLang().getMessage("button-label.move-override-down"))
                        .withEmoji(MenuEmoji.ARROW_DOWN.get())
                        .withDisabled(index < 0 || index >= amount - 1));
                buttons.add(Button.danger(WorldOverrides.scopeId(WorldOverrides.DELETE_BUTTON_ID, overrideId),
                                plugin.getBot().getLang().getMessage("button-label.delete-override"))
                        .withEmoji(MenuEmoji.WASTEBASKET.get()));
                break;
            }

            case WorldOverrides.WORLDS_MENU_ID: {
                String overrideId = args.get(WorldOverrides.ARG);
                if (overrideId == null) {
                    break;
                }
                buttons.add(Button.secondary(WorldOverrides.scopeId(WorldOverrides.PATTERNS_BUTTON_ID, overrideId),
                                plugin.getBot().getLang().getMessage("button-label.edit-patterns"))
                        .withEmoji(MenuEmoji.PENCIL2.get()));
                break;
            }

            case WorldOverrides.DELETION_MENU_ID: {
                String overrideId = args.get(WorldOverrides.ARG);
                if (overrideId == null) {
                    break;
                }
                buttons.add(Button.danger(WorldOverrides.scopeId(WorldOverrides.DELETE_CONFIRM_BUTTON_ID, overrideId),
                                plugin.getBot().getLang().getMessage("button-label.confirm-deletion"))
                        .withEmoji(MenuEmoji.WASTEBASKET.get()));
                buttons.add(Button.secondary(WorldOverrides.scopeId(WorldOverrides.OVERRIDE_MENU_ID, overrideId),
                                plugin.getBot().getLang().getMessage("button-label.cancel"))
                        .withEmoji(MenuEmoji.ARROW_LEFT.get()));
                break;
            }

            default:
                break;
        }

        return buttons;
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

    public SelectorFactory getSelectorFactory() {
        return this.selectorFactory;
    }
}
