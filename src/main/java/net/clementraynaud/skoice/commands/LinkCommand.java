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
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LinkCommand extends Command {

    private static final Map<String, String> discordIdCode = new HashMap<>();
    private static final Random random = new Random();

    public LinkCommand(Skoice plugin, CommandExecutor executor, SlashCommandInteractionEvent event) {
        super(plugin, executor, CommandInfo.LINK.isServerManagerRequired(), CommandInfo.LINK.isBotReadyRequired(), event);
    }

    public static Map<String, String> getDiscordIdCode() {
        return LinkCommand.discordIdCode;
    }

    @Override
    public void run() {
        if (super.plugin.getLinksYamlFile().getLinks().containsValue(super.executor.getUser().getId())) {
            super.event.reply(super.plugin.getBot().getMenu("account-already-linked").build()).setEphemeral(true).queue();
            return;
        }

        LinkCommand.discordIdCode.remove(super.executor.getUser().getId());
        String code;
        do {
            int number = LinkCommand.random.nextInt(1000000);
            code = String.format("%06d", number);
        } while (LinkCommand.discordIdCode.containsValue(code));
        LinkCommand.discordIdCode.put(super.executor.getUser().getId(), code);
        super.event.reply(this.plugin.getBot().getMenu("verification-code").build(code)).setEphemeral(true).queue();
    }
}
