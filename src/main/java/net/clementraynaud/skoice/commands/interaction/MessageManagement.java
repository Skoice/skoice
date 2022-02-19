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

import net.clementraynaud.skoice.lang.Console;
import net.clementraynaud.skoice.lang.Discord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.bot.Bot.getJda;
import static net.clementraynaud.skoice.commands.interaction.ActionBarAlertConfiguration.getActionBarAlertConfigurationMessage;
import static net.clementraynaud.skoice.commands.interaction.DistanceConfiguration.getHorizontalRadiusConfigurationMessage;
import static net.clementraynaud.skoice.commands.interaction.DistanceConfiguration.getVerticalRadiusConfigurationMessage;
import static net.clementraynaud.skoice.commands.interaction.LobbySelection.getLobbySelectionMessage;
import static net.clementraynaud.skoice.commands.interaction.Settings.*;

public class MessageManagement extends ListenerAdapter {

    private final Map<String, String> discordIDDistance = new HashMap<>();
    private boolean configureCommandCooldown = false;

    public static void deleteConfigurationMessage() {
        if (getPlugin().getConfig().contains("temp.guild-id")
                && getPlugin().getConfig().contains("temp.text-channel-id")
                && getPlugin().getConfig().contains("temp.message-id")) {
            try {
                Guild guild = getJda().getGuildById(getPlugin().getConfig().getString("temp.guild-id"));
                if (guild != null) {
                    TextChannel textChannel = guild.getTextChannelById(getPlugin().getConfig().getString("temp.text-channel-id"));
                    if (textChannel != null) {
                        textChannel.retrieveMessageById(getPlugin().getConfig().getString("temp.message-id"))
                                .complete().delete().queue(success -> {
                                }, failure -> {
                                });
                    }
                }
            } catch (ErrorResponseException | NullPointerException ignored) {
            }
        }
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String discordID = event.getAuthor().getId();
        if (discordID.equals(event.getJDA().getSelfUser().getId())) {
            if (!event.getMessage().isEphemeral()) {
                getPlugin().getConfig().set("temp.guild-id", event.getGuild().getId());
                getPlugin().getConfig().set("temp.text-channel-id", event.getChannel().getId());
                getPlugin().getConfig().set("temp.message-id", event.getMessageId());
                getPlugin().saveConfig();
            }
        } else if (discordIDDistance.containsKey(event.getAuthor().getId())
                && event.getMessage().getContentRaw().length() <= 4
                && event.getMessage().getContentRaw().matches("[0-9]+")) {
            int value = Integer.parseInt(event.getMessage().getContentRaw());
            if (value >= 1 && value <= 1000) {
                event.getMessage().delete().queue();
                getPlugin().getConfig().set("radius." + discordIDDistance.get(event.getAuthor().getId()), value);
                getPlugin().saveConfig();
                deleteConfigurationMessage();
                if (discordIDDistance.get(event.getAuthor().getId()).equals("horizontal")) {
                    event.getChannel().sendMessage(getHorizontalRadiusConfigurationMessage()).queue();
                } else if (discordIDDistance.get(event.getAuthor().getId()).equals("vertical")) {
                    event.getChannel().sendMessage(getVerticalRadiusConfigurationMessage()).queue();
                }
            }
        }
    }

