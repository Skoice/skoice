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
import net.clementraynaud.skoice.bot.BotCommands;
import net.clementraynaud.skoice.config.ConfigurationField;
import net.clementraynaud.skoice.lang.LangInfo;
import net.clementraynaud.skoice.tasks.InterruptSystemTask;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

public class SelectMenuListener extends ListenerAdapter {

    private final Skoice plugin;

    public SelectMenuListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onSelectionMenu(SelectionMenuEvent event) {
        Member member = event.getMember();
        if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
            if (this.plugin.getConfigurationMenu().exists()
                    && this.plugin.getConfigurationMenu().getMessageId().equals(event.getMessage().getId())
                    && event.getSelectedOptions() != null) {
                String componentId = event.getComponentId();
                switch (componentId) {
                    case "server-selection":
                        if (this.plugin.getBot().getJda().getGuildById(event.getSelectedOptions().get(0).getValue()) != null) {
                            for (SelectOption server : event.getComponent().getOptions()) {
                                if (!event.getGuild().getId().equals(server.getValue())
                                        && this.plugin.getBot().getJda().getGuilds().contains(this.plugin.getBot().getJda().getGuildById(server.getValue()))) {
                                    try {
                                        this.plugin.getBot().getJda().getGuildById(server.getValue()).leave()
                                                .queue(success -> event.editMessage(this.plugin.getConfigurationMenu().update()).queue());
                                    } catch (ErrorResponseException ignored) {
                                    }
                                }
                            }
                        }
                        break;
                    case "language-selection":
                        this.plugin.getConfiguration().getFile().set(ConfigurationField.LANG.toString(), event.getSelectedOptions().get(0).getValue());
                        this.plugin.getConfiguration().saveFile();
                        this.plugin.getLang().load(LangInfo.valueOf(event.getSelectedOptions().get(0).getValue()));
                        this.plugin.updateStatus(false);
                        new BotCommands(this.plugin).register(event.getGuild());
                        event.editMessage(this.plugin.getConfigurationMenu().update()).queue();
                        break;
                    case "lobby-selection":
                        Guild guild = event.getGuild();
                        if (guild != null) {
                            if ("generate".equals(event.getSelectedOptions().get(0).getValue())) {
                                String categoryId = guild.createCategory(this.plugin.getLang().getMessage("discord.default-category-name"))
                                        .complete().getId();
                                String lobbyId = guild.createVoiceChannel(this.plugin.getLang().getMessage("discord.default-lobby-name"),
                                                event.getGuild().getCategoryById(categoryId))
                                        .complete().getId();
                                this.plugin.getConfiguration().getFile().set(ConfigurationField.LOBBY_ID.toString(), lobbyId);
                                this.plugin.getConfiguration().saveFile();
                                new InterruptSystemTask(this.plugin.getConfiguration()).run();
                                this.plugin.updateStatus(false, event.getUser());
                            } else if ("refresh".equals(event.getSelectedOptions().get(0).getValue())) {
                                event.editMessage(this.plugin.getConfigurationMenu().update()).queue();
                            } else {
                                VoiceChannel lobby = guild.getVoiceChannelById(event.getSelectedOptions().get(0).getValue());
                                if (lobby != null && lobby.getParent() != null) {
                                    this.plugin.getConfiguration().getFile().set(ConfigurationField.LOBBY_ID.toString(), event.getSelectedOptions().get(0).getValue());
                                    this.plugin.getConfiguration().saveFile();
                                    new InterruptSystemTask(this.plugin.getConfiguration()).run();
                                    this.plugin.updateStatus(false, event.getUser());
                                }
                            }
                        }
                        event.editMessage(this.plugin.getConfigurationMenu().update()).queue();
                        break;
                    case "mode-selection":
                        if ("vanilla-mode".equals(event.getSelectedOptions().get(0).getValue())) {
                            this.plugin.getConfiguration().getFile().set(ConfigurationField.HORIZONTAL_RADIUS.toString(), 80);
                            this.plugin.getConfiguration().getFile().set(ConfigurationField.VERTICAL_RADIUS.toString(), 40);
                            this.plugin.getConfiguration().saveFile();
                            this.plugin.updateStatus(false, event.getUser());
                            event.editMessage(this.plugin.getConfigurationMenu().update()).queue();
                        } else if ("minigame-mode".equals(event.getSelectedOptions().get(0).getValue())) {
                            this.plugin.getConfiguration().getFile().set(ConfigurationField.HORIZONTAL_RADIUS.toString(), 40);
                            this.plugin.getConfiguration().getFile().set(ConfigurationField.VERTICAL_RADIUS.toString(), 20);
                            this.plugin.getConfiguration().saveFile();
                            this.plugin.updateStatus(false, event.getUser());
                            event.editMessage(this.plugin.getConfigurationMenu().update()).queue();
                        } else if ("customize".equals(event.getSelectedOptions().get(0).getValue())) {
                            event.editMessage(this.plugin.getBot().getMenu("mode").build(true)).queue();
                        }
                        break;
                    case "action-bar-alert":
                        if ("true".equals(event.getSelectedOptions().get(0).getValue())) {
                            this.plugin.getConfiguration().getFile().set(ConfigurationField.ACTION_BAR_ALERT.toString(), true);
                        } else if ("false".equals(event.getSelectedOptions().get(0).getValue())) {
                            this.plugin.getConfiguration().getFile().set(ConfigurationField.ACTION_BAR_ALERT.toString(), false);
                        }
                        this.plugin.getConfiguration().saveFile();
                        event.editMessage(this.plugin.getConfigurationMenu().update()).queue();
                        break;
                    case "channel-visibility":
                        if ("true".equals(event.getSelectedOptions().get(0).getValue())) {
                            this.plugin.getConfiguration().getFile().set(ConfigurationField.CHANNEL_VISIBILITY.toString(), true);
                        } else if ("false".equals(event.getSelectedOptions().get(0).getValue())) {
                            this.plugin.getConfiguration().getFile().set(ConfigurationField.CHANNEL_VISIBILITY.toString(), false);
                        }
                        this.plugin.getConfiguration().saveFile();
                        event.editMessage(this.plugin.getConfigurationMenu().update()).queue();
                        break;
                    default:
                        throw new IllegalStateException(this.plugin.getLang().getMessage("logger.exception.unexpected-value", componentId));
                }
            }
        } else {
            event.reply(this.plugin.getBot().getMenu("access-denied").build()).setEphemeral(true).queue();
        }
    }
}
