/*
 * Copyright 2020, 2021, 2022 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.lang.LoggerLang;
import net.clementraynaud.skoice.menus.selectmenus.LanguageSelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.LobbySelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.ModeSelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.SelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.ServerSelectMenu;
import net.clementraynaud.skoice.menus.selectmenus.ToggleSelectMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Menu {
    CONFIGURATION(MenuEmoji.GEAR, null, MenuType.DEFAULT, null),
    SERVER(MenuEmoji.FILE_CABINET, null, MenuType.DEFAULT, null),
    LOBBY(MenuEmoji.SOUND, MenuStyle.PRIMARY, MenuType.DEFAULT, Menu.CONFIGURATION),
    MODE(MenuEmoji.VIDEO_GAME, MenuStyle.PRIMARY, MenuType.DEFAULT, Menu.CONFIGURATION),
    HORIZONTAL_RADIUS(MenuEmoji.LEFT_RIGHT_ARROW, MenuStyle.PRIMARY, MenuType.DEFAULT, Menu.MODE),
    VERTICAL_RADIUS(MenuEmoji.ARROW_UP_DOWN, MenuStyle.PRIMARY, MenuType.DEFAULT, Menu.MODE),
    ADVANCED_SETTINGS(MenuEmoji.WRENCH, MenuStyle.SECONDARY, MenuType.DEFAULT, Menu.CONFIGURATION),
    LANGUAGE(MenuEmoji.GLOBE_WITH_MERIDIANS, MenuStyle.SECONDARY, MenuType.DEFAULT, Menu.CONFIGURATION),
    ACTION_BAR_ALERT(MenuEmoji.EXCLAMATION, MenuStyle.PRIMARY, MenuType.DEFAULT, Menu.ADVANCED_SETTINGS),
    CHANNEL_VISIBILITY(MenuEmoji.MAG, MenuStyle.PRIMARY, MenuType.DEFAULT, Menu.ADVANCED_SETTINGS),
    CHANGELOG(MenuEmoji.NEWSPAPER, MenuStyle.SECONDARY, MenuType.DEFAULT, Menu.ADVANCED_SETTINGS);

    public static final String CLOSE_BUTTON_ID = "CLOSE";

    private final MenuEmoji unicode;
    private final MenuStyle style;
    private final MenuType type;
    private final Menu parent;

    private List<MessageEmbed.Field> fields;
    private SelectMenu selectMenu;

    public static boolean customizeRadius;

    static {
        Menu.CONFIGURATION.fields = Collections.singletonList(MenuField.TROUBLESHOOTING.get());
        Menu.MODE.fields = Arrays.asList(MenuField.VANILLA_MODE.get(), MenuField.MINIGAME_MODE.get(), Skoice.getPlugin().isBotReady() ? MenuField.CUSTOMIZE.get() : null);
        Menu.HORIZONTAL_RADIUS.fields = Collections.singletonList(MenuField.ENTER_A_VALUE.get(Config.getHorizontalRadius()));
        Menu.VERTICAL_RADIUS.fields = Collections.singletonList(MenuField.ENTER_A_VALUE.get(Config.getVerticalRadius()));
        Menu.CHANGELOG.fields = Arrays.asList(MenuField.SKOICE_2_1.get(), MenuField.UPCOMING_FEATURES.get(), MenuField.CONTRIBUTE.get());

        Menu.SERVER.selectMenu = new ServerSelectMenu();
        Menu.LOBBY.selectMenu = new LobbySelectMenu();
        Menu.MODE.selectMenu = new ModeSelectMenu();
        Menu.LANGUAGE.selectMenu = new LanguageSelectMenu();
        Menu.ACTION_BAR_ALERT.selectMenu = new ToggleSelectMenu("ACTION_BAR_ALERT", Config.getActionBarAlert(), true);
        Menu.CHANNEL_VISIBILITY.selectMenu = new ToggleSelectMenu("CHANNEL_VISIBILITY", Config.getChannelVisibility(), false);
    }

    Menu(MenuEmoji unicode, MenuStyle style, MenuType type, Menu parent) {
        this.unicode = unicode;
        this.style = style;
        this.type = type;
        this.parent = parent;
    }

    public Message getMessage() {
        return new MessageBuilder().setEmbeds(this.getEmbed()).setActionRows(this.getActionRows()).build();
    }

    private String getTitle(boolean withEmoji) {
        if (this.isValueSet(this.name() + "_EMBED_TITLE")) {
            return withEmoji
                    ? this.unicode + DiscordLang.valueOf(this.name() + "_EMBED_TITLE").toString()
                    : DiscordLang.valueOf(this.name() + "_EMBED_TITLE").toString();
        }
        return "";
    }

    private String getDescription(boolean shortened) {
        if (!Skoice.getPlugin().isBotReady() && this.isValueSet(this.name() + "_EMBED_ALTERNATIVE_DESCRIPTION")) {
            return DiscordLang.valueOf(this.name() + "_EMBED_ALTERNATIVE_DESCRIPTION").toString();
        } else if (shortened && this.isValueSet(this.name() + "_EMBED_SHORTENED_DESCRIPTION")) {
            return DiscordLang.valueOf(this.name() + "_EMBED_SHORTENED_DESCRIPTION").toString();
        } else if (this.isValueSet(this.name() + "_EMBED_DESCRIPTION")) {
            return DiscordLang.valueOf(this.name() + "_EMBED_DESCRIPTION").toString();
        }
        return null;
    }

    private MessageEmbed getEmbed() {
        EmbedBuilder embed = new EmbedBuilder().setTitle(this.getTitle(true))
                .setColor(this.type.getColor())
                .setFooter(DiscordLang.EMBED_FOOTER.toString(), "https://www.spigotmc.org/data/resource_icons/82/82861.jpg?1597701409");
        if (this.getDescription(false) != null) {
            embed.setDescription(this.getDescription(false));
        }
        if (Skoice.getPlugin().isBotReady()) {
            StringBuilder author = new StringBuilder();
            Menu menuParent = this.parent;
            while (menuParent != null) {
                author.insert(0, menuParent.getTitle(false) + " › ");
                menuParent = menuParent.parent;
            }
            embed.setAuthor(author.toString());
        }
        if (this != Menu.MODE) {
            for (Menu menu : Menu.values()) {
                if (menu.parent == this) {
                    embed.addField(menu.getTitle(true), menu.getDescription(true), true);
                }
            }
        }
        if (this.fields != null) {
            for (MessageEmbed.Field field : this.fields) {
                embed.addField(field);
            }
        }
        return embed.build();
    }

    private List<ActionRow> getActionRows() {
        if (this.selectMenu != null) {
            return Arrays.asList(ActionRow.of(this.selectMenu.get()), ActionRow.of(this.getButtons()));
        }
        return Collections.singletonList(ActionRow.of(this.getButtons()));
    }

    private List<Button> getButtons() {
        List<Button> buttons = new ArrayList<>();
        if (this.parent != null) {
            buttons.add(Button.secondary(this.parent.name(), "← " + DiscordLang.BACK_BUTTON_LABEL));
        }
        if (this.selectMenu != null && this.selectMenu.isRefreshable()) {
            buttons.add(Button.primary(this.name(), "⟳ " + DiscordLang.REFRESH_BUTTON_LABEL));
        }
        if (this == Menu.MODE) {
            buttons.addAll(this.getModeAdditionalButtons());
        } else {
            for (Menu menu : Menu.values()) {
                if (menu.parent == this) {
                    buttons.add(menu.style == MenuStyle.PRIMARY
                            ? Button.primary(menu.name(), menu.getTitle(false))
                            .withEmoji(menu.unicode.getEmojiFromUnicode())
                            : Button.secondary(menu.name(), menu.getTitle(false))
                            .withEmoji(menu.unicode.getEmojiFromUnicode()));
                }
            }
        }
        Menu.customizeRadius = false;
        if (Skoice.getPlugin().isBotReady()) {
            buttons.add(Button.danger(Menu.CLOSE_BUTTON_ID, DiscordLang.CLOSE_BUTTON_LABEL.toString())
                    .withEmoji(MenuEmoji.HEAVY_MULTIPLICATION_X.getEmojiFromUnicode()));
        } else {
            buttons.addAll(Arrays.asList(Button.secondary(Menu.LANGUAGE.name(), DiscordLang.LANGUAGE_EMBED_TITLE.toString())
                            .withEmoji(MenuEmoji.GLOBE_WITH_MERIDIANS.getEmojiFromUnicode()),
                    Button.secondary(Menu.CLOSE_BUTTON_ID, DiscordLang.CONFIGURE_LATER_BUTTON_LABEL.toString())
                            .withEmoji(MenuEmoji.CLOCK3.getEmojiFromUnicode())));
        }
        return buttons;
    }

    private boolean isValueSet(String value) {
        for (DiscordLang message : DiscordLang.values()) {
            if (message.name().equals(value)) {
                return true;
            }
        }
        return false;
    }

    private List<Button> getModeAdditionalButtons() {
        if (this.isModeCustomizable()) {
            return Arrays.asList(Button.primary(Menu.HORIZONTAL_RADIUS.toString(), Menu.HORIZONTAL_RADIUS.getTitle(false))
                            .withEmoji(Menu.HORIZONTAL_RADIUS.unicode.getEmojiFromUnicode()),
                    Button.primary(Menu.VERTICAL_RADIUS.toString(), Menu.VERTICAL_RADIUS.getTitle(false))
                            .withEmoji(Menu.VERTICAL_RADIUS.unicode.getEmojiFromUnicode()));
        }
        return Collections.emptyList();
    }

    private boolean isModeCustomizable() {
        return Skoice.getPlugin().isBotReady() &&
                (Menu.customizeRadius
                        || (Config.getHorizontalRadius() != 80 && Config.getHorizontalRadius() != 40)
                        || (Config.getVerticalRadius() != 40 && Config.getVerticalRadius() != 20));
    }

    public void refreshFields() {
        switch (this) {
            case MODE:
                Menu.MODE.fields = Arrays.asList(MenuField.VANILLA_MODE.get(), MenuField.MINIGAME_MODE.get(),
                        Skoice.getPlugin().isBotReady() ? MenuField.CUSTOMIZE.get() : null);
                break;
            case HORIZONTAL_RADIUS:
                Menu.HORIZONTAL_RADIUS.fields = Collections.singletonList(MenuField.ENTER_A_VALUE.get(Config.getHorizontalRadius()));
                break;
            case VERTICAL_RADIUS:
                Menu.VERTICAL_RADIUS.fields = Collections.singletonList(MenuField.ENTER_A_VALUE.get(Config.getVerticalRadius()));
                break;
            default:
                throw new IllegalStateException(String.format(LoggerLang.UNEXPECTED_VALUE.toString(), this.name()));
        }
    }
}
