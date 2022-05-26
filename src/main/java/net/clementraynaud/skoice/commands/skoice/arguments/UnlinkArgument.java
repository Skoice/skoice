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
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.menus.MenuType;
import net.clementraynaud.skoice.system.Network;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

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
        String discordID = super.plugin.readConfig().getLinks().get(player.getUniqueId().toString());
        if (discordID == null) {
            player.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.player.account-not-linked"));
            return;
        }
        super.plugin.readConfig().unlinkUser(player.getUniqueId().toString());
        Member member;
        try {
            member = super.plugin.readConfig().getGuild().retrieveMemberById(discordID).complete();
            member.getUser().openPrivateChannel().complete()
                    .sendMessage(new Menu(super.plugin, "linking-process",
                            Collections.singleton(super.plugin.getBot().getFields().get("account-unlinked")),
                            MenuType.SUCCESS)
                            .toMessage())
                    .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
            GuildVoiceState voiceState = member.getVoiceState();
            if (voiceState != null) {
                VoiceChannel voiceChannel = voiceState.getChannel();
                if (voiceChannel != null && voiceChannel.equals(super.plugin.readConfig().getLobby())
                        || Network.getNetworks().stream().anyMatch(network -> network.getChannel().equals(voiceChannel))) {
                    player.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.player.disconnected-from-proximity-voice-chat"));
                }
            }
        } catch (ErrorResponseException ignored) {
        }
        player.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.player.account-unlinked"));
    }
}
