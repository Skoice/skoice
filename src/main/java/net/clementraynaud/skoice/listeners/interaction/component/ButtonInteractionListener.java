/*
 * Copyright 2020, 2021, 2022, 2023, 2024 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.listeners.interaction.component;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.menus.ConfigurationMenu;
import net.clementraynaud.skoice.menus.ConfigurationMenus;
import net.clementraynaud.skoice.menus.EmbeddedMenu;
import net.clementraynaud.skoice.storage.LoginNotificationYamlFile;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Collections;
import java.util.List;

public class ButtonInteractionListener extends ListenerAdapter {

    private final Skoice plugin;

    public ButtonInteractionListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getMessage().getAuthor().equals(event.getJDA().getSelfUser())) {
            return;
        }

        String buttonId = event.getButton().getId();
        if (buttonId == null) {
            return;
        }

        if (!ConfigurationMenus.contains(event.getMessageId())) {
            new ConfigurationMenu(this.plugin.getBot(), event.getMessageId());
        }

        Member member = event.getMember();

        if ("display-issues".equals(buttonId)) {
            event.reply(this.plugin.getBot().getLang().getMessage("display-issues"))
                    .setEphemeral(true).queue();

        } else if (member == null || member.hasPermission(Permission.MANAGE_SERVER)) {
            if ("configure-now".equals(buttonId)) {
                ConfigurationMenu menu = new ConfigurationMenu(this.plugin.getBot());
                menu.reply(event);

            } else if ("refresh".equals(buttonId)) {
                this.plugin.getListenerManager().update(event.getUser());
                ConfigurationMenus.getFromMessageId(event.getMessage().getId()).ifPresent(menu -> menu.refreshId().edit(event));

            } else if (this.plugin.getBot().getStatus() != BotStatus.READY && !"language".equals(buttonId)) {
                ConfigurationMenus.getFromMessageId(event.getMessageId()).ifPresent(menu -> menu.refreshId().edit(event));

            } else if ("clear-notified-players".equals(buttonId)) {
                this.plugin.getLoginNotificationYamlFile().set(LoginNotificationYamlFile.NOTIFIED_PLAYERS_ID_FIELD, Collections.emptyList());
                new EmbeddedMenu(this.plugin.getBot()).setContent("notified-players-cleared")
                        .reply(event);

            } else {
                ConfigurationMenus.getFromMessageId(event.getMessage().getId()).ifPresent(menu -> {
                    List<String> unreviewedSettings = this.plugin.getConfigYamlFile().getStringList(ConfigField.UNREVIEWED_SETTINGS.toString());
                    if (unreviewedSettings.contains(buttonId)) {
                        unreviewedSettings.remove(buttonId);
                        this.plugin.getConfigYamlFile().set(ConfigField.UNREVIEWED_SETTINGS.toString(), unreviewedSettings);

                        int stepSize = 3;
                        int progressBarState = (3 - unreviewedSettings.size()) * stepSize;
                        int progressBarSize = 3 * stepSize;

                        String progressBar = String.join("", Collections.nCopies(progressBarState, ":green_square:"))
                                + String.join("", Collections.nCopies(progressBarSize - progressBarState, ":black_large_square:"))
                                + ":tada:";

                        this.plugin.getBot().getLang().getFormatter().set("unreviewed-settings-progress-bar", progressBar);
                    }
                    menu.setContent(buttonId).edit(event);
                });
            }


        } else {
            new EmbeddedMenu(this.plugin.getBot()).setContent("access-denied")
                    .reply(event);
        }
    }
}
