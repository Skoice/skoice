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
import net.clementraynaud.skoice.config.ConfigurationField;
import net.clementraynaud.skoice.tasks.InterruptSystemTask;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ModalInteractionListener extends ListenerAdapter {

    private final Skoice plugin;

    public ModalInteractionListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        if (event.getGuild() == null) {
            return;
        }
        if ("new-voice-channel".equals(event.getModalId())) {
            String categoryName = event.getValue("category-name").getAsString();
            String lobbyName = event.getValue("lobby-name").getAsString();
            Category category = event.getGuild().createCategory(categoryName).complete();
            String lobbyId = event.getGuild().createVoiceChannel(lobbyName, category).complete().getId();
            this.plugin.getConfiguration().getFile().set(ConfigurationField.LOBBY_ID.toString(), lobbyId);
            this.plugin.getConfiguration().saveFile();
            new InterruptSystemTask(this.plugin.getConfiguration()).run();
            this.plugin.getListenerManager().update(event.getUser());
            event.editMessage(this.plugin.getConfigurationMenu().update()).queue();
        } else if ("customize".equals(event.getModalId())) {
            int horizontalRadius = 0;
            int verticalRadius = 0;
            if (event.getValue("horizontal-radius").getAsString().matches("[0-9]+")) {
                horizontalRadius = Integer.parseInt(event.getValue("horizontal-radius").getAsString());
            }
            if (event.getValue("vertical-radius").getAsString().matches("[0-9]+")) {
                verticalRadius = Integer.parseInt(event.getValue("vertical-radius").getAsString());
            }
            if (horizontalRadius == 0 || verticalRadius == 0) {
                event.reply(this.plugin.getBot().getMenu("illegal-value").build()).setEphemeral(true).queue();
            } else {
                this.plugin.getConfiguration().getFile().set(ConfigurationField.HORIZONTAL_RADIUS.toString(), horizontalRadius);
                this.plugin.getConfiguration().getFile().set(ConfigurationField.VERTICAL_RADIUS.toString(), verticalRadius);
                this.plugin.getConfiguration().saveFile();
                event.editMessage(this.plugin.getBot().getMenu("mode").build()).queue();
            }
        }
    }
}
