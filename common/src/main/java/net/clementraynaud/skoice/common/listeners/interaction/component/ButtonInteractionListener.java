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

package net.clementraynaud.skoice.common.listeners.interaction.component;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.bot.BotStatus;
import net.clementraynaud.skoice.common.menus.ConfigurationMenu;
import net.clementraynaud.skoice.common.menus.ConfigurationMenus;
import net.clementraynaud.skoice.common.menus.EmbeddedMenu;
import net.clementraynaud.skoice.common.menus.WorldOverrideMenus;
import net.clementraynaud.skoice.common.storage.LoginNotificationStore;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.clementraynaud.skoice.common.storage.config.WorldOverride;
import net.clementraynaud.skoice.common.storage.config.WorldOverrides;
import net.clementraynaud.skoice.common.util.MapUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ButtonInteractionListener extends ListenerAdapter {

    private final Skoice plugin;

    public ButtonInteractionListener(Skoice plugin) {
        this.plugin = plugin;
    }

    private static Map<String, String> getArgs(String overrideId) {
        return overrideId == null ? MapUtil.of() : MapUtil.of(WorldOverrides.ARG, overrideId);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getMessage().getAuthor().equals(event.getJDA().getSelfUser())) {
            return;
        }

        if (!ConfigurationMenus.contains(event.getMessageId())) {
            new ConfigurationMenu(this.plugin.getBot(), event.getMessageId());
        }

        Member member = event.getMember();
        String buttonId = event.getComponentId();

        if (member == null || member.hasPermission(Permission.MANAGE_SERVER)) {
            if ("configure-now".equals(buttonId)) {
                ConfigurationMenu menu = new ConfigurationMenu(this.plugin.getBot());
                menu.reply(event);

            } else if ("refresh".equals(buttonId)) {
                this.plugin.getListenerManager().update(event.getUser());
                ConfigurationMenus.getFromMessageId(event.getMessage().getId()).ifPresent(menu -> menu.refreshId().edit(event));

            } else if (this.plugin.getBot().getStatus() != BotStatus.READY && !"language".equals(buttonId)) {
                ConfigurationMenus.getFromMessageId(event.getMessageId()).ifPresent(menu -> menu.refreshId().edit(event));

            } else if ("clear-notified-players".equals(buttonId)) {
                this.plugin.getLoginNotificationYamlFile().set(LoginNotificationStore.NOTIFIED_PLAYERS_ID_FIELD, Collections.emptyList());
                new EmbeddedMenu(this.plugin.getBot()).setContent("notified-players-cleared")
                        .reply(event);

            } else {
                String[] parts = WorldOverrides.split(buttonId);
                String baseId = parts[0];
                String overrideId = parts.length > 1 ? parts[1] : null;

                if (this.handleWorldOverride(event, parts)) {
                    return;
                }

                ConfigurationMenus.getFromMessageId(event.getMessage().getId()).ifPresent(menu -> {
                    List<String> unreviewedSettings = this.plugin.getConfigYamlFile().getStringList(ConfigField.UNREVIEWED_SETTINGS.toString());
                    if (unreviewedSettings.contains(baseId)) {
                        unreviewedSettings.remove(baseId);
                        this.plugin.getConfigYamlFile().set(ConfigField.UNREVIEWED_SETTINGS.toString(), unreviewedSettings);
                    }
                    menu.setContent(baseId, ButtonInteractionListener.getArgs(overrideId)).edit(event);
                });
            }

        } else {
            new EmbeddedMenu(this.plugin.getBot()).setContent("access-denied")
                    .reply(event);
        }
    }

    private boolean handleWorldOverride(ButtonInteractionEvent event, String[] parts) {
        String baseId = parts[0];
        String overrideId = parts.length > 1 ? parts[1] : null;
        WorldOverrides overrides = this.plugin.getConfigYamlFile().getWorldOverrides();

        if (WorldOverrides.CREATE_BUTTON_ID.equals(baseId)) {
            event.replyModal(WorldOverrideMenus.getNameModal(this.plugin, null)).queue();
            return true;
        }

        if (overrideId == null || !baseId.startsWith(WorldOverrides.OVERRIDE_MENU_ID)) {
            return false;
        }

        WorldOverride override = overrides.get(overrideId);
        if (override == null) {
            this.show(event, WorldOverrides.LIST_MENU_ID, null);
            return true;
        }

        if (WorldOverrides.EDIT_BUTTON_ID.equals(baseId)) {
            event.replyModal(WorldOverrideMenus.getNameModal(this.plugin, override)).queue();

        } else if (WorldOverrides.PATTERNS_BUTTON_ID.equals(baseId)) {
            event.replyModal(WorldOverrideMenus.getPatternsModal(this.plugin, override)).queue();

        } else if (WorldOverrides.MOVE_UP_BUTTON_ID.equals(baseId)) {
            overrides.move(overrideId, -1);
            this.show(event, WorldOverrides.OVERRIDE_MENU_ID, overrideId);

        } else if (WorldOverrides.MOVE_DOWN_BUTTON_ID.equals(baseId)) {
            overrides.move(overrideId, 1);
            this.show(event, WorldOverrides.OVERRIDE_MENU_ID, overrideId);

        } else if (WorldOverrides.DELETE_BUTTON_ID.equals(baseId)) {
            this.show(event, WorldOverrides.DELETION_MENU_ID, overrideId);

        } else if (WorldOverrides.DELETE_CONFIRM_BUTTON_ID.equals(baseId)) {
            overrides.delete(overrideId);
            this.show(event, WorldOverrides.LIST_MENU_ID, null);

        } else if (WorldOverrides.RESET_BUTTON_ID.equals(baseId) && parts.length > 2) {
            overrides.getScope(override).reset(WorldOverrides.getFields(parts[2]));
            this.show(event, parts[2], overrideId);

        } else {
            return false;
        }

        return true;
    }

    private void show(ButtonInteractionEvent event, String menuId, String overrideId) {
        ConfigurationMenus.getFromMessageId(event.getMessage().getId())
                .ifPresent(menu -> menu.setContent(menuId, ButtonInteractionListener.getArgs(overrideId))
                        .edit(event));
    }
}
