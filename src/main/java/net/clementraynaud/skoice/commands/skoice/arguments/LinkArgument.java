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
import net.clementraynaud.skoice.commands.LinkCommand;
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.menus.MenuType;
import net.clementraynaud.skoice.util.MapUtil;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class LinkArgument extends Argument {

    private final String arg;

    public LinkArgument(Skoice plugin, CommandSender sender, String arg) {
        super(plugin, sender, ArgumentInfo.LINK.isAllowedInConsole(), ArgumentInfo.LINK.isRestrictedToOperators());
        this.arg = arg;
    }

    @Override
    public void run() {
        if (!this.canExecuteCommand()) {
            return;
        }
        Player player = (Player) this.sender;
        if (!super.plugin.getBot().isReady() || super.plugin.getBot().getJda() == null) {
            player.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.configuration.incomplete-configuration"));
            return;
        }
        if (super.plugin.readConfig().getLinks().containsKey(player.getUniqueId().toString())) {
            player.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.player.account-already-linked"));
            return;
        }
        if (this.arg.isEmpty()) {
            player.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.player.no-code"));
            return;
        }
        if (!LinkCommand.getDiscordIdCode().containsValue(this.arg)) {
            player.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.player.invalid-code"));
            return;
        }
        String discordId = MapUtil.getKeyFromValue(LinkCommand.getDiscordIdCode(), this.arg);
        if (discordId == null) {
            return;
        }
        Member member = super.plugin.readConfig().getGuild().getMemberById(discordId);
        if (member == null) {
            return;
        }
        super.plugin.readConfig().linkUser(player.getUniqueId().toString(), discordId);
        LinkCommand.getDiscordIdCode().values().remove(this.arg);
        member.getUser().openPrivateChannel().complete()
                .sendMessage(new Menu(super.plugin, "linking-process",
                        Collections.singleton(super.plugin.getBot().getFields().get("account-linked")),
                        MenuType.SUCCESS)
                        .toMessage())
                .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
        player.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.player.account-linked"));
        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState != null) {
            VoiceChannel voiceChannel = voiceState.getChannel();
            if (voiceChannel != null && voiceChannel.equals(super.plugin.readConfig().getLobby())) {
                player.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.player.connected-to-proximity-voice-chat"));
            }
        }
    }

}
