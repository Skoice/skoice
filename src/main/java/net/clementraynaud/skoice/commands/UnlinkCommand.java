/*
 * Copyright 2020, 2021, 2022, 2023, 2024 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.commands;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.system.LinkedPlayer;
import net.clementraynaud.skoice.util.MapUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class UnlinkCommand extends ListenerAdapter {

    private final Skoice plugin;

    public UnlinkCommand(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if ("unlink".equals(event.getName()) && event.getMember() != null) {
            if (this.plugin.getBot().getStatus() != BotStatus.READY && event.getMember() != null) {
                if (event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
                    event.reply(this.plugin.getBot().getMenu("incomplete-configuration-server-manager").build())
                            .setEphemeral(true).queue();
                } else {
                    event.reply(this.plugin.getBot().getMenu("incomplete-configuration").build())
                            .setEphemeral(true).queue();
                }
                return;
            }

            String minecraftId = MapUtil.getKeyFromValue(this.plugin.getLinksYamlFile().getLinks(), event.getUser().getId());
            if (minecraftId == null) {
                event.reply(this.plugin.getBot().getMenu("account-not-linked")
                                .build(this.plugin.getBot().getGuild().getName()))
                        .setEphemeral(true).queue();
                return;
            }

            this.plugin.getLinksYamlFile().unlinkUser(minecraftId);
            event.reply(this.plugin.getBot().getMenu("account-unlinked").build()).setEphemeral(true).queue();

            OfflinePlayer player = this.plugin.getServer().getOfflinePlayer(UUID.fromString(minecraftId));
            if (player.isOnline() && player.getPlayer() != null) {
                player.getPlayer().sendMessage(this.plugin.getLang().getMessage("minecraft.chat.player.account-unlinked"));
                GuildVoiceState voiceState = event.getMember().getVoiceState();
                if (voiceState != null) {
                    AudioChannel voiceChannel = voiceState.getChannel();
                    if (voiceChannel != null && voiceChannel.equals(this.plugin.getConfigYamlFile().getVoiceChannel())) {
                        player.getPlayer().sendMessage(this.plugin.getLang().getMessage("minecraft.chat.player.disconnected"));
                    }
                }
            }
        }
    }
}