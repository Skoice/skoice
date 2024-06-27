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
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.storage.LoginNotificationYamlFile;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.util.Collections;

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

        if (!this.plugin.getConfigurationMenu().getMessageId().equals(event.getMessage().getId())
                && !event.getMessage().isEphemeral()) {
            event.getMessage().delete().queue();
            return;
        }

        String buttonId = event.getButton().getId();
        if (buttonId == null) {
            return;
        }

        Member member = event.getMember();

        if (buttonId.equals(Menu.MESSAGE_NOT_SHOWING_UP)) {
            event.reply(this.plugin.getLang().getMessage("discord.message-not-showing-up"))
                    .setEphemeral(true).queue();

        } else if (member == null || member.hasPermission(Permission.MANAGE_SERVER)) {
            if (this.plugin.getConfigurationMenu().getMessageId().equals(event.getMessage().getId())) {
                if (this.plugin.getBot().getStatus() != BotStatus.READY && !"language".equals(buttonId)) {
                    event.editMessage(this.plugin.getConfigurationMenu().update()).queue();

                } else if ("customize".equals(buttonId)) {
                    TextInput horizontalRadius = TextInput.create("horizontal-radius",
                                    this.plugin.getLang().getMessage("discord.text-input.horizontal-radius.label"),
                                    TextInputStyle.SHORT)
                            .setValue(this.plugin.getConfigYamlFile().getString(ConfigField.HORIZONTAL_RADIUS.toString()))
                            .setRequiredRange(1, 3)
                            .build();
                    TextInput verticalRadius = TextInput.create("vertical-radius",
                                    this.plugin.getLang().getMessage("discord.text-input.vertical-radius.label"),
                                    TextInputStyle.SHORT)
                            .setValue(this.plugin.getConfigYamlFile().getString(ConfigField.VERTICAL_RADIUS.toString()))
                            .setRequiredRange(1, 3)
                            .build();
                    Modal modal = Modal.create("customize",
                                    this.plugin.getLang().getMessage("discord.field.customize.title"))
                            .addComponents(ActionRow.of(horizontalRadius), ActionRow.of(verticalRadius))
                            .build();
                    event.replyModal(modal).queue();

                } else if ("clear-notified-players".equals(buttonId)) {
                    this.plugin.getLoginNotificationYamlFile().set(LoginNotificationYamlFile.NOTIFIED_PLAYERS_ID_FIELD, Collections.emptyList());
                    event.reply(this.plugin.getBot().getMenu("notified-players-cleared").build())
                            .setEphemeral(true).queue();

                } else {
                    event.editMessage(MessageEditData.fromCreateData(this.plugin.getBot().getMenu(buttonId).build())).queue();
                }

            } else if ("resume-configuration".equals(event.getButton().getId())) {
                event.reply(MessageCreateData.fromEditData(this.plugin.getConfigurationMenu().update())).queue();
            }

        } else {
            event.reply(this.plugin.getBot().getMenu("access-denied").build()).setEphemeral(true).queue();
        }
    }
}
