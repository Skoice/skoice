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
import net.clementraynaud.skoice.menus.ConfigurationMenu;
import net.clementraynaud.skoice.menus.Menu;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;

public class ConfigureCommand extends ListenerAdapter {

    private boolean configureCommandCooldown = false;

    private final Config config;
    private final Lang lang;
    private final Bot bot;
    private final ConfigurationMenu configurationMenu;

    public ConfigureCommand(Config config, Lang lang, Bot bot, ConfigurationMenu configurationMenu) {
        this.config = config;
        this.lang = lang;
        this.bot = bot;
        this.configurationMenu = configurationMenu;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if ("configure".equals(event.getName())) {
            Member member = event.getMember();
            if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
                if (this.configureCommandCooldown) {
                    event.reply(new Menu(this.bot.getMenusYaml().getConfigurationSection("error"),
                            Collections.singleton(this.bot.getFields().get("too-many-interactions").toField(this.lang)))
                            .toMessage(this.config, this.lang, this.bot)).setEphemeral(true).queue();
                } else {
                    this.configurationMenu.deleteMessage();
                    event.reply(this.configurationMenu.getMessage()).queue();
                    this.configureCommandCooldown = true;
                    new Timer().schedule(new TimerTask() {

                        @Override
                        public void run() {
                            ConfigureCommand.this.configureCommandCooldown = false;
                        }
                    }, 5000);
                }
            } else {
                event.reply(new Menu(this.bot.getMenusYaml().getConfigurationSection("error"),
                        Collections.singleton(this.bot.getFields().get("access-denied").toField(this.lang)))
                        .toMessage(this.config, this.lang, this.bot)).setEphemeral(true).queue();
            }
        }
    }
}
