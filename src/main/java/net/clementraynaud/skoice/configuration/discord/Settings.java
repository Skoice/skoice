/*
 * Copyright 2020, 2021 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.configuration.discord;

import net.clementraynaud.skoice.lang.Discord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.configuration.discord.LanguageSelection.getLanguageSelectionMessage;
import static net.clementraynaud.skoice.configuration.discord.LobbySelection.getLobbySelectionMessage;
import static net.clementraynaud.skoice.configuration.discord.ServerSelection.getServerSelectionMessage;

public class Settings {

    private Settings() {
    }

    public static Message getConfigurationMessage(Guild guild) {
        if (!getPlugin().getConfigFile().contains("language")) {
            return getLanguageSelectionMessage();
        } else if (!getPlugin().isGuildUnique()) {
            return getServerSelectionMessage();
        } else if (!getPlugin().getConfigFile().contains("lobby-id")) {
            return getLobbySelectionMessage(guild);
        } else if (!getPlugin().getConfigFile().contains("radius.horizontal")
                || !getPlugin().getConfigFile().contains("radius.vertical")) {
            return ModeSelection.getModeSelectionMessage(false);
        } else {
            return getSettingsMessage();
        }
    }

    private static Message getSettingsMessage() {
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
    }

    public static Message getAdvancedSettingsMessage() {
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
    }

    public static MessageEmbed getAccessDeniedEmbed() {
        return new EmbedBuilder().setTitle(":warning: " + Discord.ERROR_EMBED_TITLE)
                .addField(":no_entry: " + Discord.ACCESS_DENIED_FIELD_TITLE, Discord.ACCESS_DENIED_FIELD_DESCRIPTION.toString(), false)
                .setColor(Color.RED).build();
    }

    public static MessageEmbed getTooManyInteractionsEmbed() {
        return new EmbedBuilder().setTitle(":warning: " + Discord.ERROR_EMBED_TITLE)
                .addField(":chart_with_upwards_trend: " + Discord.TOO_MANY_INTERACTIONS_FIELD_TITLE, Discord.TOO_MANY_INTERACTIONS_FIELD_DESCRIPTION.toString(), false)
                .setColor(Color.RED).build();
    }
}
