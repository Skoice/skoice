/*
 * Copyright 2020, 2021 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
 * Copyright 2016, 2017, 2018, 2019, 2020, 2021 Austin "Scarsz" Shapiro
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

package net.clementraynaud.skoice.bot;

import net.clementraynaud.skoice.util.Lang;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static net.clementraynaud.skoice.Skoice.getPlugin;

public class CommandRegistration extends ListenerAdapter {

    public static void registerCommands(Guild guild) {
        try {
            if (guild.retrieveCommands().complete().size() < 4) {
                guild.upsertCommand("configure", "Configure Skoice.").queue();
                guild.upsertCommand("link", "Link your Discord account to Minecraft.").queue();
                guild.upsertCommand("unlink", "Unlink your Discord account from Minecraft.").queue();
                guild.upsertCommand("invite", "Get the proximity voice chat on your server.").queue();
            }
        } catch (ErrorResponseException e) {
            getPlugin().getLogger().severe(Lang.Console.MISSING_ACCESS_ERROR.print());
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        registerCommands(event.getGuild());
    }
}
