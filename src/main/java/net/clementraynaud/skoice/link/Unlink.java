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

package net.clementraynaud.skoice.link;

import net.clementraynaud.skoice.lang.Discord;
import net.clementraynaud.skoice.lang.Minecraft;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.UUID;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.system.ChannelManagement.getNetworks;
import static net.clementraynaud.skoice.util.DataGetters.getGuild;
import static net.clementraynaud.skoice.util.DataGetters.getLobby;

public class Unlink extends ListenerAdapter implements CommandExecutor {

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (event.getName().equals("unlink")) {
            EmbedBuilder embed = new EmbedBuilder().setTitle(":link: " + Discord.LINKING_PROCESS_EMBED_TITLE);
            String minecraftID = getPlugin().getConfig().getString("link." + event.getUser().getId());
            if (minecraftID == null) {
                event.replyEmbeds(embed.addField(":warning: " + Discord.ACCOUNT_NOT_LINKED_FIELD_TITLE, Discord.ACCOUNT_NOT_LINKED_FIELD_DESCRIPTION.toString(), false)
                                .setColor(Color.RED).build())
                        .setEphemeral(true).queue();
            } else {
                unlinkUser(event.getUser().getId(), minecraftID);
                event.replyEmbeds(embed.addField(":heavy_check_mark: " + Discord.ACCOUNT_UNLINKED_FIELD_TITLE, Discord.ACCOUNT_UNLINKED_FIELD_DESCRIPTION.toString(), false)
                                .setColor(Color.GREEN).build())
                        .setEphemeral(true).queue();
                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(minecraftID));
                if (player.isOnline()) {
                    player.getPlayer().sendMessage(Minecraft.ACCOUNT_UNLINKED.toString());
                    GuildVoiceState voiceState = event.getMember().getVoiceState();
                    if (voiceState != null) {
                        VoiceChannel voiceChannel = voiceState.getChannel();
                        if (voiceChannel != null && voiceChannel.equals(getLobby())) {
                            player.getPlayer().sendMessage(Minecraft.DISCONNECTED_FROM_PROXIMITY_VOICE_CHAT.toString());
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Minecraft.ILLEGAL_EXECUTOR.toString());
            return true;
        }
        Player player = (Player) sender;
        String discordID = getPlugin().getConfig().getString("link." + player.getUniqueId());
        if (discordID == null) {
            player.sendMessage(Minecraft.ACCOUNT_NOT_LINKED.toString());
            return true;
        }
        unlinkUser(discordID, player.getUniqueId().toString());
        Member member;
        try {
            member = getGuild().retrieveMemberById(discordID).complete();
            member.getUser().openPrivateChannel().complete()
                    .sendMessageEmbeds(new EmbedBuilder().setTitle(":link: " + Discord.LINKING_PROCESS_EMBED_TITLE)
                            .addField(":heavy_check_mark: " + Discord.ACCOUNT_UNLINKED_FIELD_TITLE, Discord.ACCOUNT_UNLINKED_FIELD_DESCRIPTION.toString(), false)
                            .setColor(Color.GREEN).build()).queue(success -> {
                    }, failure -> {
                    });
            GuildVoiceState voiceState = member.getVoiceState();
            if (voiceState != null) {
                VoiceChannel voiceChannel = voiceState.getChannel();
                if (voiceChannel != null && voiceChannel.equals(getLobby()) || getNetworks().stream().anyMatch(network -> network.getChannel().equals(voiceChannel))) {
                    player.sendMessage(Minecraft.DISCONNECTED_FROM_PROXIMITY_VOICE_CHAT.toString());
                }
            }
        } catch (ErrorResponseException ignored) {
        }
        player.sendMessage(Minecraft.ACCOUNT_UNLINKED.toString());
        return true;
    }

    private void unlinkUser(String discordID, String minecraftID) {
        getPlugin().getConfig().set("link." + minecraftID, null);
        getPlugin().getConfig().set("link." + discordID, null);
        getPlugin().saveConfig();
    }
}
