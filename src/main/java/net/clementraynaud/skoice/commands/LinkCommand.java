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
                event.reply(new Menu(this.bot.getMenusYaml().getConfigurationSection("configuration"),
                                Collections.singleton(this.bot.getFields().get("incomplete-configuration").toField(this.lang)),
                                MenuType.ERROR)
                                .toMessage(this.config, this.lang, this.bot))
                        .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
                return;
            }
            if (this.config.getLinks().containsValue(event.getUser().getId())) {
                event.reply(new Menu(this.bot.getMenusYaml().getConfigurationSection("linking-process"),
                        Collections.singleton(this.bot.getFields().get("account-already-linked").toField(this.lang)),
                        MenuType.ERROR)
                        .toMessage(this.config, this.lang, this.bot)).setEphemeral(true).queue();
                return;
            }
            LinkCommand.discordIDCode.remove(event.getUser().getId());
            String code;
            do {
                code = RandomStringUtils.randomAlphanumeric(10).toUpperCase();
            } while (LinkCommand.discordIDCode.containsValue(code));
            LinkCommand.discordIDCode.put(event.getUser().getId(), code);
            event.reply(new Menu(this.bot.getMenusYaml().getConfigurationSection("linking-process"),
                    Collections.singleton(new MenuField(this.bot.getFieldsYaml().getConfigurationSection("verification-code")).toField(this.lang, code)),
                    MenuType.SUCCESS)
                    .toMessage(this.config, this.lang, this.bot)).setEphemeral(true).queue();
        }
    }
}
