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

package net.clementraynaud.skoice.commands.interaction;

import net.clementraynaud.skoice.lang.Discord;
import net.clementraynaud.skoice.lang.Lang;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.bot.Bot.getJda;
import static net.clementraynaud.skoice.config.Config.*;

public enum Menu {
    SETTINGS {
        @Override
        public Message getMessage() {
            EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: " + Discord.CONFIGURATION_EMBED_TITLE)
                    .setColor(Color.ORANGE)
                    .addField(":sound: " + Discord.LOBBY_EMBED_TITLE, Discord.LOBBY_EMBED_DESCRIPTION.toString(), true)
                    .addField(":video_game: " + Discord.MODE_EMBED_TITLE, Discord.MODE_EMBED_DESCRIPTION.toString(), true)
                    .addField(":wrench: " + Discord.ADVANCED_SETTINGS_EMBED_TITLE, Discord.ADVANCED_SETTINGS_EMBED_DESCRIPTION.toString(), true)
                    .addField(":globe_with_meridians: " + Discord.LANGUAGE_EMBED_TITLE, Discord.LANGUAGE_EMBED_DESCRIPTION.toString(), true)
                    .addField(":screwdriver: " + Discord.TROUBLESHOOTING_FIELD_TITLE, Discord.TROUBLESHOOTING_FIELD_DESCRIPTION.toString(), true);
            return new MessageBuilder().setEmbeds(embed.build())
                    .setActionRows(ActionRow.of(Button.primary("lobby", Discord.LOBBY_EMBED_TITLE.toString()).withEmoji(Emoji.fromUnicode("U+1F509")),
                            Button.primary("mode", Discord.MODE_EMBED_TITLE.toString()).withEmoji(Emoji.fromUnicode("U+1F3AE")),
                            Button.secondary("advanced-settings", Discord.ADVANCED_SETTINGS_EMBED_TITLE.toString()).withEmoji(Emoji.fromUnicode("U+1F527")),
                            Button.secondary("language", Discord.LANGUAGE_EMBED_TITLE.toString()).withEmoji(Emoji.fromUnicode("U+1F310")),
                            Button.danger("close", Discord.CLOSE_BUTTON_LABEL.toString()).withEmoji(Emoji.fromUnicode("U+2716")))).build();
        }},

    LANGUAGE {
        @Override
        public Message getMessage() {
            EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: " + Discord.CONFIGURATION_EMBED_TITLE)
                    .setColor(Color.ORANGE)
                    .addField(":globe_with_meridians: " + Discord.LANGUAGE_EMBED_TITLE, Discord.LANGUAGE_EMBED_DESCRIPTION.toString(), false);
            java.util.List<SelectOption> options = new ArrayList<>();
            for (Lang lang : Lang.values()) {
                options.add(SelectOption.of(lang.getFullName(), lang.name())
                        .withDescription(lang.name().equals("EN") ? Discord.DEFAULT_SELECT_OPTION_DESCRIPTION.toString() : null)
                        .withEmoji(lang.getEmoji()));
            }
            List<ActionRow> actionRows = new ArrayList<>();
            if (getPlugin().isBotReady()) {
                actionRows.add(ActionRow.of(SelectionMenu.create("languages")
                        .addOptions(options)
                        .setDefaultValues(Collections.singleton(getLang())).build()));
                actionRows.add(ActionRow.of(Button.secondary("settings", "← " + Discord.BACK_BUTTON_LABEL),
                        Button.danger("close", Discord.CLOSE_BUTTON_LABEL.toString()).withEmoji(Emoji.fromUnicode("U+2716"))));
            } else {
                actionRows.add(ActionRow.of(SelectionMenu.create("languages")
                        .setPlaceholder(Discord.LANGUAGE_SELECT_MENU_PLACEHOLDER.toString())
                        .addOptions(options).build()));
                actionRows.add(ActionRow.of(Button.secondary("close", Discord.CONFIGURE_LATER_BUTTON_LABEL.toString()).withEmoji(Emoji.fromUnicode("U+1F552"))));
            }
            return new MessageBuilder().setEmbeds(embed.build()).setActionRows(actionRows).build();
        }},

