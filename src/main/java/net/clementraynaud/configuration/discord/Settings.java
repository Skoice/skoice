// Copyright 2020, 2021 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
// Copyright 2016, 2017, 2018, 2019, 2020, 2021 Austin "Scarsz" Shapiro

// This file is part of Skoice.

// Skoice is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Skoice is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Skoice.  If not, see <https://www.gnu.org/licenses/>.


package net.clementraynaud.configuration.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;

import static net.clementraynaud.Skoice.getPlugin;
import static net.clementraynaud.configuration.discord.LanguageSelection.getLanguageSelectionMessage;
import static net.clementraynaud.configuration.discord.LobbySelection.getLobbySelectionMessage;
import static net.clementraynaud.configuration.discord.ModeSelection.getModeSelectionMessage;
import static net.clementraynaud.util.DataGetters.*;

public class Settings {

    public static Message getConfigurationMessage(Guild guild) {
        if (getLanguage() == null) {
            return getLanguageSelectionMessage();
        } else if (getPlugin().getConfigFile().getString("lobby-id") == null) {
            return getLobbySelectionMessage(guild);
        } else if (getVerticalRadius() == 0
                || getHorizontalRadius() == 0) {
            return getModeSelectionMessage(false);
        } else {
            return getSettingsMessage();
        }
    }

    private static Message getSettingsMessage() {
        EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: Configuration")
                .setColor(Color.ORANGE)
                .addField(":file_cabinet: Server", "The Discord server where Skoice is active.", true)
                .addField(":sound: Lobby", "The channel players have to join to use the proximity voice chat.", true)
                .addField(":wrench: Advanced Settings", "Customize the distances used by Skoice and manage other parameters.", true)
                .addField(":globe_with_meridians: Language", "The language used to display messages.", true)
                .addField(":screwdriver: Troubleshooting", "Having issues? [Join our Discord server!](https://discord.gg/h3Tgccc)", true);
        return new MessageBuilder().setEmbeds(embed.build())
                .setActionRows(ActionRow.of(Button.primary("server", "Server").withEmoji(Emoji.fromUnicode("U+1F5C4")).asDisabled(),
                        Button.primary("lobby", "Lobby").withEmoji(Emoji.fromUnicode("U+1F509")),
                        Button.secondary("advanced-settings", "Advanced Settings").withEmoji(Emoji.fromUnicode("U+1F527")),
                        Button.secondary("language", "Language").withEmoji(Emoji.fromUnicode("U+1F310")),
                        Button.danger("close", "Close").withEmoji(Emoji.fromUnicode("U+2716")))).build();
    }

    public static Message getAdvancedSettingsMessage() {
        EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: Configuration")
                .setColor(Color.ORANGE)
                .addField(":wrench: Advanced Settings", "Here you can customize the distances used by Skoice and manage other parameters.", false)
                .addField(":video_game: Mode", "Choose a mode or customize the distances.", false);
        return new MessageBuilder().setEmbeds(embed.build())
                .setActionRows(ActionRow.of(Button.secondary("settings", "← Back"),
                        Button.primary("mode", "Mode").withEmoji(Emoji.fromUnicode("U+1F3AE")),
                        Button.danger("close", "Close").withEmoji(Emoji.fromUnicode("U+2716")))).build();
    }

    public static MessageEmbed getAccessDeniedEmbed() {
        return new EmbedBuilder().setTitle(":no_entry: Access Denied")
                .addField(":warning: Error", "You do not have the required permission to execute this action.", false)
                .setColor(Color.RED).build();
    }

    public static MessageEmbed getTooManyInteractionsEmbed() {
        return new EmbedBuilder().setTitle(":no_entry: Too Many Interactions")
                .addField(":warning: Error", "You are sending commands too fast.", false)
                .setColor(Color.RED).build();
    }
}
