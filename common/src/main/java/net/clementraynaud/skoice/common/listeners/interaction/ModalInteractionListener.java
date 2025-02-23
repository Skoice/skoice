/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.common.listeners.interaction;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.menus.ConfigurationMenus;
import net.clementraynaud.skoice.common.menus.EmbeddedMenu;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.dv8tion.jda.api.entities.Guild;
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
        if (event.getMessage() == null) {
            return;
        }

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
                        this.plugin.getBot().getVoiceChannel().setup(channel, event.getUser());
                        ConfigurationMenus.getFromMessageId(event.getMessage().getId()).ifPresent(menu -> menu.refreshId().edit(event));
                    }));

        } else if ("customized".equals(event.getModalId())) {
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
                ConfigurationMenus.getFromMessageId(event.getMessage().getId())
                        .ifPresent(menu -> menu.setContent("range").editFromHook());
            } else {
                this.plugin.getConfigYamlFile().set(ConfigField.HORIZONTAL_RADIUS.toString(), horizontalRadius);
                this.plugin.getConfigYamlFile().set(ConfigField.VERTICAL_RADIUS.toString(), verticalRadius);
                this.plugin.getListenerManager().update(event.getUser());
                ConfigurationMenus.getFromMessageId(event.getMessage().getId()).ifPresent(menu -> menu.refreshId().edit(event));
            }
        }
    }
}
