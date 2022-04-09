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
import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.lang.MinecraftLang;
import net.clementraynaud.skoice.menus.MenuEmoji;
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

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if ("unlink" .equals(event.getName())) {
            EmbedBuilder embed = new EmbedBuilder().setTitle(MenuEmoji.LINK + DiscordLang.LINKING_PROCESS_EMBED_TITLE.toString());
            String minecraftID = Config.getKeyFromValue(Config.getLinkMap(), event.getUser().getId());
            if (minecraftID == null) {
                event.replyEmbeds(embed.addField(MenuEmoji.WARNING + DiscordLang.ACCOUNT_NOT_LINKED_FIELD_TITLE.toString(),
                                        DiscordLang.ACCOUNT_NOT_LINKED_FIELD_DESCRIPTION.toString(), false)
                                .setColor(Color.RED).build())
                        .setEphemeral(true).queue();
            } else {
                Config.unlinkUser(minecraftID);
                event.replyEmbeds(embed.addField(MenuEmoji.HEAVY_CHECK_MARK + DiscordLang.ACCOUNT_UNLINKED_FIELD_TITLE.toString(),
                                        DiscordLang.ACCOUNT_UNLINKED_FIELD_DESCRIPTION.toString(), false)
                                .setColor(Color.GREEN).build())
                        .setEphemeral(true).queue();
                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(minecraftID));
                if (player.isOnline()) {
                    player.getPlayer().sendMessage(MinecraftLang.ACCOUNT_UNLINKED.toString());
                    GuildVoiceState voiceState = event.getMember().getVoiceState();
                    if (voiceState != null) {
                        VoiceChannel voiceChannel = voiceState.getChannel();
                        if (voiceChannel != null && voiceChannel.equals(Config.getLobby())) {
                            player.getPlayer().sendMessage(MinecraftLang.DISCONNECTED_FROM_PROXIMITY_VOICE_CHAT.toString());
                        }
                    }
                }
            }
        }
    }
}
