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
import net.clementraynaud.skoice.lang.Logger;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.bot.Bot.getJda;
import static net.clementraynaud.skoice.commands.interaction.Settings.getAccessDeniedEmbed;
import static net.clementraynaud.skoice.config.Config.*;

public class SelectMenuInteraction extends ListenerAdapter {

    @Override
    public void onSelectionMenu(SelectionMenuEvent event) {
        Member member = event.getMember();
        if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
            if (getPlugin().getConfig().contains(TEMP_MESSAGE_ID_FIELD)
                    && getPlugin().getConfig().getString(TEMP_MESSAGE_ID_FIELD).equals(event.getMessageId())
                    && event.getSelectedOptions() != null) {
                String componentID = event.getComponentId();
                switch (componentID) {
                    case "servers":
                        if (getJda().getGuildById(event.getSelectedOptions().get(0).getValue()) != null) {
                            for (SelectOption server : event.getComponent().getOptions()) {
                                if (!event.getGuild().getId().equals(server.getValue())
                                        && getJda().getGuilds().contains(getJda().getGuildById(server.getValue()))) {
                                    try {
                                        getJda().getGuildById(server.getValue()).leave().queue(success -> event.editMessage(new Response().getMessage(event.getGuild())).queue());
                                    } catch (ErrorResponseException ignored) {
                                    }
                                }
                            }
                        }
                        break;
                    case "languages":
                        getPlugin().getConfig().set(LANG_FIELD, event.getSelectedOptions().get(0).getValue());
                        getPlugin().saveConfig();
                        getPlugin().updateConfigurationStatus(false);
                        event.editMessage(new Response().getMessage(event.getGuild())).queue();
                        break;
                    case "voice-channels":
                        Guild guild = event.getGuild();
                        if (guild != null) {
                            if (event.getSelectedOptions().get(0).getValue().equals("generate")) {
                                String categoryID = guild.createCategory(Discord.DEFAULT_CATEGORY_NAME.toString()).complete().getId();
                                String lobbyID = guild.createVoiceChannel(Discord.DEFAULT_LOBBY_NAME.toString(), event.getGuild().getCategoryById(categoryID)).complete().getId();
                                getPlugin().getConfig().set(LOBBY_ID_FIELD, lobbyID);
                                getPlugin().saveConfig();
                                getPlugin().updateConfigurationStatus(false);
                            } else if (event.getSelectedOptions().get(0).getValue().equals("refresh")) {
                                event.editMessage(new Response().getMessage(event.getGuild())).queue();
                            } else {
                                VoiceChannel lobby = guild.getVoiceChannelById(event.getSelectedOptions().get(0).getValue());
                                if (lobby != null && lobby.getParent() != null) {
                                    getPlugin().getConfig().set(LOBBY_ID_FIELD, event.getSelectedOptions().get(0).getValue());
                                    getPlugin().saveConfig();
                                    getPlugin().updateConfigurationStatus(false);
                                }
                            }
                        }
                        event.editMessage(new Response().getMessage(event.getGuild())).queue();
                        break;
                    case "modes":
                        if (event.getSelectedOptions().get(0).getValue().equals("vanilla-mode")) {
                            getPlugin().getConfig().set(HORIZONTAL_RADIUS_FIELD, 80);
                            getPlugin().getConfig().set(VERTICAL_RADIUS_FIELD, 40);
                            getPlugin().saveConfig();
                            getPlugin().updateConfigurationStatus(false);
                            event.editMessage(new Response().getMessage(event.getGuild())).queue();
                        } else if (event.getSelectedOptions().get(0).getValue().equals("minigame-mode")) {
                            getPlugin().getConfig().set(HORIZONTAL_RADIUS_FIELD, 40);
                            getPlugin().getConfig().set(VERTICAL_RADIUS_FIELD, 20);
                            getPlugin().saveConfig();
                            getPlugin().updateConfigurationStatus(false);
                            event.editMessage(new Response().getMessage(event.getGuild())).queue();
                        } else if (event.getSelectedOptions().get(0).getValue().equals("customize")) {
                            event.editMessage(ModeSelection.getModeSelectionMessage(true)).queue();
                        }
                        break;
                    case "action-bar-alert":
                        if (event.getSelectedOptions().get(0).getValue().equals("true")) {
                            getPlugin().getConfig().set(ACTION_BAR_ALERT_FIELD, true);
                        } else if (event.getSelectedOptions().get(0).getValue().equals("false")) {
                            getPlugin().getConfig().set(ACTION_BAR_ALERT_FIELD, false);
                        }
                        getPlugin().saveConfig();
                        event.editMessage(new Response().getMessage(event.getGuild())).queue();
                        break;
                    case "channel-visibility":
                        if (event.getSelectedOptions().get(0).getValue().equals("true")) {
                            getPlugin().getConfig().set(CHANNEL_VISIBILITY_FIELD, true);
                        } else if (event.getSelectedOptions().get(0).getValue().equals("false")) {
                            getPlugin().getConfig().set(CHANNEL_VISIBILITY_FIELD, false);
                        }
                        getPlugin().saveConfig();
                        event.editMessage(new Response().getMessage(event.getGuild())).queue();
                        break;
                    default:
                        throw new IllegalStateException(Logger.UNEXPECTED_VALUE.toString().replace("{value}", componentID));
                }
            }
        } else {
            event.replyEmbeds(getAccessDeniedEmbed()).setEphemeral(true).queue();
        }
    }
}
