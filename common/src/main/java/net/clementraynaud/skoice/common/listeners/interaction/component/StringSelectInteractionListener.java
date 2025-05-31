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

package net.clementraynaud.skoice.common.listeners.interaction.component;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.lang.DiscordLang;
import net.clementraynaud.skoice.common.lang.LangInfo;
import net.clementraynaud.skoice.common.menus.ConfigurationMenu;
import net.clementraynaud.skoice.common.menus.ConfigurationMenus;
import net.clementraynaud.skoice.common.menus.EmbeddedMenu;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.textinput.TextInput;
import net.dv8tion.jda.api.components.textinput.TextInputStyle;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StringSelectInteractionListener extends ListenerAdapter {

    private final Skoice plugin;

    public StringSelectInteractionListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        Guild guild = this.plugin.getBot().getGuild(event.getInteraction());
        if (!event.getMessage().getAuthor().equals(event.getJDA().getSelfUser()) || guild == null) {
            return;
        }

        if (!ConfigurationMenus.contains(event.getMessageId())) {
            new ConfigurationMenu(this.plugin.getBot(), event.getMessageId());
        }

        Member member = event.getMember();
        if (member == null || member.hasPermission(Permission.MANAGE_SERVER)) {
            String componentId = event.getComponentId();
            List<SelectOption> options = new ArrayList<>(event.getComponent().getOptions());

            switch (componentId) {
                case "server-selection":
                    String selectedGuildId = event.getSelectedOptions().get(0).getValue();

                    if (this.plugin.getBot().getJDA().getGuildById(selectedGuildId) != null) {
                        for (SelectOption server : options) {
                            Guild guildToLeave = this.plugin.getBot().getJDA().getGuildById(server.getValue());
                            if (guildToLeave == null || selectedGuildId.equals(server.getValue())) {
                                continue;
                            }

                            if (guild.getId().equals(server.getValue())) {
                                ConfigurationMenus.getFromMessageId(event.getMessageId())
                                        .ifPresent(menu -> menu.delete(event, success -> guildToLeave.leave().queue()));
                            } else {
                                guildToLeave.leave().queue();
                            }
                        }
                    }

                    if (!event.isAcknowledged()) {
                        ConfigurationMenus.getFromMessageId(event.getMessageId()).ifPresent(menu -> menu.refreshId().edit(event));
                    }
                    break;

                case "language-selection":
                    LangInfo language = LangInfo.valueOf(event.getSelectedOptions().get(0).getValue());
                    this.plugin.getConfigYamlFile().set(ConfigField.LANG.toString(), language.toString());
                    this.plugin.getLang().load(language);
                    this.plugin.getBot().getLang().load(language);
                    this.plugin.getListenerManager().update();
                    this.plugin.getBot().getCommands().register();
                    ConfigurationMenus.getFromMessageId(event.getMessageId()).ifPresent(menu -> menu.setContent("language").edit(event));
                    break;

                case "voice-channel-selection":
                    if ("refresh".equals(event.getSelectedOptions().get(0).getValue())) {
                        ConfigurationMenus.getFromMessageId(event.getMessageId()).ifPresent(menu -> menu.setContent("voice-channel").edit(event));
                    } else {
                        if ("new-voice-channel".equals(event.getSelectedOptions().get(0).getValue())) {
                            TextInput categoryName = TextInput.create("category-name",
                                            this.plugin.getBot().getLang().getMessage("text-input.category-name.label"),
                                            TextInputStyle.SHORT)
                                    .setValue(this.plugin.getBot().getLang().getMessage("text-input.category-name.default-value",
                                            DiscordLang.MAX_SHORT_TEXT_INPUT_VALUE_LENGTH))
                                    .setRequiredRange(1, 25)
                                    .build();
                            TextInput voiceChannelName = TextInput.create("voice-channel-name",
                                            this.plugin.getBot().getLang().getMessage("text-input.voice-channel-name.label"),
                                            TextInputStyle.SHORT)
                                    .setValue(this.plugin.getBot().getLang().getMessage("text-input.voice-channel-name.default-value",
                                            DiscordLang.MAX_SHORT_TEXT_INPUT_VALUE_LENGTH))
                                    .setRequiredRange(1, 25)
                                    .build();
                            Modal modal = Modal.create("new-voice-channel",
                                            this.plugin.getBot().getLang().getMessage("menu.voice-channel.select-menu.select-option.new-voice-channel.label"))
                                    .addComponents(ActionRow.of(categoryName), ActionRow.of(voiceChannelName))
                                    .build();
                            event.replyModal(modal).queue();
                        } else {
                            VoiceChannel voiceChannel = guild.getVoiceChannelById(event.getSelectedOptions().get(0).getValue());
                            if (voiceChannel != null && voiceChannel.getParentCategory() != null) {
                                this.plugin.getBot().getVoiceChannel().setup(voiceChannel, event.getUser());
                            }
                            ConfigurationMenus.getFromMessageId(event.getMessage().getId()).ifPresent(menu -> menu.refreshId().edit(event));
                        }
                    }
                    break;

                case "range-selection":
                    if ("long-range-mode".equals(event.getSelectedOptions().get(0).getValue())) {
                        this.plugin.getConfigYamlFile().set(ConfigField.HORIZONTAL_RADIUS.toString(), 80);
                        this.plugin.getConfigYamlFile().set(ConfigField.VERTICAL_RADIUS.toString(), 40);
                        this.plugin.getListenerManager().update(event.getUser());
                        ConfigurationMenus.getFromMessageId(event.getMessageId()).ifPresent(menu -> menu.refreshId().edit(event));
                    } else if ("short-range-mode".equals(event.getSelectedOptions().get(0).getValue())) {
                        this.plugin.getConfigYamlFile().set(ConfigField.HORIZONTAL_RADIUS.toString(), 40);
                        this.plugin.getConfigYamlFile().set(ConfigField.VERTICAL_RADIUS.toString(), 20);
                        this.plugin.getListenerManager().update(event.getUser());
                        ConfigurationMenus.getFromMessageId(event.getMessageId()).ifPresent(menu -> menu.refreshId().edit(event));
                    } else if ("customized".equals(event.getSelectedOptions().get(0).getValue())) {
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
                        Modal modal = Modal.create("customized",
                                        this.plugin.getBot().getLang().getMessage("field.customized.title"))
                                .addComponents(ActionRow.of(horizontalRadius), ActionRow.of(verticalRadius))
                                .build();
                        event.replyModal(modal).queue();
                    }
                    break;

                case "login-notification-selection":
                    this.plugin.getConfigYamlFile().set(ConfigField.LOGIN_NOTIFICATION.toString(), event.getSelectedOptions().get(0).getValue());
                    ConfigurationMenus.getFromMessageId(event.getMessageId()).ifPresent(menu -> menu.setContent("login-notification").edit(event));
                    break;

                case "action-bar-alerts-selection":
                    options.removeAll(event.getSelectedOptions());
                    options.forEach(option -> this.plugin.getConfigYamlFile().set(option.getValue(), false));
                    event.getSelectedOptions().forEach(option -> this.plugin.getConfigYamlFile().set(option.getValue(), true));
                    ConfigurationMenus.getFromMessageId(event.getMessageId()).ifPresent(menu -> menu.setContent("action-bar-alerts").edit(event));
                    break;

                case "included-players-selection":
                    options.removeAll(event.getSelectedOptions());
                    options.forEach(option -> this.plugin.getConfigYamlFile().set(option.getValue(), false));
                    event.getSelectedOptions().forEach(option -> this.plugin.getConfigYamlFile().set(option.getValue(), true));
                    ConfigurationMenus.getFromMessageId(event.getMessageId()).ifPresent(menu -> menu.setContent("included-players").edit(event));
                    break;

                case "active-worlds-selection":
                    options.removeAll(event.getSelectedOptions());
                    this.plugin.getConfigYamlFile().set(ConfigField.DISABLED_WORLDS.toString(),
                            options.stream().map(SelectOption::getValue).collect(Collectors.toList()));
                    ConfigurationMenus.getFromMessageId(event.getMessageId()).ifPresent(menu -> menu.setContent("active-worlds").edit(event));
                    break;

                case "chaining-selection":
                    this.plugin.getConfigYamlFile().set(ConfigField.CHAINING.toString(), event.getSelectedOptions().get(0).getValue());
                    ConfigurationMenus.getFromMessageId(event.getMessageId()).ifPresent(menu -> menu.setContent("chaining").edit(event));
                    break;

                case "link-synchronization-selection":
                    options.removeAll(event.getSelectedOptions());
                    options.forEach(option -> this.plugin.getConfigYamlFile().set(option.getValue(), false));
                    event.getSelectedOptions().forEach(option -> this.plugin.getConfigYamlFile().set(option.getValue(), true));
                    ConfigurationMenus.getFromMessageId(event.getMessageId()).ifPresent(menu -> menu.setContent("link-synchronization").edit(event));
                    break;

                case "release-channel-selection":
                    this.plugin.getConfigYamlFile().set(ConfigField.RELEASE_CHANNEL.toString(), event.getSelectedOptions().get(0).getValue());
                    ConfigurationMenus.getFromMessageId(event.getMessageId()).ifPresent(menu -> menu.setContent("release-channel").edit(event));
                    break;

                default:
                    try {
                        ConfigField configField = ConfigField.valueOf(componentId.replace("-", "_").toUpperCase());
                        this.plugin.getConfigYamlFile().set(configField.toString(),
                                Boolean.valueOf(event.getSelectedOptions().get(0).getValue()));
                        ConfigurationMenus.getFromMessageId(event.getMessageId()).ifPresent(menu -> menu.setContent(componentId).edit(event));
                    } catch (IllegalArgumentException ignored) {
                    }
            }

        } else {
            new EmbeddedMenu(this.plugin.getBot()).setContent("access-denied")
                    .reply(event);
        }
    }
}