    SERVER {
        @Override
        public Message getMessage() {
            List<Guild> servers = new ArrayList<>(getJda().getGuilds());
            List<SelectOption> options = new ArrayList<>();
            int optionIndex = 0;
            while (optionIndex < 24 && servers.size() > optionIndex) {
                options.add(SelectOption.of(servers.get(optionIndex).getName(), servers.get(optionIndex).getId())
                        .withEmoji(Emoji.fromUnicode("U+1F5C4")));
                optionIndex++;
            }
            if (options.size() == 24) {
                options.add(SelectOption.of(Discord.TOO_MANY_OPTIONS_SELECT_OPTION_LABEL.toString(), "refresh")
                        .withDescription(Discord.TOO_MANY_OPTIONS_SELECT_OPTION_DESCRIPTION.toString()).withEmoji(Emoji.fromUnicode("U+26A0")));
            }
            List<ActionRow> actionRows = new ArrayList<>();
            actionRows.add(ActionRow.of(SelectionMenu.create("servers")
                    .setPlaceholder(Discord.SERVER_SELECT_MENU_PLACEHOLDER.toString())
                    .addOptions(options)
                    .build()));
            actionRows.add(ActionRow.of(Button.primary("settings", "⟳ " + Discord.REFRESH_BUTTON_LABEL),
                    Button.secondary("close", Discord.CONFIGURE_LATER_BUTTON_LABEL.toString()).withEmoji(Emoji.fromUnicode("U+1F552"))));
            EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: " + Discord.CONFIGURATION_EMBED_TITLE)
                    .setColor(Color.ORANGE)
                    .addField(":file_cabinet: " + Discord.SERVER_EMBED_TITLE, Discord.SERVER_EMBED_DESCRIPTION.toString(), false);
            return new MessageBuilder().setEmbeds(embed.build()).setActionRows(actionRows).build();
        }},

    HORIZONTAL_RADIUS {
        @Override
        public Message getMessage() {
            EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: " + Discord.CONFIGURATION_EMBED_TITLE)
                    .setColor(Color.ORANGE)
                    .addField(":left_right_arrow: " + Discord.HORIZONTAL_RADIUS_EMBED_TITLE, Discord.HORIZONTAL_RADIUS_EMBED_DESCRIPTION.toString(), false)
                    .addField(":keyboard: " + Discord.ENTER_A_VALUE_FIELD_TITLE, Discord.ENTER_A_VALUE_FIELD_DESCRIPTION.toString().replace("{value}", String.valueOf(getHorizontalRadius())), false);
            return new MessageBuilder().setEmbeds(embed.build())
                    .setActionRows(ActionRow.of(Button.secondary("mode", "← " + Discord.BACK_BUTTON_LABEL),
                            Button.danger("close", Discord.CLOSE_BUTTON_LABEL.toString()).withEmoji(Emoji.fromUnicode("U+2716")))).build();
        }},

    VERTICAL_RADIUS {
        @Override
        public Message getMessage() {
            EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: " + Discord.CONFIGURATION_EMBED_TITLE)
                    .setColor(Color.ORANGE)
                    .addField(":arrow_up_down: " + Discord.VERTICAL_RADIUS_EMBED_TITLE, Discord.VERTICAL_RADIUS_EMBED_DESCRIPTION.toString(), false)
                    .addField(":keyboard: " + Discord.ENTER_A_VALUE_FIELD_TITLE, Discord.ENTER_A_VALUE_FIELD_DESCRIPTION.toString().replace("{value}", String.valueOf(getVerticalRadius())), false);
            return new MessageBuilder().setEmbeds(embed.build())
                    .setActionRows(ActionRow.of(Button.secondary("mode", "← " + Discord.BACK_BUTTON_LABEL),
                            Button.danger("close", Discord.CLOSE_BUTTON_LABEL.toString()).withEmoji(Emoji.fromUnicode("U+2716")))).build();
        }},

