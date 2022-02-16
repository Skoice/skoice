/*
 * Copyright 2020, 2021 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
 * Copyright 2016, 2017, 2018, 2019, 2020, 2021 Austin "Scarsz" Shapiro
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

import net.clementraynaud.skoice.util.Lang;
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
        EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: " + Lang.Discord.CONFIGURATION_EMBED_TITLE.print())
                .setColor(Color.ORANGE)
                .addField(":sound: " + Lang.Discord.LOBBY_EMBED_TITLE.print(), Lang.Discord.LOBBY_EMBED_DESCRIPTION.print(), true)
                .addField(":video_game: " + Lang.Discord.MODE_EMBED_TITLE.print(), Lang.Discord.MODE_EMBED_DESCRIPTION.print(), true)
                .addField(":wrench: " + Lang.Discord.ADVANCED_SETTINGS_EMBED_TITLE.print(), Lang.Discord.ADVANCED_SETTINGS_EMBED_DESCRIPTION.print(), true)
                .addField(":globe_with_meridians: " + Lang.Discord.LANGUAGE_EMBED_TITLE.print(), Lang.Discord.LANGUAGE_EMBED_DESCRIPTION.print(), true)
                .addField(":screwdriver: " + Lang.Discord.TROUBLESHOOTING_FIELD_TITLE.print(), Lang.Discord.TROUBLESHOOTING_FIELD_DESCRIPTION.print(), true);
        return new MessageBuilder().setEmbeds(embed.build())
                .setActionRows(ActionRow.of(Button.primary("lobby", Lang.Discord.LOBBY_EMBED_TITLE.print()).withEmoji(Emoji.fromUnicode("U+1F509")),
                        Button.primary("mode", Lang.Discord.MODE_EMBED_TITLE.print()).withEmoji(Emoji.fromUnicode("U+1F3AE")),
                        Button.secondary("advanced-settings", Lang.Discord.ADVANCED_SETTINGS_EMBED_TITLE.print()).withEmoji(Emoji.fromUnicode("U+1F527")),
                        Button.secondary("language", Lang.Discord.LANGUAGE_EMBED_TITLE.print()).withEmoji(Emoji.fromUnicode("U+1F310")),
                        Button.danger("close", Lang.Discord.CLOSE_BUTTON_LABEL.print()).withEmoji(Emoji.fromUnicode("U+2716")))).build();
    }

    public static Message getAdvancedSettingsMessage() {
        EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: " + Lang.Discord.CONFIGURATION_EMBED_TITLE.print())
                .setColor(Color.ORANGE)
                .addField(":wrench: " + Lang.Discord.ADVANCED_SETTINGS_EMBED_TITLE.print(), Lang.Discord.ADVANCED_SETTINGS_EMBED_DESCRIPTION.print(), false)
                .addField(":exclamation: " + Lang.Discord.ACTION_BAR_ALERT_EMBED_TITLE.print(), Lang.Discord.ACTION_BAR_ALERT_EMBED_DESCRIPTION.print(), true)
                .addField(":mag: " + Lang.Discord.CHANNEL_VISIBILITY_EMBED_TITLE.print(), Lang.Discord.CHANNEL_VISIBILITY_EMBED_DESCRIPTION.print(), true);
        return new MessageBuilder().setEmbeds(embed.build())
                .setActionRows(ActionRow.of(Button.secondary("settings", "← " + Lang.Discord.BACK_BUTTON_LABEL.print()),
                        Button.primary("action-bar-alert", Lang.Discord.ACTION_BAR_ALERT_EMBED_TITLE.print()).withEmoji(Emoji.fromUnicode("U+2757")),
                        Button.primary("channel-visibility", Lang.Discord.CHANNEL_VISIBILITY_EMBED_TITLE.print()).withEmoji(Emoji.fromUnicode("U+1F50D")),
                        Button.danger("close", Lang.Discord.CLOSE_BUTTON_LABEL.print()).withEmoji(Emoji.fromUnicode("U+2716")))).build();
    }

    public static MessageEmbed getAccessDeniedEmbed() {
        return new EmbedBuilder().setTitle(":warning: " + Lang.Discord.ERROR_EMBED_TITLE.print())
                .addField(":no_entry: " + Lang.Discord.ACCESS_DENIED_FIELD_TITLE.print(), Lang.Discord.ACCESS_DENIED_FIELD_DESCRIPTION.print(), false)
                .setColor(Color.RED).build();
    }

    public static MessageEmbed getTooManyInteractionsEmbed() {
        return new EmbedBuilder().setTitle(":warning: " + Lang.Discord.ERROR_EMBED_TITLE.print())
                .addField(":chart_with_upwards_trend: " + Lang.Discord.TOO_MANY_INTERACTIONS_FIELD_TITLE.print(), Lang.Discord.TOO_MANY_INTERACTIONS_FIELD_DESCRIPTION.print(), false)
                .setColor(Color.RED).build();
    }
}
