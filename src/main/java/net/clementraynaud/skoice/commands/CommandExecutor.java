/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public class CommandExecutor {

    private final User user;
    private final boolean inGuild;
    private boolean serverManager;

    public CommandExecutor(SlashCommandInteraction interaction) {
        this.user = interaction.getUser();
        this.inGuild = interaction.isFromGuild();
        this.serverManager = interaction.getMember() != null && interaction.getMember().hasPermission(Permission.MANAGE_SERVER);
    }

    public User getUser() {
        return this.user;
    }

    public boolean isInGuild() {
        return this.inGuild;
    }

    public boolean isServerManager() {
        return this.serverManager;
    }

    public void setServerManager(boolean serverManager) {
        this.serverManager = serverManager;
    }
}
