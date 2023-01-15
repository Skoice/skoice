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

package net.clementraynaud.skoice.listeners.role.update;

import net.clementraynaud.skoice.Skoice;
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
        if (event.getRole().isPublicRole() && !event.getRole().hasPermission(Permission.USE_APPLICATION_COMMANDS)) {
            event.getGuild().getPublicRole().getManager().givePermissions(Permission.USE_APPLICATION_COMMANDS).queue();
        }
        if (event.getGuild().getSelfMember().getRoles().contains(event.getRole())) {
            if (event.getOldPermissions().contains(Permission.ADMINISTRATOR)
                    && !event.getNewPermissions().contains(Permission.ADMINISTRATOR)
                    && !event.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
                this.plugin.getListenerManager().update();
            } else if (!event.getOldPermissions().contains(Permission.ADMINISTRATOR)
                    && event.getNewPermissions().contains(Permission.ADMINISTRATOR)
                    && event.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
                event.getGuild().getPublicRole().getManager().givePermissions(Permission.USE_APPLICATION_COMMANDS).queue();
                this.plugin.getListenerManager().update();
                this.plugin.getConfigurationMenu().retrieveMessage(message ->
                        message.editMessage(this.plugin.getConfigurationMenu().update()).queue());
            }
        }
    }
}