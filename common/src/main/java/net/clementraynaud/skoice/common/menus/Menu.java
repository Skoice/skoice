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
import net.clementraynaud.skoice.common.bot.BotStatus;
import net.clementraynaud.skoice.common.menus.selectors.Selector;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.clementraynaud.skoice.common.storage.config.WorldOverrides;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.internal.components.utils.ComponentsUtil;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Menu {

    private static final Separator SMALL_INVISIBLE_SEPARATOR = Separator.createInvisible(Separator.Spacing.SMALL);
    private static final Separator LARGE_INVISIBLE_SEPARATOR = Separator.createInvisible(Separator.Spacing.LARGE);
    private static final Separator LARGE_DIVIDER_SEPARATOR = Separator.createDivider(Separator.Spacing.LARGE);

    private final Skoice plugin;
    private final String menuId;
    private final String section;
    private final String footer;
    private final MenuEmoji emoji;
    private final MenuType type;
    private final String parent;
    private final boolean hidden;
    private final boolean navigable;
    private final boolean compact;
    private final String[] fields;

    public Menu(Skoice plugin, ConfigurationSection menu) {
        this.plugin = plugin;
        this.menuId = menu.getName();
        this.section = !menu.getParent().equals(menu.getRoot()) ? menu.getParent().getName() : this.menuId;
        this.footer = !menu.getParent().equals(menu.getRoot())
                ? menu.getParent().getString("footer")
                : menu.getString("footer");
        this.emoji = MenuEmoji.valueOf(!menu.getParent().equals(menu.getRoot())
                ? menu.getParent().getString("emoji").toUpperCase()
                : menu.getString("emoji").toUpperCase());
        this.type = menu.contains("type") ? MenuType.valueOf(menu.getString("type").toUpperCase()) : null;
        this.parent = menu.contains("parent") ? menu.getString("parent") : null;
        this.hidden = menu.getBoolean("hidden");
        this.navigable = menu.getBoolean("navigable");
        this.compact = menu.getBoolean("compact");
        this.fields = menu.getStringList("fields").toArray(new String[0]);
    }

    public MessageCreateData build(Map<String, String> args) {
        return new MessageCreateBuilder().useComponentsV2()
                .setComponents(this.getContainer(WorldOverrideMenus.enrich(this.plugin, args)))
                .build();
    }

    private String getTitle(boolean withEmoji) {
        return this.getTitle(withEmoji, Collections.emptyMap());
    }

    private String getTitle(boolean withEmoji, Map<String, String> args) {
        String titleKey = "menu." + this.section + ".title";
        if (args.containsKey(WorldOverrides.ARG)
                && this.plugin.getBot().getLang().contains("menu." + this.section + ".override-title")) {
            titleKey = "menu." + this.section + ".override-title";
        }
        String title = this.plugin.getBot().getLang().getMessage(titleKey);

        if (WorldOverrides.OVERRIDE_MENU_ID.equals(this.menuId)) {
            String name = args.get("name");
            if (name != null) {
                title = name;
            }
        }

        return withEmoji ? this.emoji + title : title;
    }

    private String getDescription(Map<String, String> args) {
        String descriptionKey = "menu." + this.section + ".description";
        if ("permissions".equals(this.section) && "true".equals(args.get("invoker-is-bot-owner"))) {
            descriptionKey = "menu." + this.section + ".description-owner";
        }
        if (args.containsKey(WorldOverrides.ARG)
                && this.plugin.getBot().getLang().contains("menu." + this.section + ".override-description")) {
            descriptionKey = "menu." + this.section + ".override-description";
        }
        if (!this.plugin.getBot().getLang().contains(descriptionKey)) {
            return "";
        }

        String description = this.plugin.getBot().getLang().getMessage(descriptionKey, args);

        if (WorldOverrides.OVERRIDE_MENU_ID.equals(this.menuId)) {
            String note = WorldOverrideMenus.getStatusNote(this.plugin, args.get(WorldOverrides.ARG));
            if (note != null) {
                description += "\n" + note;
            }
        }

        if (WorldOverrides.WORLDS_MENU_ID.equals(this.menuId)) {
            description += "\n" + WorldOverrideMenus.getPatternNote(this.plugin, args.get(WorldOverrides.ARG));
        }

        return description;
    }

    private ActionRow getMenuPathActionRow(Map<String, String> args) {
        List<Button> buttons = new ArrayList<>();
        String overrideId = args.get(WorldOverrides.ARG);

        if ("language".equals(this.menuId)) {
            String parentMenu = this.plugin.getBot().getStatus().getMenuId();
            Menu menu = this.plugin.getBot().getMenuFactory().getMenu(parentMenu);
            buttons.add(Button.secondary(parentMenu,
                    menu.getTitle(false) + " ›"));

        } else if (this.plugin.getBot().getStatus() == BotStatus.READY) {
            if (overrideId != null && !WorldOverrides.OVERRIDE_MENU_ID.equals(this.menuId)) {
                // A menu scoped to an override always hangs under that override, wherever it usually sits.
                Menu overrideMenu = this.plugin.getBot().getMenuFactory().getMenu(WorldOverrides.OVERRIDE_MENU_ID);
                buttons.add(Button.secondary(
                        WorldOverrides.scopeId(WorldOverrides.OVERRIDE_MENU_ID, overrideId),
                        overrideMenu.getTitle(false, args) + " ›"));
                this.prependParents(buttons, overrideMenu.parent);
            } else {
                this.prependParents(buttons, this.parent);
            }
        }

        if (buttons.isEmpty()) {
            return null;
        }

        return ActionRow.of(buttons);
    }

    private void prependParents(List<Button> buttons, String parentMenu) {
        while (parentMenu != null) {
            Menu menu = this.plugin.getBot().getMenuFactory().getMenu(parentMenu);
            buttons.add(0, Button.secondary(parentMenu, menu.getTitle(false) + " ›"));
            parentMenu = menu.parent;
        }
    }

    private ContainerChildComponent getTitleComponent(Map<String, String> args) {
        String title = "## " + this.getTitle(true, args);

        if ("settings".equals(this.getRoot())) {
            Menu languageMenu = this.plugin.getBot().getMenuFactory().getMenu("language");
            return Section.of(Button.secondary(languageMenu.menuId, languageMenu.getTitle(false))
                            .withEmoji(MenuEmoji.GLOBE_WITH_MERIDIANS.get()),
                    TextDisplay.of(title));
        }

        return TextDisplay.of(title);
    }

    private Section getFooterSection() {
        if (this.footer == null) {
            return null;
        }
        return Section.of(Button.link("https://discord.gg/skoice-proximity-voice-chat-741375523275407461",
                                this.plugin.getBot().getLang().getMessage("button-label.support-server"))
                        .withEmoji(MenuEmoji.DISCORD.get()),
                TextDisplay.of("-# " + this.plugin.getBot().getLang().getMessage("menu." + this.footer + "-footer")));
    }

    private Container getContainer(Map<String, String> args) {
        Container container = this.buildContainer(args, true);
        if (ComponentsUtil.getComponentTreeSize(Collections.singletonList(container))
                > Message.MAX_COMPONENT_COUNT_IN_COMPONENT_TREE) {
            container = this.buildContainer(args, false);
        }
        return container;
    }

    private Container buildContainer(Map<String, String> args, boolean withSeparators) {
        List<ContainerChildComponent> childComponents = new ArrayList<>();

        ActionRow menuPathSection = this.getMenuPathActionRow(args);
        if (menuPathSection != null) {
            childComponents.add(menuPathSection);
        }

        childComponents.add(this.getTitleComponent(args));

        String description = this.getDescription(args);
        if (!description.isEmpty()) {
            childComponents.add(TextDisplay.of(description));
        }

        childComponents.addAll(this.getWorldOverrideComponents(args));

        if (withSeparators) {
            childComponents.add(Menu.LARGE_INVISIBLE_SEPARATOR);
        }

        List<Menu> children = this.getChildren(args);
        for (int i = 0; i < children.size(); i++) {
            Menu child = children.get(i);
            childComponents.addAll(child.getCompactForm(args));

            if (withSeparators && !this.compact && i < children.size() - 1) {
                childComponents.add(Menu.LARGE_DIVIDER_SEPARATOR);
            }
        }

        for (String field : this.fields) {
            MenuField menuField = this.plugin.getBot().getMenuFactory().getField(field);
            childComponents.add(menuField.build(args, false));
        }

        childComponents.addAll(this.getSelectMenuActionRows(args));

        List<Button> buttons = this.plugin.getBot().getMenuFactory().getButtons(this.plugin, this.menuId, args);
        if (!buttons.isEmpty()) {
            if (withSeparators) {
                childComponents.add(Menu.LARGE_INVISIBLE_SEPARATOR);
            }
            childComponents.add(ActionRow.of(buttons));
        }
        if (withSeparators) {
            childComponents.add(Menu.SMALL_INVISIBLE_SEPARATOR);
        }
        childComponents.add(this.getFooterSection());

        if (this.type == MenuType.DEFAULT) {
            return Container.of(childComponents);
        } else {
            return Container.of(childComponents)
                    .withAccentColor(this.type.getColor());
        }
    }

    private List<ContainerChildComponent> getCompactForm(Map<String, String> args) {
        List<ContainerChildComponent> childComponents = new ArrayList<>();
        String overrideId = args.get(WorldOverrides.ARG);
        boolean scoped = overrideId != null && WorldOverrides.isScopedMenu(this.menuId);

        String description = this.getDescription(args);
        if (description.isEmpty()) {
            description = this.getChildren().stream()
                    .map(menu -> "> " + menu.getTitle(true))
                    .collect(Collectors.joining("\n"));
        }

        String title = "**" + this.getTitle(true, args) + "**\n" + description;
        if (scoped && WorldOverrides.isOverridableMenu(this.menuId)) {
            title += "\n-# " + this.plugin.getBot().getLang().getMessage(
                    this.plugin.getConfigYamlFile().scope(overrideId).isAnyOverridden(WorldOverrides.getFields(this.menuId))
                            ? "menu.world-override.overridden"
                            : "menu.world-override.inherited");
        }

        List<Menu> children = this.getChildren();

        if (children.isEmpty() && !this.navigable && !scoped) {
            childComponents.add(TextDisplay.of(title));

            for (String field : this.fields) {
                MenuField menuField = this.plugin.getBot().getMenuFactory().getField(field);
                childComponents.add(menuField.build(args, true));
            }

            childComponents.addAll(this.getSelectMenuActionRows(args));

            List<Button> buttons = this.plugin.getBot().getMenuFactory().getButtons(this.plugin, this.menuId, args);
            if (!buttons.isEmpty()) {
                childComponents.add(ActionRow.of(buttons));
            }

        } else {
            String componentId = scoped ? WorldOverrides.scopeId(this.menuId, overrideId) : this.menuId;
            List<String> unreviewedSettings = this.plugin.getConfigYamlFile().getStringList(ConfigField.UNREVIEWED_SETTINGS.toString());
            childComponents.add(Section.of(unreviewedSettings.contains(this.menuId)
                            ? Button.of(ButtonStyle.SUCCESS, componentId, this.plugin.getBot().getLang().getMessage("button-label.explore-more-settings") + " ❯")
                            : Button.of(ButtonStyle.SECONDARY, componentId, "❯"),
                    TextDisplay.of(title)
            ));
        }

        return childComponents;
    }

    private List<ContainerChildComponent> getWorldOverrideComponents(Map<String, String> args) {
        if (WorldOverrides.LIST_MENU_ID.equals(this.menuId)) {
            return Arrays.asList(
                    TextDisplay.of("-# " + this.plugin.getBot().getLang().getMessage("menu.world-overrides.rule")),
                    TextDisplay.of(WorldOverrideMenus.getList(this.plugin))
            );
        }

        return Collections.emptyList();
    }

    private List<ActionRow> getSelectMenuActionRows(Map<String, String> args) {
        List<Selector> selectors = this.plugin.getBot()
                .getMenuFactory()
                .getSelectorFactory()
                .getSelectors(this.plugin, this.menuId, args);

        if (selectors == null || selectors.isEmpty()) {
            return Collections.emptyList();
        }

        return selectors.stream()
                .map(Selector::get)
                .filter(Objects::nonNull)
                .map(ActionRow::of)
                .collect(Collectors.toList());
    }

    private String getRoot() {
        Menu root = this;
        while (root.parent != null) {
            root = this.plugin.getBot().getMenuFactory().getMenu(root.parent);
        }
        return root.menuId;
    }

    private List<Menu> getChildren(Map<String, String> args) {
        if (WorldOverrides.OVERRIDE_MENU_ID.equals(this.menuId)) {
            List<String> childIds = new ArrayList<>();
            childIds.add(WorldOverrides.WORLDS_MENU_ID);
            childIds.addAll(WorldOverrides.getOverridableMenuIds());
            return childIds.stream()
                    .map(id -> this.plugin.getBot().getMenuFactory().getMenu(id))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return this.getChildren();
    }

    private List<Menu> getChildren() {
        return this.plugin.getBot().getMenuFactory().getMenus().values().stream()
                .filter(menu -> !menu.hidden)
                .filter(menu -> menu.parent != null && menu.parent.equals(this.menuId))
                .collect(Collectors.toList());
    }

    public String getId() {
        return this.menuId;
    }
}
