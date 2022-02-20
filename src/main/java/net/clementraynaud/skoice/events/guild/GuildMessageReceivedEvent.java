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

package net.clementraynaud.skoice.events.guild;

import net.clementraynaud.skoice.commands.interaction.Response;
import net.clementraynaud.skoice.commands.interaction.Menu;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.commands.interaction.ButtonInteraction.discordIDDistance;
import static net.clementraynaud.skoice.config.Config.*;

public class GuildMessageReceivedEvent extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent event) {
        String discordID = event.getAuthor().getId();
        if (discordID.equals(event.getJDA().getSelfUser().getId())) {
            if (!event.getMessage().isEphemeral()) {
                getPlugin().getConfig().set(TEMP_GUILD_ID_FIELD, event.getGuild().getId());
                getPlugin().getConfig().set(TEMP_TEXT_CHANNEL_ID_FIELD, event.getChannel().getId());
                getPlugin().getConfig().set(TEMP_MESSAGE_ID_FIELD, event.getMessageId());
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
                new Response().deleteMessage();
                if (discordIDDistance.get(event.getAuthor().getId()).equals("horizontal")) {
                    event.getChannel().sendMessage(Menu.HORIZONTAL_RADIUS.getMessage()).queue();
                } else if (discordIDDistance.get(event.getAuthor().getId()).equals("vertical")) {
                    event.getChannel().sendMessage(Menu.VERTICAL_RADIUS.getMessage()).queue();
                }
            }
        }
    }
}
