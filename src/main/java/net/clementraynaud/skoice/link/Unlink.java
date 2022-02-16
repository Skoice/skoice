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

package net.clementraynaud.skoice.link;

import net.clementraynaud.skoice.util.Lang;
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
import static net.clementraynaud.skoice.util.DataGetters.*;

public class Unlink extends ListenerAdapter implements CommandExecutor {

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (event.getName().equals("unlink")) {
            EmbedBuilder embed = new EmbedBuilder().setTitle(":link: " + Lang.Discord.LINKING_PROCESS_EMBED_TITLE.print());
            String minecraftID = getPlugin().getConfigFile().getString("link." + event.getUser().getId());
            if (minecraftID == null) {
                event.replyEmbeds(embed.addField(":warning: " + Lang.Discord.ACCOUNT_NOT_LINKED_FIELD_TITLE.print(), Lang.Discord.ACCOUNT_NOT_LINKED_FIELD_DESCRIPTION.print(), false)
                                .setColor(Color.RED).build())
                        .setEphemeral(true).queue();
            } else {
                unlinkUser(event.getUser().getId(), minecraftID);
                event.replyEmbeds(embed.addField(":heavy_check_mark: " + Lang.Discord.ACCOUNT_UNLINKED_FIELD_TITLE.print(), Lang.Discord.ACCOUNT_UNLINKED_FIELD_DESCRIPTION.print(), false)
                                .setColor(Color.GREEN).build())
                        .setEphemeral(true).queue();
                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(minecraftID));
                if (player.isOnline()) {
                    player.getPlayer().sendMessage(Lang.Minecraft.ACCOUNT_UNLINKED.print());
                    GuildVoiceState voiceState = event.getMember().getVoiceState();
                    if (voiceState != null) {
                        VoiceChannel voiceChannel = voiceState.getChannel();
                        if (voiceChannel != null && voiceChannel.equals(getLobby())) {
                            player.getPlayer().sendMessage(Lang.Minecraft.DISCONNECTED_FROM_PROXIMITY_VOICE_CHAT.print());
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Lang.Minecraft.ILLEGAL_EXECUTOR.print());
            return true;
        }
        Player player = (Player) sender;
        String discordID = getPlugin().getConfigFile().getString("link." + player.getUniqueId());
        if (discordID == null) {
            player.sendMessage(Lang.Minecraft.ACCOUNT_NOT_LINKED.print());
            return true;
        }
        unlinkUser(discordID, player.getUniqueId().toString());
        Member member;
        try {
            member = getGuild().retrieveMemberById(discordID).complete();
            member.getUser().openPrivateChannel().complete()
                    .sendMessageEmbeds(new EmbedBuilder().setTitle(":link: " + Lang.Discord.LINKING_PROCESS_EMBED_TITLE.print())
                            .addField(":heavy_check_mark: " + Lang.Discord.ACCOUNT_UNLINKED_FIELD_TITLE.print(), Lang.Discord.ACCOUNT_UNLINKED_FIELD_DESCRIPTION.print(), false)
                            .setColor(Color.GREEN).build()).queue(success -> {}, failure -> {});
            GuildVoiceState voiceState = member.getVoiceState();
            if (voiceState != null) {
                VoiceChannel voiceChannel = voiceState.getChannel();
                if (voiceChannel != null && voiceChannel.equals(getLobby()) || getNetworks().stream().anyMatch(network -> network.getChannel().equals(voiceChannel))) {
                    player.sendMessage(Lang.Minecraft.DISCONNECTED_FROM_PROXIMITY_VOICE_CHAT.print());
                }
            }
        } catch (ErrorResponseException ignored) {
        }
        player.sendMessage(Lang.Minecraft.ACCOUNT_UNLINKED.print());
        return true;
    }

    private void unlinkUser(String discordID, String minecraftID) {
        getPlugin().getConfigFile().set("link." + minecraftID, null);
        getPlugin().getConfigFile().set("link." + discordID, null);
        getPlugin().saveConfig();
    }
}
