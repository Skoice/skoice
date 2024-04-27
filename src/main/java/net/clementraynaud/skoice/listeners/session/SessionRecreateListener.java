/*
 * Copyright 2020, 2021, 2022, 2023, 2024 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.listeners.session;

import net.clementraynaud.skoice.Skoice;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SessionRecreateListener extends ListenerAdapter {

    private final Skoice plugin;

    public SessionRecreateListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onSessionRecreate(SessionRecreateEvent event) {
        this.plugin.getConfigurationMenu().delete();
        this.plugin.getConfigYamlFile().removeInvalidVoiceChannelId();
        this.plugin.getBot().getJDA().getGuilds().forEach(guild -> {
            this.plugin.getBotCommands().register(guild);
            if (guild.getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
                guild.getPublicRole().getManager().givePermissions(Permission.USE_APPLICATION_COMMANDS).queue();
            }
        });
        this.plugin.getBot().updateGuild();
        this.plugin.getBot().updateVoiceState();
        this.plugin.getListenerManager().update();
        this.plugin.getBot().muteMembers();
        this.plugin.getBot().checkForUnlinkedUsers();
        this.plugin.getBot().refreshOnlineLinkedPlayers();
    }
}
