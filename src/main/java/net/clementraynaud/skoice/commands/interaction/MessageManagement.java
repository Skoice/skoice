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

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.bot.Bot.getJda;
import static net.clementraynaud.skoice.commands.interaction.Settings.*;

public class MessageManagement extends ListenerAdapter {

    public static final Map<String, String> discordIDDistance = new HashMap<>();
    private boolean configureCommandCooldown = false;

    public static void deleteConfigurationMessage() {
        if (getPlugin().getConfig().contains("temp.guild-id")
                && getPlugin().getConfig().contains("temp.text-channel-id")
                && getPlugin().getConfig().contains("temp.message-id")) {
            try {
                Guild guild = getJda().getGuildById(getPlugin().getConfig().getString("temp.guild-id"));
                if (guild != null) {
                    TextChannel textChannel = guild.getTextChannelById(getPlugin().getConfig().getString("temp.text-channel-id"));
                    if (textChannel != null) {
                        textChannel.retrieveMessageById(getPlugin().getConfig().getString("temp.message-id"))
                                .complete().delete().queue(success -> {
                                }, failure -> {
                                });
                    }
                }
            } catch (ErrorResponseException | NullPointerException ignored) {
            }
        }
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String discordID = event.getAuthor().getId();
        if (discordID.equals(event.getJDA().getSelfUser().getId())) {
            if (!event.getMessage().isEphemeral()) {
                getPlugin().getConfig().set("temp.guild-id", event.getGuild().getId());
                getPlugin().getConfig().set("temp.text-channel-id", event.getChannel().getId());
                getPlugin().getConfig().set("temp.message-id", event.getMessageId());
                getPlugin().saveConfig();
            }
        } else if (discordIDDistance.containsKey(event.getAuthor().getId())
                && event.getMessage().getContentRaw().length() <= 4
                && event.getMessage().getContentRaw().matches("[0-9]+")) {
            int value = Integer.parseInt(event.getMessage().getContentRaw());
            if (value >= 1 && value <= 1000) {
                event.getMessage().delete().queue();
                getPlugin().getConfig().set("radius." + discordIDDistance.get(event.getAuthor().getId()), value);
                getPlugin().saveConfig();
                deleteConfigurationMessage();
                if (discordIDDistance.get(event.getAuthor().getId()).equals("horizontal")) {
                    event.getChannel().sendMessage(Menu.HORIZONTAL_RADIUS.getMessage()).queue();
                } else if (discordIDDistance.get(event.getAuthor().getId()).equals("vertical")) {
                    event.getChannel().sendMessage(Menu.VERTICAL_RADIUS.getMessage()).queue();
                }
            }
        }
    }

    @Override
    public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {
        if (getPlugin().getConfig().contains("temp")
                && event.getMessageId().equals(getPlugin().getConfig().getString("temp.message-id"))) {
            getPlugin().getConfig().set("temp", null);
            getPlugin().saveConfig();
            discordIDDistance.clear();
        }
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (event.getName().equals("configure")) {
            Member member = event.getMember();
            if (member != null && member.hasPermission(Permission.MANAGE_SERVER)) {
                if (configureCommandCooldown) {
                    event.replyEmbeds(getTooManyInteractionsEmbed()).setEphemeral(true).queue();
                } else {
                    if (getPlugin().getConfig().contains("temp")) {
                        deleteConfigurationMessage();
                    }
                    event.reply(getConfigurationMessage(event.getGuild())).queue();
                    configureCommandCooldown = true;
                    new Timer().schedule(new TimerTask() {

                        @Override
                        public void run() {
                            configureCommandCooldown = false;
                        }
                    }, 5000);
                }
            } else {
                event.replyEmbeds(getAccessDeniedEmbed()).setEphemeral(true).queue();
            }
        }
    }
}