    ADVANCED_SETTINGS {
        @Override
        public Message getMessage() {
            EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: " + Discord.CONFIGURATION_EMBED_TITLE)
                    .setColor(Color.ORANGE)
                    .addField(":wrench: " + Discord.ADVANCED_SETTINGS_EMBED_TITLE, Discord.ADVANCED_SETTINGS_EMBED_DESCRIPTION.toString(), false)
                    .addField(":exclamation: " + Discord.ACTION_BAR_ALERT_EMBED_TITLE, Discord.ACTION_BAR_ALERT_EMBED_DESCRIPTION.toString(), true)
                    .addField(":mag: " + Discord.CHANNEL_VISIBILITY_EMBED_TITLE, Discord.CHANNEL_VISIBILITY_EMBED_DESCRIPTION.toString(), true);
            return new MessageBuilder().setEmbeds(embed.build())
                    .setActionRows(ActionRow.of(Button.secondary("settings", "← " + Discord.BACK_BUTTON_LABEL),
                            Button.primary("action-bar-alert", Discord.ACTION_BAR_ALERT_EMBED_TITLE.toString()).withEmoji(Emoji.fromUnicode("U+2757")),
                            Button.primary("channel-visibility", Discord.CHANNEL_VISIBILITY_EMBED_TITLE.toString()).withEmoji(Emoji.fromUnicode("U+1F50D")),
                            Button.danger("close", Discord.CLOSE_BUTTON_LABEL.toString()).withEmoji(Emoji.fromUnicode("U+2716")))).build();
        }},

    ACTION_BAR_ALERT {
        @Override
        public Message getMessage() {
            EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: " + Discord.CONFIGURATION_EMBED_TITLE)
                    .setColor(Color.ORANGE)
                    .addField(":exclamation: " + Discord.ACTION_BAR_ALERT_EMBED_TITLE, Discord.ACTION_BAR_ALERT_EMBED_DESCRIPTION.toString(), false);
            List<ActionRow> actionRows = new ArrayList<>();
            actionRows.add(ActionRow.of(SelectionMenu.create("action-bar-alert")
                    .addOptions(SelectOption.of(Discord.ENABLED_SELECT_OPTION_LABEL.toString(), "true")
                                    .withDescription(Discord.DEFAULT_SELECT_OPTION_DESCRIPTION.toString()).withEmoji(Emoji.fromUnicode("U+2714")),
                            SelectOption.of(Discord.DISABLED_SELECT_OPTION_LABEL.toString(), "false").withEmoji(Emoji.fromUnicode("U+2716")))
                    .setDefaultValues(Collections.singleton(String.valueOf(getActionBarAlert()))).build()));
            actionRows.add(ActionRow.of(Button.secondary("advanced-settings", "← " + Discord.BACK_BUTTON_LABEL),
                    Button.danger("close", Discord.CLOSE_BUTTON_LABEL.toString()).withEmoji(Emoji.fromUnicode("U+2716"))));
            return new MessageBuilder().setEmbeds(embed.build())
                    .setActionRows(actionRows).build();
        }},

    CHANNEL_VISIBILITY {
        @Override
        public Message getMessage() {
            EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: " + Discord.CONFIGURATION_EMBED_TITLE)
                    .setColor(Color.ORANGE)
                    .addField(":mag: " + Discord.CHANNEL_VISIBILITY_EMBED_TITLE, Discord.CHANNEL_VISIBILITY_EMBED_DESCRIPTION.toString(), false);
            List<ActionRow> actionRows = new ArrayList<>();
            actionRows.add(ActionRow.of(SelectionMenu.create("channel-visibility")
                    .addOptions(SelectOption.of(Discord.ENABLED_SELECT_OPTION_LABEL.toString(), "true").withEmoji(Emoji.fromUnicode("U+2714")),
                            SelectOption.of(Discord.DISABLED_SELECT_OPTION_LABEL.toString(), "false")
                                    .withDescription(Discord.DEFAULT_SELECT_OPTION_DESCRIPTION.toString()).withEmoji(Emoji.fromUnicode("U+2716")))
                    .setDefaultValues(Collections.singleton(String.valueOf(getChannelVisibility()))).build()));
            actionRows.add(ActionRow.of(Button.secondary("advanced-settings", "← " + Discord.BACK_BUTTON_LABEL),
                    Button.danger("close", Discord.CLOSE_BUTTON_LABEL.toString()).withEmoji(Emoji.fromUnicode("U+2716"))));
            return new MessageBuilder().setEmbeds(embed.build())
                    .setActionRows(actionRows).build();
        }};

    public abstract Message getMessage();
}
