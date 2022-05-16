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

package net.clementraynaud.skoice.commands.skoice.arguments;

import net.clementraynaud.skoice.menus.MenuEmoji;
import net.clementraynaud.skoice.system.Network;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;

public class UnlinkArgument extends Argument {

    public UnlinkArgument(CommandSender sender) {
        super(sender, null, false, false);
    }

    @Override
    public void run() {
        if (!this.canExecuteCommand()) {
            return;
        }
        Player player = (Player) this.sender;
        String discordID = super.config.getReader().getLinks().get(player.getUniqueId().toString());
        if (discordID == null) {
            player.sendMessage(super.lang.getMessage("minecraft.chat.player.account-not-linked"));
            return;
        }
        super.config.getUpdater().unlinkUser(player.getUniqueId().toString());
        Member member;
        try {
            member = super.config.getReader().getGuild().retrieveMemberById(discordID).complete();
            member.getUser().openPrivateChannel().complete()
                    .sendMessageEmbeds(new EmbedBuilder().setTitle(MenuEmoji.LINK + super.lang.getMessage("discord.menu.linking-process.title"))
                            .addField(MenuEmoji.HEAVY_CHECK_MARK + super.lang.getMessage("discord.menu.linking-process.field.account-unlinked.title"),
                                    super.lang.getMessage("discord.menu.linking-process.field.account-unlinked.description"), false)
                            .setColor(Color.GREEN).build())
                    .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
            GuildVoiceState voiceState = member.getVoiceState();
            if (voiceState != null) {
                VoiceChannel voiceChannel = voiceState.getChannel();
                if (voiceChannel != null && voiceChannel.equals(super.config.getReader().getLobby())
                        || Network.getNetworks().stream().anyMatch(network -> network.getChannel().equals(voiceChannel))) {
                    player.sendMessage(super.lang.getMessage("minecraft.chat.player.disconnected-from-proximity-voice-chat"));
                }
            }
        } catch (ErrorResponseException ignored) {
        }
        player.sendMessage(super.lang.getMessage("minecraft.chat.player.account-unlinked"));
    }
}
