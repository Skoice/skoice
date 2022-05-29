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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Timer;
import java.util.TimerTask;

public class ConfigureCommand extends ListenerAdapter {

    private boolean configureCommandCooldown = false;

    private final Skoice plugin;

    public ConfigureCommand(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if ("configure".equals(event.getName())) {
            Member member = event.getMember();
            if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
                if (this.configureCommandCooldown) {
                    event.reply(this.plugin.getBot().getMenu("too-many-interactions").toMessage())
                            .setEphemeral(true).queue();
                } else {
                    this.plugin.getConfigurationMenu().deleteMessage();
                    event.reply(this.plugin.getConfigurationMenu().getMessage()).queue();
                    this.configureCommandCooldown = true;
                    new Timer().schedule(new TimerTask() {

                        @Override
                        public void run() {
                            ConfigureCommand.this.configureCommandCooldown = false;
                        }
                    }, 5000);
                }
            } else {
                event.reply(this.plugin.getBot().getMenu("access-denied").toMessage())
                        .setEphemeral(true).queue();
            }
        }
    }
}
