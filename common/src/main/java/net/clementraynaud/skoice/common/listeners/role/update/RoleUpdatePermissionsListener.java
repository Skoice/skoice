/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.common.listeners.role.update;

import net.clementraynaud.skoice.common.Skoice;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.role.update.RoleUpdatePermissionsEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RoleUpdatePermissionsListener extends ListenerAdapter {

    private final Skoice plugin;

    public RoleUpdatePermissionsListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onRoleUpdatePermissions(RoleUpdatePermissionsEvent event) {
        this.plugin.getBot().allowApplicationCommands(event.getGuild());

        if (event.getGuild().getSelfMember().getRoles().contains(event.getRole())) {
            boolean lostAdministratorPermission = event.getOldPermissions().contains(Permission.ADMINISTRATOR)
                    && !event.getNewPermissions().contains(Permission.ADMINISTRATOR)
                    && !event.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR);

            boolean gainedAdministratorPermission = !event.getOldPermissions().contains(Permission.ADMINISTRATOR)
                    && event.getNewPermissions().contains(Permission.ADMINISTRATOR)
                    && event.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR);

            if (lostAdministratorPermission || gainedAdministratorPermission) {
                this.plugin.getListenerManager().update();
            }
        }
    }
}