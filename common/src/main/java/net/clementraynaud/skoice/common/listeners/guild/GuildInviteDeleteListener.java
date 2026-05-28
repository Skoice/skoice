/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025, 2026 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.common.listeners.guild;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.menus.EmbeddedMenu;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogChange;
import net.dv8tion.jda.api.audit.AuditLogKey;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class GuildInviteDeleteListener extends ListenerAdapter {

    private final Skoice plugin;

    public GuildInviteDeleteListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onGuildInviteDelete(GuildInviteDeleteEvent event) {
        String cachedCode = this.plugin.getBot().getGuildInviteCode();
        if (cachedCode == null || !cachedCode.equals(event.getCode())) {
            return;
        }

        this.plugin.getBot().clearGuildInvite();
        this.plugin.getBot().ensureGuildInvite();

        if (!this.plugin.getBot().isAdministrator()) {
            return;
        }

        event.getGuild().retrieveAuditLogs()
                .type(ActionType.INVITE_DELETE)
                .limit(1)
                .queue(auditLogEntries -> {
                    if (auditLogEntries.isEmpty()) {
                        return;
                    }
                    AuditLogChange codeChange = auditLogEntries.get(0).getChangeByKey(AuditLogKey.INVITE_CODE);
                    String targetCode = codeChange != null ? codeChange.getOldValue() : null;
                    if (targetCode != null && !targetCode.equals(event.getCode())) {
                        return;
                    }
                    User user = auditLogEntries.get(0).getUser();
                    if (user != null && !user.isBot()) {
                        new EmbeddedMenu(this.plugin.getBot()).setContent("invite-link-revoked")
                                .message(user);
                    }
                }, new ErrorHandler().ignore(ErrorResponse.MISSING_PERMISSIONS));
    }
}
