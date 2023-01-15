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

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.HashMap;
import java.util.Map;

public class LinkCommand extends ListenerAdapter {

    private static final Map<String, String> discordIdCode = new HashMap<>();

    private final Skoice plugin;

    public LinkCommand(Skoice plugin) {
        this.plugin = plugin;
    }

    public static Map<String, String> getDiscordIdCode() {
        return LinkCommand.discordIdCode;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if ("link".equals(event.getName())) {
            if (this.plugin.getBot().getStatus() != BotStatus.READY) {
                event.reply(this.plugin.getBot().getMenu("incomplete-configuration").build())
                        .setEphemeral(true).queue();
                return;
            }
            if (this.plugin.getLinksYamlFile().getLinks().containsValue(event.getUser().getId())) {
                event.reply(this.plugin.getBot().getMenu("account-already-linked").build()).setEphemeral(true).queue();
                return;
            }
            LinkCommand.discordIdCode.remove(event.getUser().getId());
            String code;
            do {
                code = RandomStringUtils.randomNumeric(6);
            } while (LinkCommand.discordIdCode.containsValue(code));
            LinkCommand.discordIdCode.put(event.getUser().getId(), code);
            event.reply(this.plugin.getBot().getMenu("verification-code").build(code)).setEphemeral(true).queue();
        }
    }
}
