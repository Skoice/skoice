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

package net.clementraynaud.skoice.listeners.interaction;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.menus.EmbeddedMenu;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.clementraynaud.skoice.tasks.InterruptSystemTask;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
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
        Guild guild = this.plugin.getBot().getGuild(event.getInteraction());
        if (guild == null) {
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
            guild.createCategory(categoryName).queue(category ->
                    guild.createVoiceChannel(voiceChannelName, category).queue(channel -> {
                        VoiceChannel oldVoiceChannel = this.plugin.getConfigYamlFile().getVoiceChannel();
                        if (oldVoiceChannel != null) {
                            oldVoiceChannel.modifyStatus("").queue();
                        }
                        this.plugin.getConfigYamlFile().set(ConfigField.VOICE_CHANNEL_ID.toString(), channel.getId());
                        new InterruptSystemTask(this.plugin).run();
                        this.plugin.getListenerManager().update(event.getUser());
                        this.plugin.getBot().getBotVoiceChannel().muteMembers();
                        this.plugin.getBot().getBotVoiceChannel().setStatus();
                        this.plugin.getConfigurationMenu().refreshId().edit(event);
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
                new EmbeddedMenu(this.plugin.getBot()).setContent("illegal-value")
                        .reply(event);
            } else {
                this.plugin.getConfigYamlFile().set(ConfigField.HORIZONTAL_RADIUS.toString(), horizontalRadius);
                this.plugin.getConfigYamlFile().set(ConfigField.VERTICAL_RADIUS.toString(), verticalRadius);
                this.plugin.getListenerManager().update(event.getUser());
                this.plugin.getConfigurationMenu().refreshId().edit(event);
            }
        }
    }
}
