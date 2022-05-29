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

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.system.Network;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnlinkArgument extends Argument {

    public UnlinkArgument(Skoice plugin, CommandSender sender) {
        super(plugin, sender, ArgumentInfo.UNLINK.isAllowedInConsole(), ArgumentInfo.UNLINK.isRestrictedToOperators());
    }

    @Override
    public void run() {
        if (!this.canExecuteCommand()) {
            return;
        }
        Player player = (Player) this.sender;
        String discordId = super.plugin.getConfiguration().getLinks().get(player.getUniqueId().toString());
        if (discordId == null) {
            player.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.player.account-not-linked"));
            return;
        }
        super.plugin.getConfiguration().unlinkUser(player.getUniqueId().toString());
        Member member;
        try {
            member = super.plugin.getConfiguration().getGuild().retrieveMemberById(discordId).complete();
            member.getUser().openPrivateChannel().complete()
                    .sendMessage(this.plugin.getBot().getMenus().get("account-unlinked").toMessage())
                    .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
            GuildVoiceState voiceState = member.getVoiceState();
            if (voiceState != null) {
                VoiceChannel voiceChannel = voiceState.getChannel();
                if (voiceChannel != null && voiceChannel.equals(super.plugin.getConfiguration().getLobby())
                        || Network.getNetworks().stream().anyMatch(network -> network.getChannel().equals(voiceChannel))) {
                    player.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.player.disconnected-from-proximity-voice-chat"));
                }
            }
        } catch (ErrorResponseException ignored) {
        }
        player.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.player.account-unlinked"));
    }
}
