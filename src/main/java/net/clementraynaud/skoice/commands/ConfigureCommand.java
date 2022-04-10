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

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.lang.LangFile;
import net.clementraynaud.skoice.menus.ErrorEmbed;
import net.clementraynaud.skoice.menus.Response;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Timer;
import java.util.TimerTask;

public class ConfigureCommand extends ListenerAdapter {

    private boolean configureCommandCooldown = false;

    private final Skoice plugin;
    private final Config config;
    private final LangFile lang;
    private final Bot bot;

    public ConfigureCommand(Skoice plugin, Config config, LangFile lang, Bot bot) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        this.bot = bot;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if ("configure".equals(event.getName())) {
            Member member = event.getMember();
            if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
                if (this.configureCommandCooldown) {
                    event.replyEmbeds(new ErrorEmbed(this.lang).getTooManyInteractionsEmbed()).setEphemeral(true).queue();
                } else {
                    Response response = new Response(this.plugin, this.config, this.lang, this.bot);
                    response.deleteMessage();
                    event.reply(response.getMessage()).queue();
                    this.configureCommandCooldown = true;
                    new Timer().schedule(new TimerTask() {

                        @Override
                        public void run() {
                            ConfigureCommand.this.configureCommandCooldown = false;
                        }
                    }, 5000);
                }
            } else {
                event.replyEmbeds(new ErrorEmbed(this.lang).getAccessDeniedEmbed()).setEphemeral(true).queue();
            }
        }
    }
}
