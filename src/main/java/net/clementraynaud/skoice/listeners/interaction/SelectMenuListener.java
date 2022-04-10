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
import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.bot.Commands;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.lang.LangFile;
import net.clementraynaud.skoice.menus.ErrorEmbed;
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.menus.Response;
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
    private final Config config;
    private final LangFile lang;
    private final Bot bot;

    public SelectMenuListener(Skoice plugin, Config config, LangFile lang, Bot bot) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        this.bot = bot;
    }

    @Override
    public void onSelectionMenu(SelectionMenuEvent event) {
        Member member = event.getMember();
        if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
            if (this.config.getFile().contains(ConfigField.TEMP_MESSAGE.get())
                    && this.config.getFile().get(ConfigField.TEMP_MESSAGE.get()).equals(event.getMessage())
                    && event.getSelectedOptions() != null) {
                String componentID = event.getComponentId();
                switch (componentID) {
                    case "server-selection":
                        if (this.bot.getJda().getGuildById(event.getSelectedOptions().get(0).getValue()) != null) {
                            for (SelectOption server : event.getComponent().getOptions()) {
                                if (!event.getGuild().getId().equals(server.getValue())
                                        && this.bot.getJda().getGuilds().contains(this.bot.getJda().getGuildById(server.getValue()))) {
                                    try {
                                        this.bot.getJda().getGuildById(server.getValue()).leave()
                                                .queue(success -> event.editMessage(new Response(this.plugin, this.config, this.lang, this.bot).getMessage()).queue());
                                    } catch (ErrorResponseException ignored) {
                                    }
                                }
                            }
                        }
                        break;
                    case "language-selection":
                        this.config.getFile().set(ConfigField.LANG.get(), event.getSelectedOptions().get(0).getValue());
                        this.config.saveFile();
                        this.plugin.updateConfigurationStatus(false);
                        new Commands(this.plugin, this.lang, this.bot).register(event.getGuild());
                        event.editMessage(new Response(this.plugin, this.config, this.lang, this.bot).getMessage()).queue();
                        break;
                    case "lobby-selection":
                        Guild guild = event.getGuild();
                        if (guild != null) {
                            if ("generate".equals(event.getSelectedOptions().get(0).getValue())) {
                                String categoryID = guild.createCategory(this.lang.getMessage("discord.default-category-name"))
                                        .complete().getId();
                                String lobbyID = guild.createVoiceChannel(this.lang.getMessage("discord.default-lobby-name"), event.getGuild().getCategoryById(categoryID))
                                        .complete().getId();
                                this.config.getFile().set(ConfigField.LOBBY_ID.get(), lobbyID);
                                this.config.saveFile();
                                this.plugin.updateConfigurationStatus(false);
                            } else if ("refresh".equals(event.getSelectedOptions().get(0).getValue())) {
                                event.editMessage(new Response(this.plugin, this.config, this.lang, this.bot).getMessage()).queue();
                            } else {
                                VoiceChannel lobby = guild.getVoiceChannelById(event.getSelectedOptions().get(0).getValue());
                                if (lobby != null && lobby.getParent() != null) {
                                    this.config.getFile().set(ConfigField.LOBBY_ID.get(), event.getSelectedOptions().get(0).getValue());
                                    this.config.saveFile();
                                    this.plugin.updateConfigurationStatus(false);
                                }
                            }
                        }
                        event.editMessage(new Response(this.plugin, this.config, this.lang, this.bot).getMessage()).queue();
                        break;
                    case "mode-selection":
                        if ("vanilla-mode".equals(event.getSelectedOptions().get(0).getValue())) {
                            this.config.getFile().set(ConfigField.HORIZONTAL_RADIUS.get(), 80);
                            this.config.getFile().set(ConfigField.VERTICAL_RADIUS.get(), 40);
                            this.config.saveFile();
                            this.plugin.updateConfigurationStatus(false);
                            event.editMessage(new Response(this.plugin, this.config, this.lang, this.bot).getMessage()).queue();
                        } else if ("minigame-mode".equals(event.getSelectedOptions().get(0).getValue())) {
                            this.config.getFile().set(ConfigField.HORIZONTAL_RADIUS.get(), 40);
                            this.config.getFile().set(ConfigField.VERTICAL_RADIUS.get(), 20);
                            this.config.saveFile();
                            this.plugin.updateConfigurationStatus(false);
                            event.editMessage(new Response(this.plugin, this.config, this.lang, this.bot).getMessage()).queue();
                        } else if ("customize".equals(event.getSelectedOptions().get(0).getValue())) {
                            Menu.customizeRadius = true;
                            event.editMessage(this.bot.getMenus().get("mode").toMessage(this.config, this.lang, this.bot)).queue();
                        }
                        break;
                    case "action-bar-alert":
                        if ("true".equals(event.getSelectedOptions().get(0).getValue())) {
                            this.config.getFile().set(ConfigField.ACTION_BAR_ALERT.get(), true);
                        } else if ("false".equals(event.getSelectedOptions().get(0).getValue())) {
                            this.config.getFile().set(ConfigField.ACTION_BAR_ALERT.get(), false);
                        }
                        this.config.saveFile();
                        event.editMessage(new Response(this.plugin, this.config, this.lang, this.bot).getMessage()).queue();
                        break;
                    case "channel-visibility":
                        if ("true".equals(event.getSelectedOptions().get(0).getValue())) {
                            this.config.getFile().set(ConfigField.CHANNEL_VISIBILITY.get(), true);
                        } else if ("false".equals(event.getSelectedOptions().get(0).getValue())) {
                            this.config.getFile().set(ConfigField.CHANNEL_VISIBILITY.get(), false);
                        }
                        this.config.saveFile();
                        event.editMessage(new Response(this.plugin, this.config, this.lang, this.bot).getMessage()).queue();
                        break;
                    default:
                        throw new IllegalStateException(this.lang.getMessage("logger.exception.unexpected-value", componentID));
                }
            }
        } else {
            event.replyEmbeds(new ErrorEmbed(this.lang).getAccessDeniedEmbed()).setEphemeral(true).queue();
        }
    }
}
