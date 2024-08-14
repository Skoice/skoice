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
import net.clementraynaud.skoice.menus.EmbeddedMenu;
import net.clementraynaud.skoice.storage.LoginNotificationYamlFile;
import net.clementraynaud.skoice.storage.TempYamlFile;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

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

        Member member = event.getMember();

        if ("display-issues".equals(buttonId)) {
            event.reply(this.plugin.getBot().getLang().getMessage("display-issues"))
                    .setEphemeral(true).queue();

        } else if (member == null || member.hasPermission(Permission.MANAGE_SERVER)) {
            if ("configure-now".equals(buttonId)) {
                this.plugin.getBot().generateConfigurationMenu(event);

            } else if ("customize".equals(buttonId)) {
                TextInput horizontalRadius = TextInput.create("horizontal-radius",
                                this.plugin.getBot().getLang().getMessage("text-input.horizontal-radius.label"),
                                TextInputStyle.SHORT)
                        .setValue(this.plugin.getConfigYamlFile().getString(ConfigField.HORIZONTAL_RADIUS.toString()))
                        .setRequiredRange(1, 3)
                        .build();
                TextInput verticalRadius = TextInput.create("vertical-radius",
                                this.plugin.getBot().getLang().getMessage("text-input.vertical-radius.label"),
                                TextInputStyle.SHORT)
                        .setValue(this.plugin.getConfigYamlFile().getString(ConfigField.VERTICAL_RADIUS.toString()))
                        .setRequiredRange(1, 3)
                        .build();
                Modal modal = Modal.create("customize",
                                this.plugin.getBot().getLang().getMessage("field.customize.title"))
                        .addComponents(ActionRow.of(horizontalRadius), ActionRow.of(verticalRadius))
                        .build();
                event.replyModal(modal).queue();

            } else if (this.plugin.getBot().getStatus() != BotStatus.READY && !"language".equals(buttonId)) {
                this.plugin.getBot().getConfigurationMenu().ifPresent(menu -> menu.refreshId().edit(event));

            } else if ("clear-notified-players".equals(buttonId)) {
                this.plugin.getLoginNotificationYamlFile().set(LoginNotificationYamlFile.NOTIFIED_PLAYERS_ID_FIELD, Collections.emptyList());
                new EmbeddedMenu(this.plugin.getBot()).setContent("notified-players-cleared")
                        .reply(event);

            } else {
                this.plugin.getBot().getConfigurationMenu().ifPresent(menu -> {
                    List<String> unreviewedSettings = this.plugin.getConfigYamlFile().getStringList(ConfigField.UNREVIEWED_SETTINGS.toString());
                    if (unreviewedSettings.contains(buttonId)) {
                        unreviewedSettings.remove(buttonId);
                        this.plugin.getConfigYamlFile().set(ConfigField.UNREVIEWED_SETTINGS.toString(), unreviewedSettings);
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
