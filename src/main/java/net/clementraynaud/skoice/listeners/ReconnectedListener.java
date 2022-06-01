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

import net.clementraynaud.skoice.Skoice;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ReconnectedListener extends ListenerAdapter {

    private final Skoice plugin;

    public ReconnectedListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onReconnected(@NotNull ReconnectedEvent event) {
        this.plugin.getConfigurationMenu().delete();
        this.plugin.getBot().updateGuildUniquenessStatus();
        this.plugin.getBot().checkForValidLobby();
        this.plugin.getBot().checkForUnlinkedUsersInLobby();
        this.plugin.getBot().updateVoiceState();
        this.plugin.updateStatus(false);
    }
}
