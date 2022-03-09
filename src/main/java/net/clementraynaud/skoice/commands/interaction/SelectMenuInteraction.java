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

import net.clementraynaud.skoice.commands.menus.Menu;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.lang.LoggerLang;
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
import static net.clementraynaud.skoice.commands.interaction.ErrorEmbeds.getAccessDeniedEmbed;
import static net.clementraynaud.skoice.commands.menus.Menu.customizeRadius;
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
                    case "SERVER_SELECTION":
                        if (getJda().getGuildById(event.getSelectedOptions().get(0).getValue()) != null) {
                            for (SelectOption server : event.getComponent().getOptions()) {
                                if (!event.getGuild().getId().equals(server.getValue())
                                        && getJda().getGuilds().contains(getJda().getGuildById(server.getValue()))) {
                                    try {
                                        getJda().getGuildById(server.getValue()).leave()
                                                .queue(success -> event.editMessage(new Response().getMessage()).queue());
                                    } catch (ErrorResponseException ignored) {
                                    }
                                }
                            }
                        }
                        break;
                    case "LANGUAGE_SELECTION":
                        getPlugin().getConfig().set(LANG_FIELD, event.getSelectedOptions().get(0).getValue());
                        getPlugin().saveConfig();
                        getPlugin().updateConfigurationStatus(false);
                        event.editMessage(new Response().getMessage()).queue();
                        break;
                    case "LOBBY_SELECTION":
                        Guild guild = event.getGuild();
                        if (guild != null) {
                            if (event.getSelectedOptions().get(0).getValue().equals("GENERATE")) {
                                String categoryID = guild.createCategory(DiscordLang.DEFAULT_CATEGORY_NAME.toString())
                                        .complete().getId();
                                String lobbyID = guild.createVoiceChannel(DiscordLang.DEFAULT_LOBBY_NAME.toString(), event.getGuild().getCategoryById(categoryID))
                                        .complete().getId();
                                getPlugin().getConfig().set(LOBBY_ID_FIELD, lobbyID);
                                getPlugin().saveConfig();
                                getPlugin().updateConfigurationStatus(false);
                            } else if (event.getSelectedOptions().get(0).getValue().equals("REFRESH")) {
                                event.editMessage(new Response().getMessage()).queue();
                            } else {
                                VoiceChannel lobby = guild.getVoiceChannelById(event.getSelectedOptions().get(0).getValue());
                                if (lobby != null && lobby.getParent() != null) {
                                    getPlugin().getConfig().set(LOBBY_ID_FIELD, event.getSelectedOptions().get(0).getValue());
                                    getPlugin().saveConfig();
                                    getPlugin().updateConfigurationStatus(false);
                                }
                            }
                        }
                        event.editMessage(new Response().getMessage()).queue();
                        break;
                    case "MODE_SELECTION":
                        if (event.getSelectedOptions().get(0).getValue().equals("VANILLA_MODE")) {
                            getPlugin().getConfig().set(HORIZONTAL_RADIUS_FIELD, 80);
                            getPlugin().getConfig().set(VERTICAL_RADIUS_FIELD, 40);
                            getPlugin().saveConfig();
                            getPlugin().updateConfigurationStatus(false);
                            event.editMessage(new Response().getMessage()).queue();
                        } else if (event.getSelectedOptions().get(0).getValue().equals("MINIGAME_MODE")) {
                            getPlugin().getConfig().set(HORIZONTAL_RADIUS_FIELD, 40);
                            getPlugin().getConfig().set(VERTICAL_RADIUS_FIELD, 20);
                            getPlugin().saveConfig();
                            getPlugin().updateConfigurationStatus(false);
                            event.editMessage(new Response().getMessage()).queue();
                        } else if (event.getSelectedOptions().get(0).getValue().equals("CUSTOMIZE")) {
                            customizeRadius = true;
                            event.editMessage(Menu.MODE.getMessage()).queue();
                        }
                        break;
                    case "ACTION_BAR_ALERT":
                        if (event.getSelectedOptions().get(0).getValue().equals("true")) {
                            getPlugin().getConfig().set(ACTION_BAR_ALERT_FIELD, true);
                        } else if (event.getSelectedOptions().get(0).getValue().equals("false")) {
                            getPlugin().getConfig().set(ACTION_BAR_ALERT_FIELD, false);
                        }
                        getPlugin().saveConfig();
                        event.editMessage(new Response().getMessage()).queue();
                        break;
                    case "CHANNEL_VISIBILITY":
                        if (event.getSelectedOptions().get(0).getValue().equals("true")) {
                            getPlugin().getConfig().set(CHANNEL_VISIBILITY_FIELD, true);
                        } else if (event.getSelectedOptions().get(0).getValue().equals("false")) {
                            getPlugin().getConfig().set(CHANNEL_VISIBILITY_FIELD, false);
                        }
                        getPlugin().saveConfig();
                        event.editMessage(new Response().getMessage()).queue();
                        break;
                    default:
                        throw new IllegalStateException(LoggerLang.UNEXPECTED_VALUE.toString().replace("{value}", componentID));
                }
            }
        } else {
            event.replyEmbeds(getAccessDeniedEmbed()).setEphemeral(true).queue();
        }
    }
}
