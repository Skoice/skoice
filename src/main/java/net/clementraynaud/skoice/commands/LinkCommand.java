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

import net.clementraynaud.skoice.lang.DiscordLang;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang.RandomStringUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.config.Config.getLinkMap;

public class LinkCommand extends ListenerAdapter {

    private static final Map<String, String> discordIDCode = new HashMap<>();

    public static Map<String, String> getDiscordIDCode() {
        return discordIDCode;
    }

    public static void removeValueFromDiscordIDCode(String value) {
        discordIDCode.values().remove(value);
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (event.getName().equals("link")) {
            if (!getPlugin().isBotReady()) {
                EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: " + DiscordLang.CONFIGURATION_EMBED_TITLE);
                event.replyEmbeds(embed.addField(":warning: " + DiscordLang.INCOMPLETE_CONFIGURATION_FIELD_TITLE, DiscordLang.INCOMPLETE_CONFIGURATION_FIELD_DESCRIPTION.toString(), false)
                                .setColor(Color.RED).build())
                        .setEphemeral(true).queue();
                return;
            }
            EmbedBuilder embed = new EmbedBuilder().setTitle(":link: " + DiscordLang.LINKING_PROCESS_EMBED_TITLE);
            if (getLinkMap().containsValue(event.getUser().getId())) {
                event.replyEmbeds(embed.addField(":warning: " + DiscordLang.ACCOUNT_ALREADY_LINKED_FIELD_TITLE, DiscordLang.ACCOUNT_ALREADY_LINKED_FIELD_DESCRIPTION.toString(), false)
                                .setColor(Color.RED).build())
                        .setEphemeral(true).queue();
                return;
            }
            discordIDCode.remove(event.getUser().getId());
            String code;
            do {
                code = RandomStringUtils.randomAlphanumeric(10).toUpperCase();
            } while (discordIDCode.containsValue(code));
            discordIDCode.put(event.getUser().getId(), code);
            event.replyEmbeds(embed.addField(":key: " + DiscordLang.VERIFICATION_CODE_FIELD_TITLE,
                                    String.format(DiscordLang.VERIFICATION_CODE_FIELD_DESCRIPTION.toString(), code), false)
                            .setColor(Color.GREEN).build())
                    .setEphemeral(true).queue();
        }
    }
}
