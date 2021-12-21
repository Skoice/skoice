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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static net.clementraynaud.Bot.getJda;
import static net.clementraynaud.Skoice.getPlugin;
import static net.clementraynaud.configuration.discord.DistanceConfiguration.getHorizontalStrengthConfigurationMessage;
import static net.clementraynaud.configuration.discord.DistanceConfiguration.getVerticalStrengthConfigurationMessage;
import static net.clementraynaud.configuration.discord.LobbySelection.getLobbySelectionMessage;
import static net.clementraynaud.configuration.discord.ModeSelection.getModeSelectionMessage;
import static net.clementraynaud.configuration.discord.ServerMigration.getServerMigrationMessage;
import static net.clementraynaud.configuration.discord.Settings.*;
import static net.clementraynaud.util.SaveConfigurationFile.saveConfigurationFile;

public class MessageManagement extends ListenerAdapter {

    private static Map<String, String> discordIDDistanceMap;

    public static void initializeDiscordIDDistanceMap() {
        discordIDDistanceMap = new HashMap<>();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String discordID = event.getAuthor().getId();
        if (discordID.equals(event.getJDA().getSelfUser().getId())) {
            if (!event.getMessage().isEphemeral()) {
                getPlugin().getPlayerData().set("settingsModification.guildID", event.getGuild().getId());
                getPlugin().getPlayerData().set("settingsModification.textChannelID", event.getChannel().getId());
                getPlugin().getPlayerData().set("settingsModification.messageID", event.getMessageId());
                saveConfigurationFile();
            }
        } else if (discordIDDistanceMap.containsKey(event.getAuthor().getId())
                && event.getMessage().getContentRaw().matches("[0-9]+")) {
            int value = Integer.parseInt(event.getMessage().getContentRaw());
            if (value >= 1 && value <= 1000) {
                event.getMessage().delete().queue();
                getPlugin().getPlayerData().set("distance." + discordIDDistanceMap.get(event.getAuthor().getId()), value);
                saveConfigurationFile();
                deleteConfigurationMessage();
                if (discordIDDistanceMap.get(event.getAuthor().getId()).equals("horizontalStrength")) {
                    event.getChannel().sendMessage(getHorizontalStrengthConfigurationMessage()).queue();
                } else if (discordIDDistanceMap.get(event.getAuthor().getId()).equals("verticalStrength")) {
                    event.getChannel().sendMessage(getVerticalStrengthConfigurationMessage()).queue();
                }
            }
        }
    }

