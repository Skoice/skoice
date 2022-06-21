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

package net.clementraynaud.skoice.listeners.interaction.component;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.config.ConfigurationField;
import net.clementraynaud.skoice.menus.Menu;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;

public class ButtonInteractionListener extends ListenerAdapter {

    private static final Map<String, String> discordIdAxis = new HashMap<>();

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
        Member member = event.getMember();
        if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
            if (event.getButton().getId() != null) {
                String buttonId = event.getButton().getId();
                if (this.plugin.getConfigurationMenu().getMessageId().equals(event.getMessage().getId())) {
                    if (buttonId.equals(Menu.CLOSE_BUTTON_ID)) {
                        event.getMessage().delete().queue();
                        if (!this.plugin.getBot().isReady()) {
                            event.reply(this.plugin.getBot().getMenu("incomplete-configuration-server-manager").build())
                                    .setEphemeral(true).queue();
                        }
                    } else if (!this.plugin.getBot().isReady() && !"language".equals(buttonId)) {
                        event.editMessage(this.plugin.getConfigurationMenu().update()).queue();
                    } else {
                        if ("mode".equals(buttonId)) {
                            ButtonInteractionListener.discordIdAxis.remove(member.getId());
                        } else if ("horizontal-radius".equals(buttonId)) {
                            ButtonInteractionListener.discordIdAxis.put(member.getId(), ConfigurationField.HORIZONTAL_RADIUS.toString());
                            event.editMessage(this.plugin.getBot().getMenu(buttonId)
                                    .build(this.plugin.getConfiguration().getFile().getString(ConfigurationField.HORIZONTAL_RADIUS.toString()))).queue();
                            return;
                        } else if ("vertical-radius".equals(buttonId)) {
                            ButtonInteractionListener.discordIdAxis.put(member.getId(), ConfigurationField.VERTICAL_RADIUS.toString());
                            event.editMessage(this.plugin.getBot().getMenu(buttonId)
                                    .build(this.plugin.getConfiguration().getFile().getString(ConfigurationField.VERTICAL_RADIUS.toString()))).queue();
                            return;
                        }
                        event.editMessage(this.plugin.getBot().getMenu(buttonId).build()).queue();
                    }
                } else if ("resume-configuration".equals(event.getButton().getId())) {
                    event.reply(this.plugin.getConfigurationMenu().update()).queue();
                }
            }
        } else {
            event.reply(this.plugin.getBot().getMenu("access-denied").build()).setEphemeral(true).queue();
        }
    }

    public static Map<String, String> getDiscordIdAxis() {
        return ButtonInteractionListener.discordIdAxis;
    }
}