    @Override
    public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {
        if (getPlugin().getConfig().contains("temp")
                && event.getMessageId().equals(getPlugin().getConfig().getString("temp.message-id"))) {
            getPlugin().getConfig().set("temp", null);
            getPlugin().saveConfig();
            discordIDDistance.clear();
        }
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (event.getName().equals("configure")) {
            Member member = event.getMember();
            if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
                if (configureCommandCooldown) {
                    event.replyEmbeds(getTooManyInteractionsEmbed()).setEphemeral(true).queue();
                } else {
                    if (getPlugin().getConfig().contains("temp")) {
                        deleteConfigurationMessage();
                    }
                    event.reply(getConfigurationMessage(event.getGuild())).queue();
                    configureCommandCooldown = true;
                    new Timer().schedule(new TimerTask() {

                        @Override
                        public void run() {
                            configureCommandCooldown = false;
                        }
                    }, 5000);
                }
            } else {
                event.replyEmbeds(getAccessDeniedEmbed()).setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        Member member = event.getMember();
        if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
            if (getPlugin().getConfig().contains("temp.message-id")
                    && getPlugin().getConfig().getString("temp.message-id").equals(event.getMessageId())
                    && event.getButton() != null) {
                String buttonID = event.getButton().getId();
                switch (buttonID) {
                    case "settings":
                        event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        break;
                    case "close":
                        event.getMessage().delete().queue();
                        discordIDDistance.remove(event.getUser().getId());
                        getPlugin().getConfig().set("temp", null);
                        getPlugin().saveConfig();
                        if (!getPlugin().isBotReady()) {
                            event.replyEmbeds(new EmbedBuilder()
                                            .setTitle(":gear: " + Discord.CONFIGURATION_EMBED_TITLE)
                                            .addField(":warning: " + Discord.INCOMPLETE_CONFIGURATION_FIELD_TITLE, Discord.INCOMPLETE_CONFIGURATION_SERVER_MANAGER_FIELD_DESCRIPTION.toString(), false)
                                            .setColor(Color.RED).build())
                                    .setEphemeral(true).queue();
                        }
                        break;
                    case "lobby":
                        if (getPlugin().isBotReady()) {
                            event.editMessage(getLobbySelectionMessage(event.getGuild())).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    case "language":
                        if (getPlugin().isBotReady()) {
                            event.editMessage(LanguageSelection.getLanguageSelectionMessage()).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    case "advanced-settings":
                        if (getPlugin().isBotReady()) {
                            event.editMessage(getAdvancedSettingsMessage()).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    case "mode":
                        discordIDDistance.remove(member.getId());
                        if (getPlugin().isBotReady()) {
                            event.editMessage(ModeSelection.getModeSelectionMessage(false)).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    case "horizontal-radius":
                        if (getPlugin().isBotReady()) {
                            discordIDDistance.put(member.getId(), "horizontal");
                            event.editMessage(getHorizontalRadiusConfigurationMessage()).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    case "vertical-radius":
                        if (getPlugin().isBotReady()) {
                            discordIDDistance.put(member.getId(), "vertical");
                            event.editMessage(getVerticalRadiusConfigurationMessage()).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    case "action-bar-alert":
                        if (getPlugin().isBotReady()) {
                            event.editMessage(getActionBarAlertConfigurationMessage()).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    case "channel-visibility":
                        if (getPlugin().isBotReady()) {
                            event.editMessage(ChannelVisibilityConfiguration.getChannelVisibilityConfigurationMessage()).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    default:
                        throw new IllegalStateException(Console.UNEXPECTED_VALUE.toString().replace("{value}", buttonID));
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
            if (getPlugin().getConfig().contains("temp.message-id")
                    && getPlugin().getConfig().getString("temp.message-id").equals(event.getMessageId())
                    && event.getSelectedOptions() != null) {
                String componentID = event.getComponentId();
                if (componentID.equals("servers")
                        && getJda().getGuildById(event.getSelectedOptions().get(0).getValue()) != null) {
                    for (SelectOption server : event.getComponent().getOptions()) {
                        if (!event.getGuild().getId().equals(server.getValue())
                                && getJda().getGuilds().contains(getJda().getGuildById(server.getValue()))) {
                            try {
                                getJda().getGuildById(server.getValue()).leave().queue(success -> event.editMessage(getConfigurationMessage(event.getGuild())).queue());
                            } catch (ErrorResponseException ignored) {
                            }
                        }
                    }
                } else if (componentID.equals("languages")) {
                    getPlugin().getConfig().set("lang", event.getSelectedOptions().get(0).getValue());
                    getPlugin().saveConfig();
                    getPlugin().updateConfigurationStatus(false);
                    event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                } else if (componentID.equals("voice-channels")) {
                    Guild guild = event.getGuild();
                    if (guild != null) {
                        if (event.getSelectedOptions().get(0).getValue().equals("generate")) {
                            String categoryID = guild.createCategory(Discord.DEFAULT_CATEGORY_NAME.toString()).complete().getId();
                            String lobbyID = guild.createVoiceChannel(Discord.DEFAULT_LOBBY_NAME.toString(), event.getGuild().getCategoryById(categoryID)).complete().getId();
                            getPlugin().getConfig().set("lobby-id", lobbyID);
                            getPlugin().saveConfig();
                            getPlugin().updateConfigurationStatus(false);
                        } else if (event.getSelectedOptions().get(0).getValue().equals("refresh")) {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        } else {
                            VoiceChannel lobby = guild.getVoiceChannelById(event.getSelectedOptions().get(0).getValue());
                            if (lobby != null && lobby.getParent() != null) {
                                getPlugin().getConfig().set("lobby-id", event.getSelectedOptions().get(0).getValue());
                                getPlugin().saveConfig();
                                getPlugin().updateConfigurationStatus(false);
                            }
                        }
                    }
                    event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                } else if (componentID.equals("modes")) {
                    if (event.getSelectedOptions().get(0).getValue().equals("vanilla-mode")) {
                        getPlugin().getConfig().set("radius.horizontal", 80);
                        getPlugin().getConfig().set("radius.vertical", 40);
                        getPlugin().saveConfig();
                        getPlugin().updateConfigurationStatus(false);
                        event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                    } else if (event.getSelectedOptions().get(0).getValue().equals("minigame-mode")) {
                        getPlugin().getConfig().set("radius.horizontal", 40);
                        getPlugin().getConfig().set("radius.vertical", 20);
                        getPlugin().saveConfig();
                        getPlugin().updateConfigurationStatus(false);
                        event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                    } else if (event.getSelectedOptions().get(0).getValue().equals("customize")) {
                        event.editMessage(ModeSelection.getModeSelectionMessage(true)).queue();
                    }
                } else if (componentID.equals("action-bar-alert")) {
                    if (event.getSelectedOptions().get(0).getValue().equals("true")) {
                        getPlugin().getConfig().set("action-bar-alert", true);
                    } else if (event.getSelectedOptions().get(0).getValue().equals("false")) {
                        getPlugin().getConfig().set("action-bar-alert", false);
                    }
                    getPlugin().saveConfig();
                    event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                } else if (componentID.equals("channel-visibility")) {
                    if (event.getSelectedOptions().get(0).getValue().equals("true")) {
                        getPlugin().getConfig().set("channel-visibility", true);
                    } else if (event.getSelectedOptions().get(0).getValue().equals("false")) {
                        getPlugin().getConfig().set("channel-visibility", false);
                    }
                    getPlugin().saveConfig();
                    event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                }
            }
        } else {
            event.replyEmbeds(getAccessDeniedEmbed()).setEphemeral(true).queue();
        }
    }
}
