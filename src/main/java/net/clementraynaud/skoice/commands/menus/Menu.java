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

package net.clementraynaud.skoice.commands.menus;

import net.clementraynaud.skoice.commands.menus.components.*;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.lang.LoggerLang;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.util.*;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.commands.menus.MenuStyle.*;
import static net.clementraynaud.skoice.commands.menus.MenuType.*;
import static net.clementraynaud.skoice.commands.menus.MenuEmoji.*;
import static net.clementraynaud.skoice.config.Config.*;
import static net.clementraynaud.skoice.lang.DiscordLang.*;
import static net.dv8tion.jda.api.entities.MessageEmbed.*;

public enum Menu {
    SETTINGS(null, null, DEFAULT, null, false, false),
    SERVER(FILE_CABINET, null, DEFAULT, null, true, true),
    LOBBY(SOUND, PRIMARY, DEFAULT, SETTINGS, true, true),
    MODE(VIDEO_GAME, PRIMARY, DEFAULT, SETTINGS, true, false),
    HORIZONTAL_RADIUS(LEFT_RIGHT_ARROW, PRIMARY, DEFAULT, MODE, false, false),
    VERTICAL_RADIUS(UP_DOWN_ARROW, PRIMARY, DEFAULT, MODE, false, false),
    ADVANCED_SETTINGS(WRENCH, SECONDARY, DEFAULT, SETTINGS, false, false),
    LANGUAGE(GLOBE_WITH_MERIDIANS, SECONDARY, DEFAULT, SETTINGS, true, false),
    ACTION_BAR_ALERT(EXCLAMATION, PRIMARY, DEFAULT, ADVANCED_SETTINGS, true, false),
    CHANNEL_VISIBILITY(MAG, PRIMARY, DEFAULT, ADVANCED_SETTINGS, true, false);

    private static final String CLOSE_BUTTON_ID = "CLOSE";

    private final MenuEmoji unicode;
    private final MenuStyle style;
    private final MenuType type;
    private final Menu parent;
    private final boolean hasSelectMenu;
    private final boolean isRefreshable;

    private List<Menu> children;
    private List<Field> additionalFields;

    public static boolean customizeRadius;

    static {
        SETTINGS.children = Arrays.asList(LOBBY, MODE, ADVANCED_SETTINGS, LANGUAGE);
        ADVANCED_SETTINGS.children = Arrays.asList(ACTION_BAR_ALERT, CHANNEL_VISIBILITY);

        MODE.additionalFields = Arrays.asList(
                new Field(MAP + " " + VANILLA_MODE_FIELD_TITLE, VANILLA_MODE_FIELD_DESCRIPTION.toString(), true),
                new Field(CROSSED_SWORDS + " " + MINIGAME_MODE_FIELD_TITLE, MINIGAME_MODE_FIELD_DESCRIPTION.toString(), true),
                getPlugin().isBotReady() ? new Field(PENCIL2 + " " + CUSTOMIZE_FIELD_TITLE, CUSTOMIZE_FIELD_DESCRIPTION.toString(), true) : null);
        HORIZONTAL_RADIUS.additionalFields = Collections.singletonList(
                new Field(KEYBOARD + " " + ENTER_A_VALUE_FIELD_TITLE, String.format(ENTER_A_VALUE_FIELD_DESCRIPTION.toString(),
                        getHorizontalRadius()), false));
        VERTICAL_RADIUS.additionalFields = Collections.singletonList(
                new Field(KEYBOARD + " " + ENTER_A_VALUE_FIELD_TITLE, String.format(ENTER_A_VALUE_FIELD_DESCRIPTION.toString(),
                                getVerticalRadius()), false));
    }

    Menu(MenuEmoji unicode, MenuStyle style, MenuType type, Menu parent, boolean hasSelectMenu, boolean isRefreshable) {
        this.unicode = unicode;
        this.style = style;
        this.type = type;
        this.parent = parent;
        this.hasSelectMenu = hasSelectMenu;
        this.isRefreshable = isRefreshable;
    }

    public Message getMessage() {
        return new MessageBuilder().setEmbeds(getEmbed()).setActionRows(getActionRows()).build();
    }

    private String getTitle(boolean withEmoji) {
        if (isValueSet(this.name() + "_EMBED_TITLE"))
            return withEmoji ?
                    unicode + " " + DiscordLang.valueOf(this.name() + "_EMBED_TITLE") :
                    DiscordLang.valueOf(this.name() + "_EMBED_TITLE").toString();
        return null;
    }

    private String getDescription(boolean shortened) {
        if (!getPlugin().isBotReady() && isValueSet(this.name() + "_EMBED_ALTERNATIVE_DESCRIPTION")) {
            return DiscordLang.valueOf(this.name() + "_EMBED_ALTERNATIVE_DESCRIPTION").toString();
        } else if (shortened && isValueSet(this.name() + "_EMBED_SHORTENED_DESCRIPTION")) {
            return DiscordLang.valueOf(this.name() + "_EMBED_SHORTENED_DESCRIPTION").toString();
        }
        return DiscordLang.valueOf(this.name() + "_EMBED_DESCRIPTION").toString();
    }

