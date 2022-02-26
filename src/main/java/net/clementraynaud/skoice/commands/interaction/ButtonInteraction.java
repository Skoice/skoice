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
import net.clementraynaud.skoice.lang.LoggerLang;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.commands.interaction.ErrorEmbeds.*;
import static net.clementraynaud.skoice.config.Config.TEMP_MESSAGE_ID_FIELD;

public class ButtonInteraction extends ListenerAdapter {

    public static final Map<String, String> discordIDAxis = new HashMap<>();

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        Member member = event.getMember();
        if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
            if (getPlugin().getConfig().contains(TEMP_MESSAGE_ID_FIELD)
                    && getPlugin().getConfig().getString(TEMP_MESSAGE_ID_FIELD).equals(event.getMessageId())
                    && event.getButton() != null) {
                String buttonID = event.getButton().getId();
                switch (buttonID) {
                    case "SETTINGS":
                        event.editMessage(new Response().getMessage()).queue();
                        break;
                    case "CLOSE":
                        event.getMessage().delete().queue();
                        discordIDAxis.remove(event.getUser().getId());
                        getPlugin().getConfig().set("temp", null);
                        getPlugin().saveConfig();
                        if (!getPlugin().isBotReady()) {
                            event.replyEmbeds(new EmbedBuilder()
                                            .setTitle(":gear: " + DiscordLang.CONFIGURATION_EMBED_TITLE)
                                            .addField(":warning: " + DiscordLang.INCOMPLETE_CONFIGURATION_FIELD_TITLE, DiscordLang.INCOMPLETE_CONFIGURATION_SERVER_MANAGER_FIELD_DESCRIPTION.toString(), false)
                                            .setColor(Color.RED).build())
                                    .setEphemeral(true).queue();
                        }
                        break;
                    case "LOBBY":
                        event.editMessage(getPlugin().isBotReady() ? Menu.LOBBY.getMessage() : new Response().getMessage()).queue();
                        break;
                    case "LANGUAGE":
                        event.editMessage(getPlugin().isBotReady() ? Menu.LANGUAGE.getMessage() : new Response().getMessage()).queue();
                        break;
                    case "ADVANCED_SETTINGS":
                        event.editMessage(getPlugin().isBotReady() ? Menu.ADVANCED_SETTINGS.getMessage() : new Response().getMessage()).queue();
                        break;
                    case "MODE":
                        discordIDAxis.remove(member.getId());
                        event.editMessage(getPlugin().isBotReady() ? Menu.MODE.getMessage() : new Response().getMessage()).queue();
                        break;
                    case "HORIZONTAL_RADIUS":
                        if (getPlugin().isBotReady()) {
                            discordIDAxis.put(member.getId(), "horizontal-radius");
                            event.editMessage(Menu.HORIZONTAL_RADIUS.getMessage()).queue();
                        } else {
                            event.editMessage(new Response().getMessage()).queue();
                        }
                        break;
                    case "VERTICAL_RADIUS":
                        if (getPlugin().isBotReady()) {
                            discordIDAxis.put(member.getId(), "vertical-radius");
                            event.editMessage(Menu.VERTICAL_RADIUS.getMessage()).queue();
                        } else {
                            event.editMessage(new Response().getMessage()).queue();
                        }
                        break;
                    case "ACTION_BAR_ALERT":
                        event.editMessage(getPlugin().isBotReady() ? Menu.ACTION_BAR_ALERT.getMessage() : new Response().getMessage()).queue();
                        break;
                    case "CHANNEL_VISIBILITY":
                        event.editMessage(getPlugin().isBotReady() ? Menu.CHANNEL_VISIBILITY.getMessage() : new Response().getMessage()).queue();
                        break;
                    default:
                        throw new IllegalStateException(LoggerLang.UNEXPECTED_VALUE.toString().replace("{value}", buttonID));
                }
            }
        } else {
            event.replyEmbeds(getAccessDeniedEmbed()).setEphemeral(true).queue();
        }
    }
}
