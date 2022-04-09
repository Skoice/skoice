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
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang.RandomStringUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class LinkCommand extends ListenerAdapter {

    private static final Map<String, String> discordIDCode = new HashMap<>();

    public static Map<String, String> getDiscordIDCode() {
        return LinkCommand.discordIDCode;
    }

    public static void removeValueFromDiscordIDCode(String value) {
        LinkCommand.discordIDCode.values().remove(value);
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if ("link" .equals(event.getName())) {
            if (!Skoice.getPlugin().isBotReady()) {
                EmbedBuilder embed = new EmbedBuilder().setTitle(MenuEmoji.GEAR + DiscordLang.CONFIGURATION_EMBED_TITLE.toString());
                event.replyEmbeds(embed.addField(MenuEmoji.WARNING + DiscordLang.INCOMPLETE_CONFIGURATION_FIELD_TITLE.toString(),
                                        DiscordLang.INCOMPLETE_CONFIGURATION_FIELD_DESCRIPTION.toString(), false)
                                .setColor(Color.RED).build())
                        .setEphemeral(true).queue();
                return;
            }
            EmbedBuilder embed = new EmbedBuilder().setTitle(MenuEmoji.LINK + DiscordLang.LINKING_PROCESS_EMBED_TITLE.toString());
            if (Config.getLinkMap().containsValue(event.getUser().getId())) {
                event.replyEmbeds(embed.addField(MenuEmoji.WARNING + DiscordLang.ACCOUNT_ALREADY_LINKED_FIELD_TITLE.toString(),
                                        DiscordLang.ACCOUNT_ALREADY_LINKED_FIELD_DESCRIPTION.toString(), false)
                                .setColor(Color.RED).build())
                        .setEphemeral(true).queue();
                return;
            }
            LinkCommand.discordIDCode.remove(event.getUser().getId());
            String code;
            do {
                code = RandomStringUtils.randomAlphanumeric(10).toUpperCase();
            } while (LinkCommand.discordIDCode.containsValue(code));
            LinkCommand.discordIDCode.put(event.getUser().getId(), code);
            event.replyEmbeds(embed.addField(MenuEmoji.KEY + DiscordLang.VERIFICATION_CODE_FIELD_TITLE.toString(),
                                    String.format(DiscordLang.VERIFICATION_CODE_FIELD_DESCRIPTION.toString(), code), false)
                            .setColor(Color.GREEN).build())
                    .setEphemeral(true).queue();
        }
    }
}
