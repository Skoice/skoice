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
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.menus.selectmenus.SelectMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Menu {

    private final Skoice plugin;
    private final String menuId;
    private final String parentName;
    private final String footer;
    private final MenuEmoji emoji;
    private final MenuType type;
    private final MenuStyle style;
    private final String parent;
    private final String[] fields;
    private Button[] buttons;

    public Menu(Skoice plugin, ConfigurationSection menu) {
        this.plugin = plugin;
        this.menuId = menu.getName();
        this.parentName = !menu.getParent().equals(menu.getRoot()) ? menu.getParent().getName() : this.menuId;
        this.footer = !menu.getParent().equals(menu.getRoot())
                ? menu.getParent().getString("footer")
                : menu.getString("footer");
        this.emoji = MenuEmoji.valueOf(!menu.getParent().equals(menu.getRoot())
                ? menu.getParent().getString("emoji").toUpperCase()
                : menu.getString("emoji").toUpperCase());
        this.type = menu.contains("type") ? MenuType.valueOf(menu.getString("type").toUpperCase()) : null;
        this.style = menu.contains("style") ? MenuStyle.valueOf(menu.getString("style").toUpperCase()) : null;
        this.parent = menu.contains("parent") ? menu.getString("parent") : null;
        this.fields = menu.getStringList("fields").toArray(new String[0]);
        this.buttons = new Button[0];
    }

    public MessageCreateData build(String... args) {
        return new MessageCreateBuilder().setEmbeds(this.getEmbed(args))
                .setComponents(this.getActionRows()).build();
    }

    private String getTitle(boolean withEmoji) {
        return withEmoji ? this.emoji + this.plugin.getBot().getLang().getMessage("menu." + this.parentName + ".title") :
                this.plugin.getBot().getLang().getMessage("menu." + this.parentName + ".title");
    }

    private String getDescription(boolean shortened) {
        if (shortened && this.plugin.getBot().getLang().contains("menu." + this.parentName + ".shortened-description")) {
            return this.plugin.getBot().getLang().getMessage("menu." + this.parentName + ".shortened-description");
        } else if (this.plugin.getBot().getLang().contains("menu." + this.parentName + ".description")) {
            return this.plugin.getBot().getLang().getMessage("menu." + this.parentName + ".description");
        }
        return null;
    }

    private MessageEmbed getEmbed(String... args) {
        EmbedBuilder embed = new EmbedBuilder().setTitle(this.getTitle(true))
                .setColor(this.type.getColor());

        if (this.footer != null) {
            embed.setFooter(this.plugin.getBot().getLang().getMessage("menu." + this.footer + "-footer"),
                    "https://clementraynaud.net/Skoice.jpeg");
        }

        if (this.getDescription(false) != null) {
            embed.setDescription(this.getDescription(false));
        }
        if (this.plugin.getBot().getStatus() == BotStatus.READY) {
            StringBuilder author = new StringBuilder();
            String parentMenu = this.parent;
            while (parentMenu != null) {
                Menu menuParent = this.plugin.getBot().getMenuFactory().getMenu(parentMenu);
                author.insert(0, menuParent.getTitle(false) + " › ");
                parentMenu = menuParent.parent;
            }
            embed.setAuthor(author.toString());
        }
        for (Menu menu : this.plugin.getBot().getMenuFactory().getMenus().values()) {
            String description = menu.getDescription(true);
            if (menu.parent != null && menu.parent.equals(this.menuId) && description != null) {
                embed.addField(menu.getTitle(true), description, true);
            }
        }
        int startIndex = 0;
        for (String field : this.fields) {
            MenuField menuField = this.plugin.getBot().getMenuFactory().getField(field);
            int endIndex = this.plugin.getBot().getLang().getAmountOfArgsRequired(menuField.getDescription());
            embed.addField(menuField.build(Arrays.copyOfRange(args, startIndex, endIndex)));
            startIndex = endIndex;
        }
        return embed.build();
    }

    private List<ActionRow> getActionRows() {
        List<ActionRow> actionRows = new ArrayList<>();

        ActionRow selectMenuActionRow = this.getSelectMenuActionRow();
        if (selectMenuActionRow != null) {
            actionRows.add(selectMenuActionRow);
        }

        actionRows.addAll(this.getMainActionRows());

        ActionRow secondaryActionRow = this.getSecondaryActionRow();
        if (secondaryActionRow != null) {
            actionRows.add(secondaryActionRow);
        }

        return actionRows;
    }

    private List<ActionRow> getMainActionRows() {
        List<ActionRow> mainActionRows = new ArrayList<>();
        List<Button> mainButtons = new ArrayList<>(Arrays.asList(this.buttons));
        for (Menu menu : this.plugin.getBot().getMenuFactory().getMenus().values()) {
            if (menu.parent != null && menu.parent.equals(this.menuId)) {
                mainButtons.add(menu.style == MenuStyle.PRIMARY
                        ? Button.primary(menu.menuId, menu.getTitle(false))
                        .withEmoji(menu.emoji.get())
                        : Button.secondary(menu.menuId, menu.getTitle(false))
                        .withEmoji(menu.emoji.get()));
            }
        }

        for (int i = 0; i < mainButtons.size(); i += 5) {
            mainActionRows.add(ActionRow.of(mainButtons.subList(i, Math.min(i + 5, mainButtons.size()))));
        }

        return mainActionRows;
    }

    private ActionRow getSelectMenuActionRow() {
        SelectMenu selectMenu = this.plugin.getBot()
                .getMenuFactory()
                .getSelectMenuFactory()
                .getSelectMenu(this.plugin, this.menuId);
        if (selectMenu == null) {
            return null;
        }
        return ActionRow.of(selectMenu.get());
    }

    private ActionRow getSecondaryActionRow() {
        List<Button> secondaryButtons = new ArrayList<>();
        if (this.parent != null && (this.plugin.getBot().getStatus() == BotStatus.READY || "language".equals(this.menuId))) {
            secondaryButtons.add(Button.secondary(this.parent, "← " + this.plugin.getBot().getLang().getMessage("button-label.back")));
        }
        secondaryButtons.add(Button.secondary("display-issues",
                        this.plugin.getBot().getLang().getMessage("button-label.display-issues"))
                .withEmoji(MenuEmoji.QUESTION.get()));
        if (this.type == MenuType.DEFAULT
                && this.plugin.getBot().getStatus() != BotStatus.READY
                && !"language".equals(this.menuId)) {
            Menu languageMenu = this.plugin.getBot().getMenuFactory().getMenu("language");
            secondaryButtons.add(Button.secondary(languageMenu.menuId, languageMenu.getTitle(false))
                    .withEmoji(MenuEmoji.GLOBE_WITH_MERIDIANS.get()));
        }

        return ActionRow.of(secondaryButtons);
    }

    public void setButtons(Button... buttons) {
        this.buttons = buttons;
    }

    public String getId() {
        return this.menuId;
    }
}
