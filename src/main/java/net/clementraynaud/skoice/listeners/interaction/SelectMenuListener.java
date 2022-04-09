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

package net.clementraynaud.skoice.listeners.interaction;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.bot.Commands;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.menus.ErrorEmbeds;
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.lang.LoggerLang;
import net.clementraynaud.skoice.menus.Response;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

public class SelectMenuListener extends ListenerAdapter {

    @Override
    public void onSelectionMenu(SelectionMenuEvent event) {
        Member member = event.getMember();
        if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
            if (Skoice.getPlugin().getConfig().contains(Config.TEMP_MESSAGE_ID_FIELD)
                    && Skoice.getPlugin().getConfig().getString(Config.TEMP_MESSAGE_ID_FIELD).equals(event.getMessageId())
                    && event.getSelectedOptions() != null) {
                String componentID = event.getComponentId();
                switch (componentID) {
                    case "SERVER_SELECTION":
                        if (Bot.getJda().getGuildById(event.getSelectedOptions().get(0).getValue()) != null) {
                            for (SelectOption server : event.getComponent().getOptions()) {
                                if (!event.getGuild().getId().equals(server.getValue())
                                        && Bot.getJda().getGuilds().contains(Bot.getJda().getGuildById(server.getValue()))) {
                                    try {
                                        Bot.getJda().getGuildById(server.getValue()).leave()
                                                .queue(success -> event.editMessage(new Response().getMessage()).queue());
                                    } catch (ErrorResponseException ignored) {
                                    }
                                }
                            }
                        }
                        break;
                    case "LANGUAGE_SELECTION":
                        Skoice.getPlugin().getConfig().set(Config.LANG_FIELD, event.getSelectedOptions().get(0).getValue());
                        Skoice.getPlugin().saveConfig();
                        Skoice.getPlugin().updateConfigurationStatus(false);
                        new Commands().register(event.getGuild());
                        event.editMessage(new Response().getMessage()).queue();
                        break;
                    case "LOBBY_SELECTION":
                        Guild guild = event.getGuild();
                        if (guild != null) {
                            if ("GENERATE" .equals(event.getSelectedOptions().get(0).getValue())) {
                                String categoryID = guild.createCategory(DiscordLang.DEFAULT_CATEGORY_NAME.toString())
                                        .complete().getId();
                                String lobbyID = guild.createVoiceChannel(DiscordLang.DEFAULT_LOBBY_NAME.toString(), event.getGuild().getCategoryById(categoryID))
                                        .complete().getId();
                                Skoice.getPlugin().getConfig().set(Config.LOBBY_ID_FIELD, lobbyID);
                                Skoice.getPlugin().saveConfig();
                                Skoice.getPlugin().updateConfigurationStatus(false);
                            } else if ("REFRESH" .equals(event.getSelectedOptions().get(0).getValue())) {
                                event.editMessage(new Response().getMessage()).queue();
                            } else {
                                VoiceChannel lobby = guild.getVoiceChannelById(event.getSelectedOptions().get(0).getValue());
                                if (lobby != null && lobby.getParent() != null) {
                                    Skoice.getPlugin().getConfig().set(Config.LOBBY_ID_FIELD, event.getSelectedOptions().get(0).getValue());
                                    Skoice.getPlugin().saveConfig();
                                    Skoice.getPlugin().updateConfigurationStatus(false);
                                }
                            }
                        }
                        event.editMessage(new Response().getMessage()).queue();
                        break;
                    case "MODE_SELECTION":
                        if ("VANILLA_MODE" .equals(event.getSelectedOptions().get(0).getValue())) {
                            Skoice.getPlugin().getConfig().set(Config.HORIZONTAL_RADIUS_FIELD, 80);
                            Skoice.getPlugin().getConfig().set(Config.VERTICAL_RADIUS_FIELD, 40);
                            Skoice.getPlugin().saveConfig();
                            Skoice.getPlugin().updateConfigurationStatus(false);
                            event.editMessage(new Response().getMessage()).queue();
                        } else if ("MINIGAME_MODE" .equals(event.getSelectedOptions().get(0).getValue())) {
                            Skoice.getPlugin().getConfig().set(Config.HORIZONTAL_RADIUS_FIELD, 40);
                            Skoice.getPlugin().getConfig().set(Config.VERTICAL_RADIUS_FIELD, 20);
                            Skoice.getPlugin().saveConfig();
                            Skoice.getPlugin().updateConfigurationStatus(false);
                            event.editMessage(new Response().getMessage()).queue();
                        } else if ("CUSTOMIZE" .equals(event.getSelectedOptions().get(0).getValue())) {
                            Menu.customizeRadius = true;
                            event.editMessage(Menu.MODE.getMessage()).queue();
                        }
                        break;
                    case "ACTION_BAR_ALERT":
                        if ("true" .equals(event.getSelectedOptions().get(0).getValue())) {
                            Skoice.getPlugin().getConfig().set(Config.ACTION_BAR_ALERT_FIELD, true);
                        } else if ("false" .equals(event.getSelectedOptions().get(0).getValue())) {
                            Skoice.getPlugin().getConfig().set(Config.ACTION_BAR_ALERT_FIELD, false);
                        }
                        Skoice.getPlugin().saveConfig();
                        event.editMessage(new Response().getMessage()).queue();
                        break;
                    case "CHANNEL_VISIBILITY":
                        if ("true" .equals(event.getSelectedOptions().get(0).getValue())) {
                            Skoice.getPlugin().getConfig().set(Config.CHANNEL_VISIBILITY_FIELD, true);
                        } else if ("false" .equals(event.getSelectedOptions().get(0).getValue())) {
                            Skoice.getPlugin().getConfig().set(Config.CHANNEL_VISIBILITY_FIELD, false);
                        }
                        Skoice.getPlugin().saveConfig();
                        event.editMessage(new Response().getMessage()).queue();
                        break;
                    default:
                        throw new IllegalStateException(String.format(LoggerLang.UNEXPECTED_VALUE.toString(), componentID));
                }
            }
        } else {
            event.replyEmbeds(ErrorEmbeds.getAccessDeniedEmbed()).setEphemeral(true).queue();
        }
    }
}
