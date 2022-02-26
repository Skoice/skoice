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

package net.clementraynaud.skoice.commands.arguments;

import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.lang.MinecraftLang;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.networks.NetworkManager.getNetworks;
import static net.clementraynaud.skoice.config.Config.*;

public class UnlinkArgument {

    public void execute(CommandSender sender) {
        Player player = (Player) sender;
        String discordID = getLinkMap().get(player.getUniqueId());
        if (discordID == null) {
            player.sendMessage(MinecraftLang.ACCOUNT_NOT_LINKED.toString());
            return;
        }
        unlinkUser(player.getUniqueId());
        Member member;
        try {
            member = getGuild().retrieveMemberById(discordID).complete();
            member.getUser().openPrivateChannel().complete()
                    .sendMessageEmbeds(new EmbedBuilder().setTitle(":link: " + DiscordLang.LINKING_PROCESS_EMBED_TITLE)
                            .addField(":heavy_check_mark: " + DiscordLang.ACCOUNT_UNLINKED_FIELD_TITLE, DiscordLang.ACCOUNT_UNLINKED_FIELD_DESCRIPTION.toString(), false)
                            .setColor(Color.GREEN).build()).queue(success -> {
                    }, failure -> {
                    });
            GuildVoiceState voiceState = member.getVoiceState();
            if (voiceState != null) {
                VoiceChannel voiceChannel = voiceState.getChannel();
                if (voiceChannel != null && voiceChannel.equals(getLobby()) || getNetworks().stream().anyMatch(network -> network.getChannel().equals(voiceChannel))) {
                    player.sendMessage(MinecraftLang.DISCONNECTED_FROM_PROXIMITY_VOICE_CHAT.toString());
                }
            }
        } catch (ErrorResponseException ignored) {
        }
        player.sendMessage(MinecraftLang.ACCOUNT_UNLINKED.toString());
    }
}
