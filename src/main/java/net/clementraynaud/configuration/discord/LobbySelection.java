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
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.voice.update.VoiceChannelUpdateParentEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.clementraynaud.Skoice.getPlugin;
import static net.clementraynaud.util.DataGetters.getLobby;

public class LobbySelection extends ListenerAdapter {

    @Override
    public void onVoiceChannelDelete(VoiceChannelDeleteEvent event) {
        if (event.getChannel().getId().equals(getPlugin().getConfigFile().getString("lobby-id"))) {
            deleteLobby(event.getGuild());
        }
    }

    @Override
    public void onVoiceChannelUpdateParent(VoiceChannelUpdateParentEvent event) {
        if (event.getChannel().getId().equals(getPlugin().getConfigFile().getString("lobby-id"))) {
            deleteLobby(event.getGuild());
        }
    }

    private static void deleteLobby(Guild guild) {
        getPlugin().getConfigFile().set("lobby-id", null);
        getPlugin().saveConfig();
        getPlugin().updateConfigurationStatus(false);
        guild.retrieveAuditLogs().limit(1).type(ActionType.CHANNEL_DELETE)
                .complete().get(0).getUser().openPrivateChannel().complete()
                .sendMessageEmbeds(new EmbedBuilder().setTitle(":gear: Configuration")
                        .addField(":warning: Incomplete Configuration", "You have either moved or deleted the lobby.\nType `/configure` on your Discord server to complete the configuration and use Skoice.", false)
                        .setColor(Color.RED).build()).queue();
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
            if (voiceChannels.get(optionIndex).getParent() != null){
                options.add(SelectOption.of(voiceChannels.get(optionIndex).getName(), voiceChannels.get(optionIndex).getId())
                        .withDescription(categories.get(optionIndex).getName()).withEmoji(Emoji.fromUnicode("U+1F509")));
            }
            optionIndex++;
        }
        options.add(SelectOption.of("New Voice Channel", "generate")
                .withDescription("Skoice will automatically set up a voice channel.").withEmoji(Emoji.fromUnicode("U+2795")));
        if (options.size() == 23) {
            options.add(SelectOption.of("Too Many Options", "refresh")
                    .withDescription("Skoice is unabled to load more options.").withEmoji(Emoji.fromUnicode("U+26A0")));
        }
        EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: Configuration")
                .setColor(Color.ORANGE)
                .setFooter("The voice channel must be in a category.");
        List<ActionRow> actionRows = new ArrayList<>();
        if (getPlugin().isBotConfigured()) {
            VoiceChannel lobby = getLobby();
            if (lobby != null) {
                embed.addField(":sound: Lobby", "This is the channel players have to join in order to use the proximity voice chat.\nSelected: " + lobby.getName(), false);
            } else {
                embed.addField(":sound: Lobby", "This is the channel players have to join in order to use the proximity voice chat.", false);
            }
            actionRows.add(ActionRow.of(SelectionMenu.create("voice-channels")
                    .addOptions(options)
                    .setDefaultValues(Collections.singleton(getPlugin().getConfigFile().getString("lobby-id"))).build()));
            actionRows.add(ActionRow.of(Button.secondary("settings", "← Back"),
                    Button.primary("lobby", "⟳ Refresh"),
                    Button.danger("close", "Close").withEmoji(Emoji.fromUnicode("U+2716"))));
        } else {
            embed.addField(":sound: Lobby", "This is the channel players have to join in order to use the proximity voice chat.", false);
            actionRows.add(ActionRow.of(SelectionMenu.create("voice-channels")
                    .setPlaceholder("Select a Voice Channel")
                    .addOptions(options).build()));
            actionRows.add(ActionRow.of(Button.primary("settings", "⟳ Refresh"),
                    Button.secondary("close", "Configure Later").withEmoji(Emoji.fromUnicode("U+1F552"))));
        }
        return new MessageBuilder().setEmbeds(embed.build()).setActionRows(actionRows).build();
    }
}
