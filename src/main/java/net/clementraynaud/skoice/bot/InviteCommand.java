/*
 * Copyright 2020, 2021 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
 * Copyright 2016, 2017, 2018, 2019, 2020, 2021 Austin "Scarsz" Shapiro
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

package net.clementraynaud.skoice.bot;

import net.clementraynaud.skoice.lang.Discord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class InviteCommand extends ListenerAdapter {

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (event.getName().equals("invite")) {
            EmbedBuilder embed = new EmbedBuilder().setTitle(":envelope: " + Discord.GET_THE_PROXIMITY_VOICE_CHAT_EMBED_TITLE)
                    .addField(":inbox_tray: " + Discord.DOWNLOAD_SKOICE_FIELD_TITLE, Discord.DOWNLOAD_SKOICE_FIELD_DESRIPTION.toString(), false)
                    .addField(":screwdriver: " + Discord.TROUBLESHOOTING_FIELD_TITLE, Discord.TROUBLESHOOTING_FIELD_DESCRIPTION.toString(), false)
                    .setColor(Color.ORANGE);
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        }
    }
}
