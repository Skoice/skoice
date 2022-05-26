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
import net.clementraynaud.skoice.bot.Commands;
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.lang.LangInfo;
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.tasks.InterruptSystemTask;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.Collections;

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
                String componentID = event.getComponentId();
                switch (componentID) {
                    case "server-selection":
                        if (this.plugin.getBot().getJda().getGuildById(event.getSelectedOptions().get(0).getValue()) != null) {
                            for (SelectOption server : event.getComponent().getOptions()) {
                                if (!event.getGuild().getId().equals(server.getValue())
                                        && this.plugin.getBot().getJda().getGuilds().contains(this.plugin.getBot().getJda().getGuildById(server.getValue()))) {
                                    try {
                                        this.plugin.getBot().getJda().getGuildById(server.getValue()).leave()
                                                .queue(success -> event.editMessage(this.plugin.getConfigurationMenu().getMessage()).queue());
                                    } catch (ErrorResponseException ignored) {
                                    }
                                }
                            }
                        }
                        break;
                    case "language-selection":
                        this.plugin.readConfig().getFile().set(ConfigField.LANG.get(), event.getSelectedOptions().get(0).getValue());
                        this.plugin.readConfig().saveFile();
                        this.plugin.updateStatus(false);
                        this.plugin.getLang().load(LangInfo.valueOf(event.getSelectedOptions().get(0).getValue()));
                        new Commands(this.plugin).register(event.getGuild());
                        event.editMessage(this.plugin.getConfigurationMenu().getMessage()).queue();
                        break;
                    case "lobby-selection":
                        Guild guild = event.getGuild();
                        if (guild != null) {
                            if ("generate".equals(event.getSelectedOptions().get(0).getValue())) {
                                String categoryID = guild.createCategory(this.plugin.getLang().getMessage("discord.default-category-name"))
                                        .complete().getId();
                                String lobbyID = guild.createVoiceChannel(this.plugin.getLang().getMessage("discord.default-lobby-name"),
                                                event.getGuild().getCategoryById(categoryID))
                                        .complete().getId();
                                this.plugin.readConfig().getFile().set(ConfigField.LOBBY_ID.get(), lobbyID);
                                this.plugin.readConfig().saveFile();
                                new InterruptSystemTask(this.plugin.readConfig()).run();
                                this.plugin.updateStatus(false);
                            } else if ("refresh".equals(event.getSelectedOptions().get(0).getValue())) {
                                event.editMessage(this.plugin.getConfigurationMenu().getMessage()).queue();
                            } else {
                                VoiceChannel lobby = guild.getVoiceChannelById(event.getSelectedOptions().get(0).getValue());
                                if (lobby != null && lobby.getParent() != null) {
                                    this.plugin.readConfig().getFile().set(ConfigField.LOBBY_ID.get(), event.getSelectedOptions().get(0).getValue());
                                    this.plugin.readConfig().saveFile();
                                    new InterruptSystemTask(this.plugin.readConfig()).run();
                                    this.plugin.updateStatus(false);
                                }
                            }
                        }
                        event.editMessage(this.plugin.getConfigurationMenu().getMessage()).queue();
                        break;
                    case "mode-selection":
                        if ("vanilla-mode".equals(event.getSelectedOptions().get(0).getValue())) {
                            this.plugin.readConfig().getFile().set(ConfigField.HORIZONTAL_RADIUS.get(), 80);
                            this.plugin.readConfig().getFile().set(ConfigField.VERTICAL_RADIUS.get(), 40);
                            this.plugin.readConfig().saveFile();
                            this.plugin.updateStatus(false);
                            event.editMessage(this.plugin.getConfigurationMenu().getMessage()).queue();
                        } else if ("minigame-mode".equals(event.getSelectedOptions().get(0).getValue())) {
                            this.plugin.readConfig().getFile().set(ConfigField.HORIZONTAL_RADIUS.get(), 40);
                            this.plugin.readConfig().getFile().set(ConfigField.VERTICAL_RADIUS.get(), 20);
                            this.plugin.readConfig().saveFile();
                            this.plugin.updateStatus(false);
                            event.editMessage(this.plugin.getConfigurationMenu().getMessage()).queue();
                        } else if ("customize".equals(event.getSelectedOptions().get(0).getValue())) {
                            event.editMessage(this.plugin.getBot().getMenus().get("mode").toMessage(true)).queue();
                        }
                        break;
                    case "action-bar-alert":
                        if ("true".equals(event.getSelectedOptions().get(0).getValue())) {
                            this.plugin.readConfig().getFile().set(ConfigField.ACTION_BAR_ALERT.get(), true);
                        } else if ("false".equals(event.getSelectedOptions().get(0).getValue())) {
                            this.plugin.readConfig().getFile().set(ConfigField.ACTION_BAR_ALERT.get(), false);
                        }
                        this.plugin.readConfig().saveFile();
                        event.editMessage(this.plugin.getConfigurationMenu().getMessage()).queue();
                        break;
                    case "channel-visibility":
                        if ("true".equals(event.getSelectedOptions().get(0).getValue())) {
                            this.plugin.readConfig().getFile().set(ConfigField.CHANNEL_VISIBILITY.get(), true);
                        } else if ("false".equals(event.getSelectedOptions().get(0).getValue())) {
                            this.plugin.readConfig().getFile().set(ConfigField.CHANNEL_VISIBILITY.get(), false);
                        }
                        this.plugin.readConfig().saveFile();
                        event.editMessage(this.plugin.getConfigurationMenu().getMessage()).queue();
                        break;
                    default:
                        throw new IllegalStateException(this.plugin.getLang().getMessage("logger.exception.unexpected-value", componentID));
                }
            }
        } else {
            event.reply(new Menu(this.plugin, "error",
                    Collections.singleton(this.plugin.getBot().getFields().get("access-denied")))
                    .toMessage()).setEphemeral(true).queue();
        }
    }
}
