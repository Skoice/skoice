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

package net.clementraynaud.skoice.listeners;

import net.clementraynaud.skoice.bot.Commands;
import net.clementraynaud.skoice.commands.interaction.Response;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static net.clementraynaud.skoice.Skoice.getBot;
import static net.clementraynaud.skoice.Skoice.getPlugin;

public class BotListeners extends ListenerAdapter {

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        new Commands().register(event.getGuild());
        getBot().updateGuildUniquenessStatus();
        getPlugin().updateConfigurationStatus(false);
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        getBot().updateGuildUniquenessStatus();
        getPlugin().updateConfigurationStatus(false);
    }

    @Override
    public void onReconnected(@NotNull ReconnectedEvent event) {
        new Response().deleteMessage();
        getBot().updateGuildUniquenessStatus();
        getBot().checkForValidLobby();
        getBot().checkForUnlinkedUsersInLobby();
        getPlugin().updateConfigurationStatus(false);
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if (!event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
            event.getMessage().replyEmbeds(new EmbedBuilder().setTitle(":warning: " + DiscordLang.ERROR_EMBED_TITLE)
                    .addField(":no_entry: " + DiscordLang.ILLEGAL_INTERACTION_FIELD_TITLE, DiscordLang.ILLEGAL_INTERACTION_FIELD_DESCRIPTION.toString(), false)
                    .setColor(Color.RED).build()).queue();
        }
    }
}
