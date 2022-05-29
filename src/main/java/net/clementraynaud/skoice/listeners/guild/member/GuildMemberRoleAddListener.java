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

package net.clementraynaud.skoice.listeners.guild.member;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.tasks.InterruptSystemTask;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildMemberRoleAddListener extends ListenerAdapter {

    private final Skoice plugin;

    public GuildMemberRoleAddListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        if (event.getMember().equals(event.getGuild().getSelfMember())
                && event.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
            this.plugin.updateStatus(false, event.getUser());
            this.plugin.getConfigurationMenu().retrieveMessage().editMessage(this.plugin.getConfigurationMenu().getMessage()).queue();
        }
    }
}