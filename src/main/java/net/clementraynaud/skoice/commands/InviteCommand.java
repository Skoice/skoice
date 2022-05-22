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

import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.lang.Lang;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class InviteCommand extends ListenerAdapter {

    private final Config config;
    private final Lang lang;
    private final Bot bot;

    public InviteCommand(Config config, Lang lang, Bot bot) {
        this.config = config;
        this.lang = lang;
        this.bot = bot;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if ("invite".equals(event.getName())) {
            event.reply(this.bot.getMenus().get("get-the-proximity-voice-chat").toMessage(this.config, this.lang, this.bot))
                    .setEphemeral(true).queue();
        }
    }
}
