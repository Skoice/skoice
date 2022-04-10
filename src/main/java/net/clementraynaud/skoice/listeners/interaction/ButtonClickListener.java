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

package net.clementraynaud.skoice.listeners.interaction;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.lang.LangFile;
import net.clementraynaud.skoice.menus.ErrorEmbed;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.menus.Response;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ButtonClickListener extends ListenerAdapter {

    private final Skoice plugin;
    private final Config config;
    private final LangFile lang;
    private final Bot bot;

    public ButtonClickListener(Skoice plugin, Config config, LangFile lang, Bot bot) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        this.bot = bot;
    }

    public static final Map<String, String> discordIDAxis = new HashMap<>();

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        Member member = event.getMember();
        if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
            if (this.config.getFile().contains(ConfigField.TEMP_MESSAGE.get())
                    && this.config.getFile().get(ConfigField.TEMP_MESSAGE.get()).equals(event.getMessage())
                    && event.getButton() != null && event.getButton().getId() != null) {
                String buttonID = event.getButton().getId();
                if (buttonID.equals(Menu.CLOSE_BUTTON_ID)) {
                    event.getMessage().delete().queue();
                    if (!this.bot.isReady()) {
                        event.replyEmbeds(new EmbedBuilder()
                                        .setTitle(MenuEmoji.GEAR + this.lang.getMessage("discord.menu.configuration.title"))
                                        .addField(MenuEmoji.WARNING + this.lang.getMessage("discord.field.incomplete-configuration.title"),
                                                this.lang.getMessage("discord.field.incomplete-configuration.server-manager-description"), false)
                                        .setColor(Color.RED).build())
                                .setEphemeral(true).queue();
                    }
                } else if (!this.bot.isReady()) {
                    event.editMessage(new Response(this.plugin, this.config, this.lang, this.bot).getMessage()).queue();
                } else {
                    if ("mode".equals(buttonID)) {
                        ButtonClickListener.discordIDAxis.remove(member.getId());
                    } else if ("horizontal-radius".equals(buttonID)) {
                        ButtonClickListener.discordIDAxis.put(member.getId(), ConfigField.HORIZONTAL_RADIUS.get());
                    } else if ("vertical-radius".equals(buttonID)) {
                        ButtonClickListener.discordIDAxis.put(member.getId(), ConfigField.VERTICAL_RADIUS.get());
                    }
                    event.editMessage(this.bot.getMenus().get(buttonID).toMessage(this.config, this.lang, this.bot)).queue();
                }
            }
        } else {
            event.replyEmbeds(new ErrorEmbed(this.lang).getAccessDeniedEmbed()).setEphemeral(true).queue();
        }
    }
}
