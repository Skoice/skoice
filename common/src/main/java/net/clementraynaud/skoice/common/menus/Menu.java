/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.simpleyaml.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        this.fields = menu.getStringList("fields").toArray(new String[0]);
    }

    public MessageCreateData build(Map<String, String> args) {
        return new MessageCreateBuilder().useComponentsV2()
                .setComponents(this.getContainer(args))
                .build();
    }

    private String getTitle(boolean withEmoji) {
        return withEmoji ? this.emoji + this.plugin.getBot().getLang().getMessage("menu." + this.section + ".title") :
                this.plugin.getBot().getLang().getMessage("menu." + this.section + ".title");
    }

    private String getDescription() {
        StringBuilder description = new StringBuilder();
        if (this.plugin.getBot().getLang().contains("menu." + this.section + ".description")) {
            description.append(this.plugin.getBot().getLang().getMessage("menu." + this.section + ".description"));
        }
        return description.toString();
    }

    private TextDisplay getContainerHeader() {
        String title = "## " + this.getTitle(true);
        String description = this.getDescription();

        if (this.plugin.getBot().getStatus() != BotStatus.READY
                || this.parent == null) {
            return TextDisplay.of(title + "\n" + description);
        } else {

        StringBuilder menuPath = new StringBuilder();
        String parentMenu = this.parent;

        while (parentMenu != null) {
            Menu menu = this.plugin.getBot().getMenuFactory().getMenu(parentMenu);
            menuPath.insert(0, menu.getTitle(false) + " › ");
            parentMenu = menu.parent;
        }

        return TextDisplay.of("**" + menuPath + "**\n" + title + "\n" + description);
        }
    }

    private TextDisplay getContainerFooter() {
        if (this.footer == null) {
            return null;
        }
        return TextDisplay.of("-# " + this.plugin.getBot().getLang().getMessage("menu." + this.footer + "-footer"));
    }

    private Container getContainer(Map<String, String> args) {
        List<ContainerChildComponent> childComponents = new ArrayList<>();

        childComponents.add(this.getContainerHeader());
        childComponents.add(Menu.LARGE_INVISIBLE_SEPARATOR);

        List<Menu> children = this.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Menu child = children.get(i);
            childComponents.addAll(child.getCompactForm(args));

            if (i < children.size() - 1) {
                childComponents.add(Menu.LARGE_DIVIDER_SEPARATOR);
            }
        }

        for (String field : this.fields) {
            MenuField menuField = this.plugin.getBot().getMenuFactory().getField(field);
            childComponents.add(menuField.build(args, false));
        }

        ActionRow selectMenuActionRow = this.getSelectMenuActionRow();
        if (selectMenuActionRow != null) {
            childComponents.add(selectMenuActionRow);
        }
        childComponents.add(Menu.LARGE_INVISIBLE_SEPARATOR);
        List<Button> buttons = this.plugin.getBot().getMenuFactory().getButtons(this.plugin, this.menuId);
        if (!buttons.isEmpty()) {
            childComponents.add(ActionRow.of(buttons));
        }
        childComponents.add(this.getButtonActionRow());
        childComponents.add(Menu.SMALL_INVISIBLE_SEPARATOR);
        childComponents.add(this.getContainerFooter());

        if (this.type == MenuType.DEFAULT) {
            return Container.of(childComponents);
        } else {
            return Container.of(childComponents)
                    .withAccentColor(this.type.getColor());
        }
    }

    private List<ContainerChildComponent> getCompactForm(Map<String, String> args) {
        List<ContainerChildComponent> childComponents = new ArrayList<>();

        String description = this.getDescription();
        if (description.isEmpty()) {
            description = this.getChildren().stream()
                    .map(menu -> "> " + menu.getTitle(true))
                    .collect(Collectors.joining("\n"));
        }

        TextDisplay text = TextDisplay.of("**" + this.getTitle(true) + "**\n" + description);
        if (this.getChildren().isEmpty()) {
            childComponents.add(text);

            for (String field : this.fields) {
                MenuField menuField = this.plugin.getBot().getMenuFactory().getField(field);
                childComponents.add(menuField.build(args, true));
            }

            ActionRow selectMenuActionRow = this.getSelectMenuActionRow();
            if (selectMenuActionRow != null) {
                childComponents.add(selectMenuActionRow);
            }

        } else {
            List<String> unreviewedSettings = this.plugin.getConfigYamlFile().getStringList(ConfigField.UNREVIEWED_SETTINGS.toString());
            childComponents.add(Section.of(unreviewedSettings.contains(this.menuId)
                            ? Button.of(ButtonStyle.SUCCESS, this.menuId, this.plugin.getBot().getLang().getMessage("button-label.explore-more-settings") + " ❯")
                            : Button.of(ButtonStyle.SECONDARY, this.menuId, "❯"),
                    text
            ));
        }

        List<Button> buttons = this.plugin.getBot().getMenuFactory().getButtons(this.plugin, this.menuId);
        if (!buttons.isEmpty()) {
            childComponents.add(ActionRow.of(buttons));
        }

        return childComponents;
    }

    private ActionRow getButtonActionRow() {
        List<Button> secondaryButtons = new ArrayList<>();

        if ("settings".equals(this.getRoot()) || "language".equals(this.menuId)) {
            if (this.plugin.getBot().getStatus() == BotStatus.READY || "language".equals(this.menuId)) {
                String backButtonId;
                if ("language".equals(this.menuId)) {
                    backButtonId = "settings";
                } else if (this.parent == null) {
                    backButtonId = "unreachable";
                } else {
                    backButtonId = this.parent;
                }

                Button backButton = Button.secondary(backButtonId, "← " + this.plugin.getBot().getLang().getMessage("button-label.back"));
                if (this.parent == null && !"language".equals(this.menuId)) {
                    backButton = backButton.asDisabled();
                }

                secondaryButtons.add(backButton);
            }

            if (!"language".equals(this.menuId)) {
                Menu languageMenu = this.plugin.getBot().getMenuFactory().getMenu("language");
                secondaryButtons.add(Button.secondary(languageMenu.menuId, languageMenu.getTitle(false))
                        .withEmoji(MenuEmoji.GLOBE_WITH_MERIDIANS.get()));
            }
        }

        secondaryButtons.add(Button.link("https://discord.gg/skoice-proximity-voice-chat-741375523275407461",
                        this.plugin.getBot().getLang().getMessage("button-label.support-server"))
                .withEmoji(MenuEmoji.SCREWDRIVER.get()));

        return ActionRow.of(secondaryButtons);
    }

    private ActionRow getSelectMenuActionRow() {
        Selector selector = this.plugin.getBot()
                .getMenuFactory()
                .getSelectorFactory()
                .getSelector(this.plugin, this.menuId);
        if (selector == null) {
            return null;
        }
        return ActionRow.of(selector.get());
    }

    private String getRoot() {
        Menu root = this;
        while (root.parent != null) {
            root = this.plugin.getBot().getMenuFactory().getMenu(root.parent);
        }
        return root.menuId;
    }

    private List<Menu> getChildren() {
        return this.plugin.getBot().getMenuFactory().getMenus().values().stream()
                .filter(menu -> menu.parent != null && menu.parent.equals(this.menuId))
                .collect(Collectors.toList());
    }

    public String getId() {
        return this.menuId;
    }
}
