/*
 * Copyright 2020, 2021 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
 * Copyright 2016, 2017, 2018, 2019, 2020, 2021 Austin "Scarsz" Shapiro
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

package net.clementraynaud.skoice.system;

import net.clementraynaud.skoice.lang.Discord;
import net.clementraynaud.skoice.lang.Minecraft;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static net.clementraynaud.skoice.system.ChannelManagement.refreshMutedUsers;
import static net.clementraynaud.skoice.util.DataGetters.*;

public class MarkPlayersDirty extends ListenerAdapter implements Listener {

    private static Set<UUID> dirtyPlayers = new HashSet<>();

    public static Set<UUID> getDirtyPlayers() {
        return dirtyPlayers;
    }

    public static void clearDirtyPlayers() {
        dirtyPlayers = new HashSet<>();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        markDirty(player);
        Member member = getMember(player.getUniqueId());
        if (member != null) {
            GuildVoiceState voiceState = member.getVoiceState();
            if (voiceState != null) {
                VoiceChannel voiceChannel = voiceState.getChannel();
                if (voiceChannel != null && voiceChannel.equals(getLobby())) {
                    player.sendMessage(Minecraft.CONNECTED_TO_PROXIMITY_VOICE_CHAT.toString());
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        markDirty(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        markDirty(player);
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        refreshMutedUsers(event.getChannelJoined(), event.getMember());
        if (!event.getChannelJoined().equals(getLobby())) return;
        UUID minecraftID = getMinecraftID(event.getMember());
        if (minecraftID == null) {
            try {
                event.getMember().getUser().openPrivateChannel().complete()
                        .sendMessageEmbeds(new EmbedBuilder().setTitle(":link: " + Discord.LINKING_PROCESS_EMBED_TITLE.toString())
                                .addField(":warning: " + Discord.ACCOUNT_NOT_LINKED_FIELD_TITLE.toString(), Discord.ACCOUNT_NOT_LINKED_FIELD_ALTERNATIVE_DESCRIPTION.toString().replace("{discordServer}", event.getGuild().getName()), false)
                                .setColor(Color.RED).build()).queue(success -> {
                        }, failure -> {
                        });
            } catch (ErrorResponseException ignored) {
            }
        } else {
            OfflinePlayer player = Bukkit.getOfflinePlayer(minecraftID);
            if (player.isOnline()) {
                markDirty(player.getPlayer());
                player.getPlayer().sendMessage(Minecraft.CONNECTED_TO_PROXIMITY_VOICE_CHAT.toString());
            }
        }
    }

    public void markDirty(Player player) {
        dirtyPlayers.add(player.getUniqueId());
    }
}
