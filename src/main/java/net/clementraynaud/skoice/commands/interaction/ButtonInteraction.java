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

import net.clementraynaud.skoice.lang.Logger;
import net.clementraynaud.skoice.lang.Discord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.commands.interaction.Settings.*;
import static net.clementraynaud.skoice.config.Config.TEMP_MESSAGE_ID_FIELD;

public class ButtonInteraction extends ListenerAdapter {

    public static final Map<String, String> discordIDDistance = new HashMap<>();

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        Member member = event.getMember();
        if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
            if (getPlugin().getConfig().contains(TEMP_MESSAGE_ID_FIELD)
                    && getPlugin().getConfig().getString(TEMP_MESSAGE_ID_FIELD).equals(event.getMessageId())
                    && event.getButton() != null) {
                String buttonID = event.getButton().getId();
                switch (buttonID) {
                    case "settings":
                        event.editMessage(new Response().getMessage(event.getGuild())).queue();
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
                        event.editMessage(getPlugin().isBotReady() ? LobbySelection.getLobbySelectionMessage(event.getGuild()) : new Response().getMessage(event.getGuild())).queue();
                        break;
                    case "language":
                        event.editMessage(getPlugin().isBotReady() ? Menu.LANGUAGE.getMessage() : new Response().getMessage(event.getGuild())).queue();
                        break;
                    case "advanced-settings":
                        event.editMessage(getPlugin().isBotReady() ? Menu.ADVANCED_SETTINGS.getMessage() : new Response().getMessage(event.getGuild())).queue();
                        break;
                    case "mode":
                        discordIDDistance.remove(member.getId());
                        event.editMessage(getPlugin().isBotReady() ? ModeSelection.getModeSelectionMessage(false) : new Response().getMessage(event.getGuild())).queue();
                        break;
                    case "horizontal-radius":
                        if (getPlugin().isBotReady()) {
                            discordIDDistance.put(member.getId(), "horizontal");
                            event.editMessage(Menu.HORIZONTAL_RADIUS.getMessage()).queue();
                        } else {
                            event.editMessage(new Response().getMessage(event.getGuild())).queue();
                        }
                        break;
                    case "vertical-radius":
                        if (getPlugin().isBotReady()) {
                            discordIDDistance.put(member.getId(), "vertical");
                            event.editMessage(Menu.VERTICAL_RADIUS.getMessage()).queue();
                        } else {
                            event.editMessage(new Response().getMessage(event.getGuild())).queue();
                        }
                        break;
                    case "action-bar-alert":
                        event.editMessage(getPlugin().isBotReady() ? Menu.ACTION_BAR_ALERT.getMessage() : new Response().getMessage(event.getGuild())).queue();
                        break;
                    case "channel-visibility":
                        event.editMessage(getPlugin().isBotReady() ? Menu.CHANNEL_VISIBILITY.getMessage() : new Response().getMessage(event.getGuild())).queue();
                        break;
                    default:
                        throw new IllegalStateException(Logger.UNEXPECTED_VALUE.toString().replace("{value}", buttonID));
                }
            }
        } else {
            event.replyEmbeds(getAccessDeniedEmbed()).setEphemeral(true).queue();
        }
    }
}
