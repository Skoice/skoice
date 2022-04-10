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

package net.clementraynaud.skoice.commands;

import net.clementraynaud.skoice.lang.LangFile;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class InviteCommand extends ListenerAdapter {

    private final LangFile lang;

    public InviteCommand(LangFile lang) {
        this.lang = lang;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if ("invite".equals(event.getName())) {
            EmbedBuilder embed = new EmbedBuilder().setTitle(MenuEmoji.ENVELOPE + this.lang.getMessage("discord.menu.get-the-proximity-voice-chat.title"))
                    .addField(MenuEmoji.INBOX_TRAY + this.lang.getMessage("discord.menu.get-the-proximity-voice-chat.field.download-skoice.title"),
                            this.lang.getMessage("discord.menu.get-the-proximity-voice-chat.field.download-skoice.description"), false)
                    .addField(MenuEmoji.GREEN_HEART + this.lang.getMessage("discord.menu.get-the-proximity-voice-chat.field.donate.title"),
                            this.lang.getMessage("discord.menu.get-the-proximity-voice-chat.field.donate.description"), false)
                    .addField(MenuEmoji.SCREWDRIVER + this.lang.getMessage("discord.field.troubleshooting.title"),
                            this.lang.getMessage("discord.field.troubleshooting.description"), false)
                    .addField(MenuEmoji.HAMMER + this.lang.getMessage("discord.field.contribute.title"),
                            this.lang.getMessage("discord.field.contribute.description"), false)
                    .setColor(Color.ORANGE);
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        }
    }
}
