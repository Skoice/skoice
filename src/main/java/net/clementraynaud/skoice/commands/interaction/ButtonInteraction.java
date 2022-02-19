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

package net.clementraynaud.skoice.commands.interaction;

import net.clementraynaud.skoice.lang.Logger;
import net.clementraynaud.skoice.lang.Discord;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.commands.interaction.LobbySelection.getLobbySelectionMessage;
import static net.clementraynaud.skoice.commands.interaction.MessageManagement.discordIDDistance;
import static net.clementraynaud.skoice.commands.interaction.Settings.*;

public class ButtonInteraction extends ListenerAdapter {

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        Member member = event.getMember();
        if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
            if (getPlugin().getConfig().contains("temp.message-id")
                    && getPlugin().getConfig().getString("temp.message-id").equals(event.getMessageId())
                    && event.getButton() != null) {
                String buttonID = event.getButton().getId();
                switch (buttonID) {
                    case "settings":
                        event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        break;
                    case "close":
                        event.getMessage().delete().queue();
                        discordIDDistance.remove(event.getUser().getId());
                        getPlugin().getConfig().set("temp", null);
                        getPlugin().saveConfig();
                        if (!getPlugin().isBotReady()) {
                            event.replyEmbeds(new EmbedBuilder()
                                            .setTitle(":gear: " + Discord.CONFIGURATION_EMBED_TITLE)
                                            .addField(":warning: " + Discord.INCOMPLETE_CONFIGURATION_FIELD_TITLE, Discord.INCOMPLETE_CONFIGURATION_SERVER_MANAGER_FIELD_DESCRIPTION.toString(), false)
                                            .setColor(Color.RED).build())
                                    .setEphemeral(true).queue();
                        }
                        break;
                    case "lobby":
                        if (getPlugin().isBotReady()) {
                            event.editMessage(getLobbySelectionMessage(event.getGuild())).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    case "language":
                        if (getPlugin().isBotReady()) {
                            event.editMessage(Menu.LANGUAGE.getMessage()).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    case "advanced-settings":
                        if (getPlugin().isBotReady()) {
                            event.editMessage(Menu.ADVANCED_SETTINGS.getMessage()).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    case "mode":
                        discordIDDistance.remove(member.getId());
                        if (getPlugin().isBotReady()) {
                            event.editMessage(ModeSelection.getModeSelectionMessage(false)).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    case "horizontal-radius":
                        if (getPlugin().isBotReady()) {
                            discordIDDistance.put(member.getId(), "horizontal");
                            event.editMessage(Menu.HORIZONTAL_RADIUS.getMessage()).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    case "vertical-radius":
                        if (getPlugin().isBotReady()) {
                            discordIDDistance.put(member.getId(), "vertical");
                            event.editMessage(Menu.VERTICAL_RADIUS.getMessage()).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    case "action-bar-alert":
                        if (getPlugin().isBotReady()) {
                            event.editMessage(Menu.ACTION_BAR_ALERT.getMessage()).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    case "channel-visibility":
                        if (getPlugin().isBotReady()) {
                            event.editMessage(Menu.CHANNEL_VISIBILITY.getMessage()).queue();
                        } else {
                            event.editMessage(getConfigurationMessage(event.getGuild())).queue();
                        }
                        break;
                    default:
                        throw new IllegalStateException(Logger.UNEXPECTED_VALUE.toString().replace("{value}", buttonID));
                }
            }
        } else {
            event.replyEmbeds(getAccessDeniedEmbed()).setEphemeral(true).queue();
        }
    }
}
