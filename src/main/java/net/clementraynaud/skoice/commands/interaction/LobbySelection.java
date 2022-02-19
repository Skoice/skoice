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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdateParentEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.clementraynaud.skoice.Skoice.getPlugin;

public class LobbySelection extends ListenerAdapter {

    private static void deleteLobby(Guild guild) {
        getPlugin().getConfig().set("lobby-id", null);
        getPlugin().saveConfig();
        getPlugin().updateConfigurationStatus(false);
        User user = guild.retrieveAuditLogs().limit(1).type(ActionType.CHANNEL_DELETE).complete().get(0).getUser();
        if (user != null && !user.isBot()) {
            try {
                user.openPrivateChannel().complete()
                        .sendMessageEmbeds(new EmbedBuilder().setTitle(":gear: " + Discord.CONFIGURATION_EMBED_TITLE)
                                .addField(":warning: " + Discord.INCOMPLETE_CONFIGURATION_FIELD_TITLE, Discord.INCOMPLETE_CONFIGURATION_SERVER_MANAGER_FIELD_ALTERNATIVE_DESCRIPTION.toString(), false)
                                .setColor(Color.RED).build()).queue(success -> {
                        }, failure -> {
                        });
            } catch (ErrorResponseException ignored) {
            }
        }
    }

    public static Message getLobbySelectionMessage(Guild guild) {
        List<VoiceChannel> voiceChannels = new ArrayList<>(guild.getVoiceChannels());
        List<Category> categories = new ArrayList<>();
        for (VoiceChannel voiceChannel : voiceChannels) {
            categories.add(voiceChannel.getParent());
        }
        List<SelectOption> options = new ArrayList<>();
        int optionIndex = 0;
        while (optionIndex < 23 && voiceChannels.size() > optionIndex) {
            if (voiceChannels.get(optionIndex).getParent() != null) {
                options.add(SelectOption.of(voiceChannels.get(optionIndex).getName(), voiceChannels.get(optionIndex).getId())
                        .withDescription(categories.get(optionIndex).getName()).withEmoji(Emoji.fromUnicode("U+1F509")));
            }
            optionIndex++;
        }
        options.add(SelectOption.of(Discord.NEW_VOICE_CHANNEL_SELECT_OPTION_LABEL.toString(), "generate")
                .withDescription(Discord.NEW_VOICE_CHANNEL_SELECT_OPTION_DESCRIPTION.toString()).withEmoji(Emoji.fromUnicode("U+2795")));
        if (options.size() == 23) {
            options.add(SelectOption.of(Discord.TOO_MANY_OPTIONS_SELECT_OPTION_LABEL.toString(), "refresh")
                    .withDescription(Discord.TOO_MANY_OPTIONS_SELECT_OPTION_DESCRIPTION.toString()).withEmoji(Emoji.fromUnicode("U+26A0")));
        }
        EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: " + Discord.CONFIGURATION_EMBED_TITLE)
                .setColor(Color.ORANGE)
                .addField(":sound: " + Discord.LOBBY_EMBED_TITLE, Discord.LOBBY_EMBED_ALTERNATIVE_DESCRIPTION.toString(), false);
        List<ActionRow> actionRows = new ArrayList<>();
        if (getPlugin().isBotReady()) {
            actionRows.add(ActionRow.of(SelectionMenu.create("voice-channels")
                    .addOptions(options)
                    .setDefaultValues(Collections.singleton(getPlugin().getConfig().getString("lobby-id"))).build()));
            actionRows.add(ActionRow.of(Button.secondary("settings", "← " + Discord.BACK_BUTTON_LABEL),
                    Button.primary("lobby", "⟳ " + Discord.REFRESH_BUTTON_LABEL),
                    Button.danger("close", Discord.CLOSE_BUTTON_LABEL.toString()).withEmoji(Emoji.fromUnicode("U+2716"))));
        } else {
            actionRows.add(ActionRow.of(SelectionMenu.create("voice-channels")
                    .setPlaceholder(Discord.LOBBY_SELECT_MENU_PLACEHOLDER.toString())
                    .addOptions(options).build()));
            actionRows.add(ActionRow.of(Button.primary("settings", "⟳ " + Discord.REFRESH_BUTTON_LABEL),
                    Button.secondary("close", Discord.CONFIGURE_LATER_BUTTON_LABEL.toString()).withEmoji(Emoji.fromUnicode("U+1F552"))));
        }
        return new MessageBuilder().setEmbeds(embed.build()).setActionRows(actionRows).build();
    }

    @Override
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
        if (event.getChannel().getId().equals(getPlugin().getConfig().getString("lobby-id"))) {
            deleteLobby(event.getGuild());
        }
    }

    @Override
    public void onVoiceChannelUpdateParent(VoiceChannelUpdateParentEvent event) {
        if (event.getChannel().getId().equals(getPlugin().getConfig().getString("lobby-id"))) {
            deleteLobby(event.getGuild());
        }
    }
}
