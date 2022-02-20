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

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.util.HashMap;
import java.util.Map;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.bot.Bot.getJda;

public class MessageManagement {

    public static final Map<String, String> discordIDDistance = new HashMap<>();

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
}
