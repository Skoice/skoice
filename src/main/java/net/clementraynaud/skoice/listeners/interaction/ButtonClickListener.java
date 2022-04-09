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
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.menus.ErrorEmbeds;
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.clementraynaud.skoice.menus.Response;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ButtonClickListener extends ListenerAdapter {

    public static final Map<String, String> discordIDAxis = new HashMap<>();

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        Member member = event.getMember();
        if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
            if (Skoice.getPlugin().getConfig().contains(Config.TEMP_MESSAGE_ID_FIELD)
                    && Skoice.getPlugin().getConfig().getString(Config.TEMP_MESSAGE_ID_FIELD).equals(event.getMessageId())
                    && event.getButton() != null && event.getButton().getId() != null) {
                String buttonID = event.getButton().getId();
                if (buttonID.equals(Menu.CLOSE_BUTTON_ID)) {
                    event.getMessage().delete().queue();
                    if (!Skoice.getPlugin().isBotReady()) {
                        event.replyEmbeds(new EmbedBuilder()
                                        .setTitle(MenuEmoji.GEAR + DiscordLang.CONFIGURATION_EMBED_TITLE.toString())
                                        .addField(MenuEmoji.WARNING + DiscordLang.INCOMPLETE_CONFIGURATION_FIELD_TITLE.toString(),
                                                DiscordLang.INCOMPLETE_CONFIGURATION_SERVER_MANAGER_FIELD_DESCRIPTION.toString(), false)
                                        .setColor(Color.RED).build())
                                .setEphemeral(true).queue();
                    }
                } else if (!Skoice.getPlugin().isBotReady()) {
                    event.editMessage(new Response().getMessage()).queue();
                } else {
                    if (buttonID.equals(Menu.MODE.name())) {
                        ButtonClickListener.discordIDAxis.remove(member.getId());
                    } else if (buttonID.equals(Menu.HORIZONTAL_RADIUS.name())) {
                        ButtonClickListener.discordIDAxis.put(member.getId(), Config.HORIZONTAL_RADIUS_FIELD);
                    } else if (buttonID.equals(Menu.VERTICAL_RADIUS.name())) {
                        ButtonClickListener.discordIDAxis.put(member.getId(), Config.VERTICAL_RADIUS_FIELD);
                    }
                    event.editMessage(Menu.valueOf(buttonID).getMessage()).queue();
                }
            }
        } else {
            event.replyEmbeds(ErrorEmbeds.getAccessDeniedEmbed()).setEphemeral(true).queue();
        }
    }
}
