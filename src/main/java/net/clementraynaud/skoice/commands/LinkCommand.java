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
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.menus.MenuField;
import net.clementraynaud.skoice.menus.MenuType;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.apache.commons.lang.RandomStringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LinkCommand extends ListenerAdapter {

    private static final Map<String, String> discordIDCode = new HashMap<>();

    private final Skoice plugin;

    public LinkCommand(Skoice plugin) {
        this.plugin = plugin;
    }

    public static Map<String, String> getDiscordIDCode() {
        return LinkCommand.discordIDCode;
    }

    public static void removeValueFromDiscordIDCode(String value) {
        LinkCommand.discordIDCode.values().remove(value);
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if ("link".equals(event.getName())) {
            if (!this.plugin.getBot().isReady()) {
                event.reply(new Menu(this.plugin, "configuration",
                                Collections.singleton(this.plugin.getBot().getFields().get("incomplete-configuration")),
                                MenuType.ERROR)
                                .toMessage())
                        .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
                return;
            }
            if (this.plugin.readConfig().getLinks().containsValue(event.getUser().getId())) {
                event.reply(new Menu(this.plugin, "linking-process",
                        Collections.singleton(this.plugin.getBot().getFields().get("account-already-linked")),
                        MenuType.ERROR)
                        .toMessage()).setEphemeral(true).queue();
                return;
            }
            LinkCommand.discordIDCode.remove(event.getUser().getId());
            String code;
            do {
                code = RandomStringUtils.randomAlphanumeric(10).toUpperCase();
            } while (LinkCommand.discordIDCode.containsValue(code));
            LinkCommand.discordIDCode.put(event.getUser().getId(), code);
            event.reply(new Menu(this.plugin, "linking-process",
                    Collections.singleton(new MenuField(this.plugin, "verification-code", code)),
                    MenuType.SUCCESS)
                    .toMessage()).setEphemeral(true).queue();
        }
    }
}
