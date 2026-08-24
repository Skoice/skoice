/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025, 2026 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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
import net.clementraynaud.skoice.common.bot.BotStatus;
import net.clementraynaud.skoice.common.menus.ConfigurationMenus;
import net.clementraynaud.skoice.common.menus.EmbeddedMenu;
import net.clementraynaud.skoice.common.menus.WorldOverrideMenus;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.clementraynaud.skoice.common.storage.config.ConfigScope;
import net.clementraynaud.skoice.common.storage.config.WorldOverride;
import net.clementraynaud.skoice.common.storage.config.WorldOverrides;
import net.clementraynaud.skoice.common.util.MapUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

import java.util.ArrayList;
import java.util.List;

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

        BotStatus oldStatus = this.plugin.getBot().getStatus();
        String[] parts = WorldOverrides.split(event.getModalId());
        String modalId = parts[0];
        String overrideId = parts.length > 1 ? parts[1] : null;

        if (WorldOverrides.CREATE_BUTTON_ID.equals(modalId) || WorldOverrides.EDIT_BUTTON_ID.equals(modalId)) {
            this.saveWorldOverrideName(event, modalId, overrideId);

        } else if (WorldOverrides.PATTERNS_BUTTON_ID.equals(modalId)) {
            this.saveWorldOverridePatterns(event, overrideId);

        } else if ("new-voice-channel".equals(modalId)) {
            ModalMapping categoryValue = event.getValue("category-name");
            ModalMapping voiceChannelValue = event.getValue("voice-channel-name");
            if (categoryValue == null || voiceChannelValue == null) {
                return;
            }
            String categoryName = categoryValue.getAsString();
            String voiceChannelName = voiceChannelValue.getAsString();
            guild.createCategory(categoryName).queue(category -> {
                category.getManager().setPosition(0).queue();
                guild.createVoiceChannel(voiceChannelName, category).queue(channel -> {
                    this.plugin.getBot().getVoiceChannel().setup(channel, event.getUser());
                    ConfigurationMenus.getFromMessageId(event.getMessage().getId()).ifPresent(menu -> {
                        if (oldStatus != this.plugin.getBot().getStatus()) {
                            menu.refreshId();
                        }
                        menu.edit(event);
                    });
                });
            });

        } else if ("customized".equals(modalId)) {
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
                        .ifPresent(EmbeddedMenu::editFromHook);
            } else {
                ConfigScope scope = this.plugin.getConfigYamlFile().scope(overrideId);
                scope.set(ConfigField.HORIZONTAL_RADIUS.toString(), horizontalRadius);
                scope.set(ConfigField.VERTICAL_RADIUS.toString(), verticalRadius);
                if (overrideId == null) {
                    this.plugin.getListenerManager().update(event.getUser());
                }
                ConfigurationMenus.getFromMessageId(event.getMessage().getId()).ifPresent(menu -> {
                    if (oldStatus != this.plugin.getBot().getStatus()) {
                        menu.refreshId();
                    }
                    menu.edit(event);
                });
            }
        }
    }

    private void saveWorldOverrideName(ModalInteractionEvent event, String modalId, String overrideId) {
        ModalMapping nameValue = event.getValue(WorldOverrides.NAME_INPUT_ID);
        if (nameValue == null) {
            return;
        }

        WorldOverrides overrides = this.plugin.getConfigYamlFile().getWorldOverrides();

        if (WorldOverrides.CREATE_BUTTON_ID.equals(modalId)) {
            if (overrides.getAll().size() >= WorldOverrides.MAX_AMOUNT) {
                this.show(event, WorldOverrides.LIST_MENU_ID, null);
                return;
            }
            WorldOverride created = overrides.create(nameValue.getAsString());
            if (created == null) {
                this.show(event, WorldOverrides.LIST_MENU_ID, null);
                return;
            }
            this.show(event, WorldOverrides.WORLDS_MENU_ID, created.getId());
            return;
        }

        if (overrides.get(overrideId) == null) {
            this.show(event, WorldOverrides.LIST_MENU_ID, null);
            return;
        }
        overrides.rename(overrideId, nameValue.getAsString());
        this.show(event, WorldOverrides.OVERRIDE_MENU_ID, overrideId);
    }

    private void saveWorldOverridePatterns(ModalInteractionEvent event, String overrideId) {
        ModalMapping patternsValue = event.getValue(WorldOverrides.PATTERNS_INPUT_ID);
        WorldOverrides overrides = this.plugin.getConfigYamlFile().getWorldOverrides();

        if (patternsValue == null || overrides.get(overrideId) == null) {
            this.show(event, WorldOverrides.LIST_MENU_ID, null);
            return;
        }

        List<String> valid = new ArrayList<>();
        List<String> invalid = new ArrayList<>();
        for (String pattern : WorldOverrideMenus.readPatterns(patternsValue.getAsString())) {
            if (WorldOverride.isValidPattern(pattern)) {
                valid.add(pattern);
            } else {
                invalid.add(pattern);
            }
        }
        overrides.setPatterns(overrideId, valid);

        if (invalid.isEmpty()) {
            this.show(event, WorldOverrides.WORLDS_MENU_ID, overrideId);
            return;
        }

        new EmbeddedMenu(this.plugin.getBot())
                .setContent("invalid-pattern",
                        MapUtil.of("patterns", WorldOverrideMenus.format(invalid)))
                .reply(event);
        ConfigurationMenus.getFromMessageId(event.getMessage().getId())
                .ifPresent(EmbeddedMenu::editFromHook);
    }

    private void show(ModalInteractionEvent event, String menuId, String overrideId) {
        ConfigurationMenus.getFromMessageId(event.getMessage().getId())
                .ifPresent(menu -> menu.setContent(menuId, overrideId == null
                                ? MapUtil.of()
                                : MapUtil.of(WorldOverrides.ARG, overrideId))
                        .edit(event));
    }
}