    @Override
    public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {
        if (getPlugin().getPlayerData().contains("settingsModification")
                && event.getMessageId().equals(getPlugin().getPlayerData().getString("settingsModification.messageID"))) {
            getPlugin().getPlayerData().set("settingsModification", null);
            saveConfigurationFile();
            discordIDDistanceMap.clear();
        }
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (event.getName().equals("configure")) {
            Member member = event.getMember();
            if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
                if (getPlugin().getPlayerData().contains("settingsModification")) {
                    deleteConfigurationMessage();
                }
                event.reply(getConfigurationMessage(event.getGuild())).queue();
            } else {
                event.replyEmbeds(getAccessDeniedEmbed()).setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        Member member = event.getMember();
        if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
            if (getPlugin().getPlayerData().contains("settingsModification.messageID")
                    && getPlugin().getPlayerData().getString("settingsModification.messageID").equals(event.getMessageId())
                    && event.getButton() != null) {
                String buttonID = event.getButton().getId();
                switch (buttonID) {
                    case "settings":
                        event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        break;
                    case "close":
                        event.getMessage().delete().queue();
                        discordIDDistanceMap.remove(event.getUser().getId());
                        getPlugin().getPlayerData().set("settingsModification", null);
                        saveConfigurationFile();
                        if (!getPlugin().isBotConfigured()) {
                            event.replyEmbeds(new EmbedBuilder()
                                            .setTitle(":gear: Configuration")
                                            .addField(":warning: Incomplete Configuration", "Type `/configure` on your Discord server to complete the configuration and use Skoice.", false)
                                            .setColor(Color.RED).build())
                                    .setEphemeral(true).queue();
                        }
                        break;
                    case "server":
                        if (getPlugin().isBotConfigured()) {
                            event.editMessage(getServerMigrationMessage()).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    case "lobby":
                        if (getPlugin().isBotConfigured()) {
                            event.editMessage(getLobbySelectionMessage(event.getGuild())).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    case "advanced-settings":
                        if (getPlugin().isBotConfigured()) {
                            event.editMessage(getAdvancedSettingsMessage()).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    case "mode":
                        discordIDDistanceMap.remove(member.getId());
                        if (getPlugin().isBotConfigured()) {
                            event.editMessage(getModeSelectionMessage(false)).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    case "horizontal-radius":
                        if (getPlugin().isBotConfigured()) {
                            discordIDDistanceMap.put(member.getId(), "horizontalStrength");
                            event.editMessage(getHorizontalStrengthConfigurationMessage()).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    case "vertical-radius":
                        if (getPlugin().isBotConfigured()) {
                            discordIDDistanceMap.put(member.getId(), "verticalStrength");
                            event.editMessage(getVerticalStrengthConfigurationMessage()).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + buttonID);
                }
            }
        } else {
            event.replyEmbeds(getAccessDeniedEmbed()).setEphemeral(true).queue();
        }
    }

    @Override
    public void onSelectionMenu(SelectionMenuEvent event) {
        Member member = event.getMember();
        if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
            if (getPlugin().getPlayerData().contains("settingsModification.messageID")
                    && getPlugin().getPlayerData().getString("settingsModification.messageID").equals(event.getMessageId())
                    && event.getSelectedOptions() != null) {
                String componentID = event.getComponentId();
                if (componentID.equals("servers")
                        && getJda().getGuildById(event.getSelectedOptions().get(0).getValue()) != null) {
                    getPlugin().getPlayerData().set("serverID", event.getSelectedOptions().get(0).getValue());
                    saveConfigurationFile();
                    event.editMessage(getServerMigrationMessage()).queue();
                } else if (componentID.equals("voice-channels")) {
                    Guild guild = event.getGuild();
                    if (guild != null) {
                        if (event.getSelectedOptions().get(0).getValue().equals("generate")) {
                            String categoryID = guild.createCategory("Proximity Voice Chat").complete().getId();
                            String lobbyID = guild.createVoiceChannel("Lobby", event.getGuild().getCategoryById(categoryID)).complete().getId();
                            getPlugin().getPlayerData().set("lobbyID", lobbyID);
                            saveConfigurationFile();
                            getPlugin().updateConfigurationStatus(false);
                        } else if (event.getSelectedOptions().get(0).getValue().equals("refresh")) {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        } else {
                            VoiceChannel lobby = guild.getVoiceChannelById(event.getSelectedOptions().get(0).getValue());
                            if (lobby != null && lobby.getParent() != null) {
                                getPlugin().getPlayerData().set("lobbyID", event.getSelectedOptions().get(0).getValue());
                                saveConfigurationFile();
                                getPlugin().updateConfigurationStatus(false);
                            }
                        }
                    }
                    event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                } else if (componentID.equals("modes")) {
                    if (event.getSelectedOptions().get(0).getValue().equals("vanilla-mode")) {
                        getPlugin().getPlayerData().set("distance.horizontalStrength", 80);
                        getPlugin().getPlayerData().set("distance.verticalStrength", 40);
                        saveConfigurationFile();
                        getPlugin().updateConfigurationStatus(false);
                        event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                    } else if (event.getSelectedOptions().get(0).getValue().equals("minigame-mode")) {
                        getPlugin().getPlayerData().set("distance.horizontalStrength", 40);
                        getPlugin().getPlayerData().set("distance.verticalStrength", 20);
                        saveConfigurationFile();
                        getPlugin().updateConfigurationStatus(false);
                        event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                    } else if (event.getSelectedOptions().get(0).getValue().equals("customize")) {
                        event.editMessage(getModeSelectionMessage(true)).queue();
                    }
                }
            }
        } else {
            event.replyEmbeds(getAccessDeniedEmbed()).setEphemeral(true).queue();
        }
    }

    public static void deleteConfigurationMessage() {
        if (getPlugin().getPlayerData().contains("settingsModification.guildID")
                && getPlugin().getPlayerData().contains("settingsModification.textChannelID")
                && getPlugin().getPlayerData().contains("settingsModification.messageID")) {
            try {
                Guild guild = getJda().getGuildById(getPlugin().getPlayerData().getString("settingsModification.guildID"));
                if (guild != null) {
                    TextChannel textChannel = guild.getTextChannelById(getPlugin().getPlayerData().getString("settingsModification.textChannelID"));
                    if (textChannel != null) {
                        textChannel.retrieveMessageById(getPlugin().getPlayerData().getString("settingsModification.messageID"))
                                .complete().delete().queue(success -> {
                                }, failure -> {
                                });
                    }
                }
            } catch (ErrorResponseException | NullPointerException e) {
                getPlugin().getLogger().warning("A Discord message could not be deleted.");
            }
        }
    }
}
