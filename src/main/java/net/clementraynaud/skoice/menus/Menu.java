/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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
import net.clementraynaud.skoice.menus.selectmenus.*;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Menu {

    public static final String CLOSE_BUTTON_ID = "close";

    private final Skoice plugin;
    private final String name;
    private final String parentName;
    private final MenuEmoji emoji;
    private final MenuType type;
    private final MenuStyle style;
    private final String parent;
    private final String[] fields;
    private SelectMenu selectMenu;

    public Menu(Skoice plugin, ConfigurationSection menu) {
        this.plugin = plugin;
        this.name = menu.getName();
        this.parentName = !menu.getParent().equals(menu.getRoot()) ? menu.getParent().getName() : this.name;
        this.emoji = MenuEmoji.valueOf(!menu.getParent().equals(menu.getRoot())
                ? menu.getParent().getString("emoji").toUpperCase()
                : menu.getString("emoji").toUpperCase());
        this.type = menu.contains("type") ? MenuType.valueOf(menu.getString("type").toUpperCase()) : null;
        this.style = menu.contains("style") ? MenuStyle.valueOf(menu.getString("style").toUpperCase()) : null;
        this.parent = menu.contains("parent") ? menu.getString("parent") : null;
        this.fields = menu.getStringList("fields").toArray(new String[0]);
    }

    public MessageCreateData build(String... args) {
        return new MessageCreateBuilder().setEmbeds(this.getEmbed(args))
                .setComponents(this.getActionRows()).build();
    }

    private String getTitle(boolean withEmoji) {
        return withEmoji ? this.emoji + this.plugin.getLang().getMessage("discord.menu." + this.parentName + ".title") :
                this.plugin.getLang().getMessage("discord.menu." + this.parentName + ".title");
    }

    private String getDescription(boolean shortened) {
        if (this.plugin.getBot().getStatus() != BotStatus.READY && this.plugin.getLang().contains("discord.menu." + this.parentName + ".alternative-description")) {
            return this.plugin.getLang().getMessage("discord.menu." + this.parentName + ".alternative-description");
        } else if (shortened && this.plugin.getLang().contains("discord.menu." + this.parentName + ".shortened-description")) {
            return this.plugin.getLang().getMessage("discord.menu." + this.parentName + ".shortened-description");
        } else if (this.plugin.getLang().contains("discord.menu." + this.parentName + ".description")) {
            return this.plugin.getLang().getMessage("discord.menu." + this.parentName + ".description");
        }
        return null;
    }

    private MessageEmbed getEmbed(String... args) {
        EmbedBuilder embed = new EmbedBuilder().setTitle(this.getTitle(true))
                .setColor(this.type.getColor())
                .setFooter(this.plugin.getLang().getMessage("discord.menu.footer"),
                        "https://www.spigotmc.org/data/resource_icons/82/82861.jpg?1597701409");
        if (this.getDescription(false) != null) {
            embed.setDescription(this.getDescription(false));
        }
        if (this.plugin.getBot().getStatus() == BotStatus.READY) {
            StringBuilder author = new StringBuilder();
            String parentMenu = this.parent;
            while (parentMenu != null) {
                Menu menuParent = this.plugin.getBot().getMenu(parentMenu);
                author.insert(0, menuParent.getTitle(false) + " › ");
                parentMenu = menuParent.parent;
            }
            embed.setAuthor(author.toString());
        }
        if (!"range".equals(this.name)) {
            for (Menu menu : this.plugin.getBot().getMenus().values()) {
                String description = menu.getDescription(true);
                if (menu.parent != null && menu.parent.equals(this.name) && description != null) {
                    embed.addField(menu.getTitle(true), description, true);
                }
            }
        }
        int startIndex = 0;
        for (String field : this.fields) {
            if ("customize".equals(field) && this.plugin.getBot().getStatus() != BotStatus.READY) {
                break;
            }
            MenuField menuField = this.plugin.getBot().getField(field);
            int endIndex = this.plugin.getLang().getAmountOfArgsRequired(menuField.getDescription());
            embed.addField(menuField.build(Arrays.copyOfRange(args, startIndex, endIndex)));
            startIndex = endIndex;
        }
        return embed.build();
    }

    private List<ActionRow> getActionRows() {
        switch (this.name) {
            case "server":
                this.selectMenu = new ServerSelectMenu(this.plugin);
                break;
            case "voice-channel":
                this.selectMenu = new VoiceChannelSelectMenu(this.plugin);
                break;
            case "range":
                this.selectMenu = new RangeSelectMenu(this.plugin);
                break;
            case "language":
                this.selectMenu = new LanguageSelectMenu(this.plugin);
                break;
            case "login-notification":
                this.selectMenu = new LoginNotificationSelectMenu(this.plugin);
                break;
            case "included-players":
                this.selectMenu = new IncludedPlayersSelectMenu(this.plugin);
                break;
            case "action-bar-alert":
            case "channel-visibility":
                this.selectMenu = new ToggleSelectMenu(this.plugin, this.name);
                break;
            default:
                List<Button> buttons = this.getButtons();
                if (!buttons.isEmpty()) {
                    return Collections.singletonList(ActionRow.of(buttons));
                }
                return Collections.emptyList();
        }
        return Arrays.asList(ActionRow.of(this.selectMenu.get()), ActionRow.of(this.getButtons()));
    }

    private List<Button> getButtons() {
        List<Button> buttons = new ArrayList<>();
        if (this.parent != null && (this.plugin.getBot().getStatus() == BotStatus.READY || "language".equals(this.name))) {
            buttons.add(Button.secondary(this.parent, "← " + this.plugin.getLang().getMessage("discord.button-label.back")));
        }
        if (this.selectMenu != null && this.selectMenu.isRefreshable()) {
            buttons.add(Button.primary(this.name, "⟳ " + this.plugin.getLang().getMessage("discord.button-label.refresh")));
        }
        buttons.addAll(this.getAdditionalButtons());
        if (!"range".equals(this.name)) {
            for (Menu menu : this.plugin.getBot().getMenus().values()) {
                if (menu.parent != null && menu.parent.equals(this.name)) {
                    buttons.add(menu.style == MenuStyle.PRIMARY
                            ? Button.primary(menu.name, menu.getTitle(false))
                            .withEmoji(menu.emoji.get())
                            : Button.secondary(menu.name, menu.getTitle(false))
                            .withEmoji(menu.emoji.get()));
                }
            }
        }
        if (this.type == MenuType.DEFAULT) {
            if (this.plugin.getBot().getStatus() == BotStatus.READY) {
                buttons.add(Button.danger(Menu.CLOSE_BUTTON_ID,
                                this.plugin.getLang().getMessage("discord.button-label.close"))
                        .withEmoji(MenuEmoji.HEAVY_MULTIPLICATION_X.get()));
            } else {
                if (!"language".equals(this.name)) {
                    Menu languageMenu = this.plugin.getBot().getMenu("language");
                    buttons.add(Button.secondary(languageMenu.name, languageMenu.getTitle(false))
                            .withEmoji(MenuEmoji.GLOBE_WITH_MERIDIANS.get()));
                }
                buttons.add(Button.secondary(Menu.CLOSE_BUTTON_ID,
                                this.plugin.getLang().getMessage("discord.button-label.configure-later"))
                        .withEmoji(MenuEmoji.CLOCK3.get()));
            }
        }
        return buttons;
    }

    private List<Button> getAdditionalButtons() {
        List<Button> additionalButtons = new ArrayList<>();
        if ("incomplete-configuration-server-manager".equals(this.name)) {
            additionalButtons.add(Button.primary("resume-configuration",
                            this.plugin.getLang().getMessage("discord.button-label.resume-configuration"))
                    .withEmoji(MenuEmoji.ARROW_FORWARD.get()));
        } else if ("permissions".equals(this.name)) {
            additionalButtons.add(Button.link("https://discord.com/api/oauth2/authorize?client_id="
                            + this.plugin.getBot().getJDA().getSelfUser().getApplicationId()
                            + "&permissions=8&scope=bot%20applications.commands", "Update Permissions")
                    .withEmoji(this.emoji.get()));
        } else if ("range".equals(this.name) && this.plugin.getBot().getStatus() == BotStatus.READY) {
            additionalButtons.add(Button.primary("customize",
                            this.plugin.getLang().getMessage("discord.field.customize.title"))
                    .withEmoji(MenuEmoji.PENCIL2.get()));
        } else if ("login-notification".equals(this.name)
                && LoginNotificationSelectMenu.REMIND_ONCE.equals(this.plugin.getConfigYamlFile().getString(ConfigField.LOGIN_NOTIFICATION.toString()))) {
            additionalButtons.add(Button.danger("clear-notified-players",
                            this.plugin.getLang().getMessage("discord.button-label.clear-notified-players"))
                    .withEmoji(MenuEmoji.WASTEBASKET.get()));
        }
        return additionalButtons;
    }
}
