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

import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.lang.Lang;
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

    private final Config config;
    private final Lang lang;
    private final Bot bot;

    public LinkCommand(Config config, Lang lang, Bot bot) {
        this.config = config;
        this.lang = lang;
        this.bot = bot;
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
            if (!this.bot.isReady()) {
                EmbedBuilder embed = new EmbedBuilder().setTitle(MenuEmoji.GEAR + this.lang.getMessage("discord.menu.configuration.title"));
                event.replyEmbeds(embed.addField(MenuEmoji.WARNING + this.lang.getMessage("discord.field.incomplete-configuration.title"),
                                        this.lang.getMessage("discord.field.incomplete-configuration.description"), false)
                                .setColor(Color.RED).build())
                        .setEphemeral(true).queue();
                return;
            }
            EmbedBuilder embed = new EmbedBuilder().setTitle(MenuEmoji.LINK + this.lang.getMessage("discord.menu.linking-process.title"));
            if (this.config.getReader().getLinks().containsValue(event.getUser().getId())) {
                event.replyEmbeds(embed.addField(MenuEmoji.WARNING + this.lang.getMessage("discord.menu.linking-process.field.account-already-linked.title"),
                                        this.lang.getMessage("discord.menu.linking-process.field.account-already-linked.description"), false)
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
            event.replyEmbeds(embed.addField(MenuEmoji.KEY + this.lang.getMessage("discord.menu.linking-process.field.verification-code.title"),
                                    this.lang.getMessage("discord.menu.linking-process.field.verification-code.description", code), false)
                            .setColor(Color.GREEN).build())
                    .setEphemeral(true).queue();
        }
    }
}