    private MessageEmbed getEmbed() {
        EmbedBuilder embed = new EmbedBuilder().setTitle(GEAR + " " + CONFIGURATION_EMBED_TITLE)
                .setColor(type.getColor());
        if (this.getTitle(false) != null)
            embed.addField(this.getTitle(true), this.getDescription(false), false);
        if (children != null)
            for (Menu child : children)
                embed.addField(child.getTitle(true), child.getDescription(true), true);
        else if (additionalFields != null)
            for (Field additionalField : additionalFields)
                embed.addField(additionalField);
        return embed.build();
    }

    private List<ActionRow> getActionRows() {
        if (hasSelectMenu) {
            return Arrays.asList(ActionRow.of(getSelectMenu(this)), ActionRow.of(getButtons()));
        }
        return Collections.singletonList(ActionRow.of(getButtons()));
    }

    private List<Button> getButtons() {
        List<Button> buttons = new ArrayList<>();
        if (parent != null)
            buttons.add(Button.secondary(parent.name(), "← " + DiscordLang.BACK_BUTTON_LABEL));
        if (isRefreshable)
            buttons.add(Button.primary(this.name(), "⟳ " + DiscordLang.REFRESH_BUTTON_LABEL));
        if (children != null)
            for (Menu child : children)
                buttons.add(child.style.equals(PRIMARY) ?
                        Button.primary(child.name(), child.getTitle(false)).withEmoji(child.unicode.getEmojifromUnicode()) :
                        Button.secondary(child.name(), child.getTitle(false)).withEmoji(child.unicode.getEmojifromUnicode()));
        else if (this == MODE)
            buttons.addAll(getModeAdditionalButtons());
        customizeRadius = false;
        if (getPlugin().isBotReady()) {
            buttons.add(Button.danger(CLOSE_BUTTON_ID, CLOSE_BUTTON_LABEL.toString()).withEmoji(HEAVY_MULTIPLICATION_X.getEmojifromUnicode()));
        } else {
            buttons.addAll(Arrays.asList(Button.secondary(LANGUAGE.name(), LANGUAGE_EMBED_TITLE.toString()).withEmoji(GLOBE_WITH_MERIDIANS.getEmojifromUnicode()),
                    Button.secondary(CLOSE_BUTTON_ID, CONFIGURE_LATER_BUTTON_LABEL.toString()).withEmoji(CLOCK3.getEmojifromUnicode())));
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
        if (getPlugin().isBotReady() &&
                (customizeRadius
                        || (getHorizontalRadius() != 80 && getHorizontalRadius() != 40)
                        || (getVerticalRadius() != 40 && getVerticalRadius() != 20)))
            return Arrays.asList(Button.primary(HORIZONTAL_RADIUS.toString(), HORIZONTAL_RADIUS.getTitle(false))
                            .withEmoji(HORIZONTAL_RADIUS.unicode.getEmojifromUnicode()),
                    Button.primary(VERTICAL_RADIUS.toString(), VERTICAL_RADIUS.getTitle(false))
                            .withEmoji(VERTICAL_RADIUS.unicode.getEmojifromUnicode()));
        return Collections.emptyList();
    }

    public void refreshAdditionalFields() {
        switch (this) {
            case MODE:
                MODE.additionalFields = Arrays.asList(
                        new Field(MAP + " " + VANILLA_MODE_FIELD_TITLE, VANILLA_MODE_FIELD_DESCRIPTION.toString(), true),
                        new Field(CROSSED_SWORDS + " " + MINIGAME_MODE_FIELD_TITLE, MINIGAME_MODE_FIELD_DESCRIPTION.toString(), true),
                        getPlugin().isBotReady() ? new Field(PENCIL2 + " " + CUSTOMIZE_FIELD_TITLE, CUSTOMIZE_FIELD_DESCRIPTION.toString(), true) : null);
                break;
            case HORIZONTAL_RADIUS:
                HORIZONTAL_RADIUS.additionalFields = Collections.singletonList(
                        new Field(KEYBOARD + " " + ENTER_A_VALUE_FIELD_TITLE, String.format(ENTER_A_VALUE_FIELD_DESCRIPTION.toString(),
                                        getHorizontalRadius()), false));
                break;
            case VERTICAL_RADIUS:
                VERTICAL_RADIUS.additionalFields = Collections.singletonList(
                        new Field(KEYBOARD + " " + ENTER_A_VALUE_FIELD_TITLE, String.format(ENTER_A_VALUE_FIELD_DESCRIPTION.toString(),
                                        getVerticalRadius()), false));
                break;
            default:
                throw new IllegalStateException(String.format(LoggerLang.UNEXPECTED_VALUE.toString(), this.name()));
        }
    }

    private SelectionMenu getSelectMenu(Menu menu) {
        switch (menu) {
            case SERVER:
                return new ServerSelectMenu().getComponent();
            case LOBBY:
                return new LobbySelectMenu().getComponent();
            case MODE:
                return new ModeSelectMenu().getComponent();
            case LANGUAGE:
                return new LanguageSelectMenu().getComponent();
            case ACTION_BAR_ALERT:
                return new ToggleSelectMenu(ACTION_BAR_ALERT.name(), getActionBarAlert(), true).getComponent();
            case CHANNEL_VISIBILITY:
                return new ToggleSelectMenu(CHANNEL_VISIBILITY.name(), getChannelVisibility(), true).getComponent();
            default:
                throw new IllegalStateException(String.format(LoggerLang.UNEXPECTED_VALUE.toString(), menu));
        }
    }
}
