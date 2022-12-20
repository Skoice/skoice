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
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.clementraynaud.skoice.tasks.InterruptSystemTask;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

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
            ModalMapping categoryValue = event.getValue("category-name");
            ModalMapping voiceChannelValue = event.getValue("voice-channel-name");
            if (categoryValue == null || voiceChannelValue == null) {
                return;
            }
            String categoryName = categoryValue.getAsString();
            String voiceChannelName = voiceChannelValue.getAsString();
            event.getGuild().createCategory(categoryName).queue(category ->
                    event.getGuild().createVoiceChannel(voiceChannelName, category).queue(channel -> {
                        this.plugin.getConfigYamlFile().set(ConfigField.VOICE_CHANNEL_ID.toString(), channel.getId());
                        new InterruptSystemTask(this.plugin.getConfigYamlFile()).run();
                        this.plugin.getListenerManager().update(event.getUser());
                        event.editMessage(this.plugin.getConfigurationMenu().update()).queue();
                    }));
        } else if ("customize".equals(event.getModalId())) {
            int horizontalRadius = 0;
            int verticalRadius = 0;
            ModalMapping horizontalRadiusValue = event.getValue("horizontal-radius");
            ModalMapping verticalRadiusValue = event.getValue("vertical-radius");
            if (horizontalRadiusValue == null || verticalRadiusValue == null) {
                return;
            }
            if (horizontalRadiusValue.getAsString().matches("[0-9]+")) {
                horizontalRadius = Integer.parseInt(horizontalRadiusValue.getAsString());
            }
            if (verticalRadiusValue.getAsString().matches("[0-9]+")) {
                verticalRadius = Integer.parseInt(verticalRadiusValue.getAsString());
            }
            if (horizontalRadius == 0 || verticalRadius == 0) {
                event.reply(this.plugin.getBot().getMenu("illegal-value").build()).setEphemeral(true).queue();
            } else {
                this.plugin.getConfigYamlFile().set(ConfigField.HORIZONTAL_RADIUS.toString(), horizontalRadius);
                this.plugin.getConfigYamlFile().set(ConfigField.VERTICAL_RADIUS.toString(), verticalRadius);
                event.editMessage(this.plugin.getBot().getMenu("mode").build()).queue();
            }
        }
    }
}
