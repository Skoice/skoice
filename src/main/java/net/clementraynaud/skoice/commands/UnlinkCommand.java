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

package net.clementraynaud.skoice.commands;

import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.lang.Lang;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.clementraynaud.skoice.util.MapUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.awt.*;
import java.util.UUID;

public class UnlinkCommand extends ListenerAdapter {

    private final Config config;
    private final Lang lang;

    public UnlinkCommand(Config config, Lang lang) {
        this.config = config;
        this.lang = lang;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if ("unlink".equals(event.getName())) {
            EmbedBuilder embed = new EmbedBuilder().setTitle(MenuEmoji.LINK + this.lang.getMessage("discord.menu.linking-process.title"));
            String minecraftID = new MapUtil().getKeyFromValue(this.config.getReader().getLinks(), event.getUser().getId());
            if (minecraftID == null) {
                event.replyEmbeds(embed.addField(MenuEmoji.WARNING + this.lang.getMessage("discord.menu.linking-process.field.account-not-linked.title"),
                                        this.lang.getMessage("discord.menu.linking-process.field.account-not-linked.description"), false)
                                .setColor(Color.RED).build())
                        .setEphemeral(true).queue();
            } else {
                this.config.getUpdater().unlinkUser(minecraftID);
                event.replyEmbeds(embed.addField(MenuEmoji.HEAVY_CHECK_MARK + this.lang.getMessage("discord.menu.linking-process.field.account-unlinked.title"),
                                        this.lang.getMessage("discord.menu.linking-process.field.account-unlinked.description"), false)
                                .setColor(Color.GREEN).build())
                        .setEphemeral(true).queue();
                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(minecraftID));
                if (player.isOnline() && player.getPlayer() != null) {
                    player.getPlayer().sendMessage(this.lang.getMessage("minecraft.chat.player.account-unlinked"));
                    GuildVoiceState voiceState = event.getMember().getVoiceState();
                    if (voiceState != null) {
                        VoiceChannel voiceChannel = voiceState.getChannel();
                        if (voiceChannel != null && voiceChannel.equals(this.config.getReader().getLobby())) {
                            player.getPlayer().sendMessage(this.lang.getMessage("minecraft.chat.player.disconnected-from-proximity-voice-chat"));
                        }
                    }
                }
            }
        }
    }
}
