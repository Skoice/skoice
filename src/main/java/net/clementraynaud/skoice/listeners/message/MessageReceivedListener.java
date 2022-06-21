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

package net.clementraynaud.skoice.listeners.message;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.listeners.interaction.component.ButtonInteractionListener;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageReceivedListener extends ListenerAdapter {

    private final Skoice plugin;

    public MessageReceivedListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromGuild()) {
            if (event.getAuthor().equals(event.getJDA().getSelfUser())) {
                if (!event.getMessage().isEphemeral()) {
                    this.plugin.getConfigurationMenu().store(event.getMessage());
                }
            } else if (ButtonInteractionListener.getDiscordIdAxis().containsKey(event.getAuthor().getId())
                    && event.getMessage().getContentRaw().length() <= 4
                    && event.getMessage().getContentRaw().matches("[0-9]+")) {
                int value = Integer.parseInt(event.getMessage().getContentRaw());
                if (value >= 1 && value <= 1000) {
                    event.getMessage().delete().queue();
                    String axis = ButtonInteractionListener.getDiscordIdAxis().get(event.getAuthor().getId());
                    this.plugin.getConfiguration().getFile().set(axis, value);
                    this.plugin.getConfiguration().saveFile();
                    this.plugin.getConfigurationMenu().retrieveMessage()
                            .editMessage(this.plugin.getBot().getMenu(axis)
                                    .build(this.plugin.getConfiguration().getFile().getString(axis))).queue();
                }
            }
        } else if (event.isFromType(ChannelType.PRIVATE)
                && !event.getAuthor().getId().equals(event.getJDA().getSelfUser().getApplicationId())) {
            event.getMessage().reply(this.plugin.getBot().getMenu("illegal-interaction").build()).queue();
        }
    }
}
