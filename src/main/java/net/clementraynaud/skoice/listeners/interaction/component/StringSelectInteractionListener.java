/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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
import net.clementraynaud.skoice.lang.LangInfo;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.clementraynaud.skoice.tasks.InterruptSystemTask;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class StringSelectInteractionListener extends ListenerAdapter {

    private final Skoice plugin;

    public StringSelectInteractionListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (!event.getMessage().getAuthor().equals(event.getJDA().getSelfUser()) || event.getGuild() == null) {
            return;
        }
        if (!this.plugin.getConfigurationMenu().getMessageId().equals(event.getMessage().getId())) {
            event.getMessage().delete().queue();
            return;
        }
        Member member = event.getMember();
        if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
            String componentId = event.getComponentId();
            switch (componentId) {
                case "server-selection":
                    if (this.plugin.getBot().getJDA().getGuildById(event.getSelectedOptions().get(0).getValue()) != null) {
                        for (SelectOption server : event.getComponent().getOptions()) {
                            Guild guild = this.plugin.getBot().getJDA().getGuildById(server.getValue());
                            if (guild != null && !event.getSelectedOptions().get(0).getValue().equals(server.getValue())) {
                                if (event.getGuild().getId().equals(server.getValue())) {
                                    this.plugin.getConfigurationMenu().retrieveMessage(message ->
                                            message.delete().queue(success ->
                                                    guild.leave().queue()));
                                } else {
                                    guild.leave().queue(success ->
                                            event.editMessage(this.plugin.getConfigurationMenu().update()).queue());
                                }
                            }
                        }
                    }
                    break;
                case "language-selection":
                    this.plugin.getConfigYamlFile().set(ConfigField.LANG.toString(), event.getSelectedOptions().get(0).getValue());
                    this.plugin.getLang().load(LangInfo.valueOf(event.getSelectedOptions().get(0).getValue()));
                    this.plugin.getListenerManager().update();
                    this.plugin.getBotCommands().register(event.getGuild());
                    event.editMessage(MessageEditData.fromCreateData(this.plugin.getBot().getMenu("language").build())).queue();
                    break;
                case "voice-channel-selection":
                    Guild guild = event.getGuild();
                    if (guild != null) {
                        if ("refresh".equals(event.getSelectedOptions().get(0).getValue())) {
                            event.editMessage(MessageEditData.fromCreateData(this.plugin.getBot().getMenu("voice-channel").build())).queue();
                        } else {
                            if ("new-voice-channel".equals(event.getSelectedOptions().get(0).getValue())) {
                                TextInput categoryName = TextInput.create("category-name",
                                                this.plugin.getLang().getMessage("discord.text-input.category-name.label"),
                                                TextInputStyle.SHORT)
                                        .setValue(this.plugin.getLang().getMessage("discord.text-input.category-name.default-value"))
                                        .setRequiredRange(1, 25)
                                        .build();
                                TextInput voiceChannelName = TextInput.create("voice-channel-name",
                                                this.plugin.getLang().getMessage("discord.text-input.voice-channel-name.label"),
                                                TextInputStyle.SHORT)
                                        .setValue(this.plugin.getLang().getMessage("discord.text-input.voice-channel-name.default-value"))
                                        .setRequiredRange(1, 25)
                                        .build();
                                Modal modal = Modal.create("new-voice-channel",
                                                this.plugin.getLang().getMessage("discord.menu.voice-channel.select-menu.select-option.new-voice-channel.label"))
                                        .addComponents(ActionRow.of(categoryName), ActionRow.of(voiceChannelName))
                                        .build();
                                event.replyModal(modal).queue();
                            } else {
                                VoiceChannel voiceChannel = guild.getVoiceChannelById(event.getSelectedOptions().get(0).getValue());
                                if (voiceChannel != null && voiceChannel.getParentCategory() != null) {
                                    this.plugin.getConfigYamlFile().set(ConfigField.VOICE_CHANNEL_ID.toString(),
                                            event.getSelectedOptions().get(0).getValue());
                                    this.plugin.getBot().updateVoiceState();
                                    new InterruptSystemTask(this.plugin.getConfigYamlFile()).run();
                                    this.plugin.getListenerManager().update(event.getUser());
                                    this.plugin.getBot().muteMembers();
                                }
                                event.editMessage(this.plugin.getConfigurationMenu().update()).queue();
                            }
                        }
                    }
                    break;
                case "mode-selection":
                    if ("long-range-mode".equals(event.getSelectedOptions().get(0).getValue())) {
                        this.plugin.getConfigYamlFile().set(ConfigField.HORIZONTAL_RADIUS.toString(), 80);
                        this.plugin.getConfigYamlFile().set(ConfigField.VERTICAL_RADIUS.toString(), 40);
                        this.plugin.getListenerManager().update(event.getUser());
                        event.editMessage(this.plugin.getConfigurationMenu().update()).queue();
                    } else if ("short-range-mode".equals(event.getSelectedOptions().get(0).getValue())) {
                        this.plugin.getConfigYamlFile().set(ConfigField.HORIZONTAL_RADIUS.toString(), 40);
                        this.plugin.getConfigYamlFile().set(ConfigField.VERTICAL_RADIUS.toString(), 20);
                        this.plugin.getListenerManager().update(event.getUser());
                        event.editMessage(this.plugin.getConfigurationMenu().update()).queue();
                    }
                    break;
                case "action-bar-alert":
                    if ("true".equals(event.getSelectedOptions().get(0).getValue())) {
                        this.plugin.getConfigYamlFile().set(ConfigField.ACTION_BAR_ALERT.toString(), true);
                    } else if ("false".equals(event.getSelectedOptions().get(0).getValue())) {
                        this.plugin.getConfigYamlFile().set(ConfigField.ACTION_BAR_ALERT.toString(), false);
                    }
                    event.editMessage(MessageEditData.fromCreateData(this.plugin.getBot().getMenu("action-bar-alert").build())).queue();
                    break;
                case "channel-visibility":
                    if ("true".equals(event.getSelectedOptions().get(0).getValue())) {
                        this.plugin.getConfigYamlFile().set(ConfigField.CHANNEL_VISIBILITY.toString(), true);
                    } else if ("false".equals(event.getSelectedOptions().get(0).getValue())) {
                        this.plugin.getConfigYamlFile().set(ConfigField.CHANNEL_VISIBILITY.toString(), false);
                    }
                    event.editMessage(MessageEditData.fromCreateData(this.plugin.getBot().getMenu("channel-visibility").build())).queue();
                    break;
                default:
                    throw new IllegalStateException(this.plugin.getLang().getMessage("logger.exception.unexpected-value", componentId));
            }
        } else {
            event.reply(this.plugin.getBot().getMenu("access-denied").build()).setEphemeral(true).queue();
        }
    }
}
