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

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.commands.LinkCommand;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.lang.MinecraftLang;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;

public class LinkArgument extends Argument {

    public LinkArgument(CommandSender sender, String arg) {
        super(sender, arg, false, false);
    }

    @Override
    public void run() {
        if (!this.canExecuteCommand()) {
            return;
        }
        Player player = (Player) this.sender;
        if (!Skoice.getPlugin().isBotReady() || Bot.getJda() == null) {
            player.sendMessage(MinecraftLang.INCOMPLETE_CONFIGURATION.toString());
            return;
        }
        if (Config.getLinkMap().containsKey(player.getUniqueId().toString())) {
            player.sendMessage(MinecraftLang.ACCOUNT_ALREADY_LINKED.toString());
            return;
        }
        if (this.arg.isEmpty()) {
            player.sendMessage(MinecraftLang.NO_CODE.toString());
            return;
        }
        if (!LinkCommand.getDiscordIDCode().containsValue(this.arg)) {
            player.sendMessage(MinecraftLang.INVALID_CODE.toString());
            return;
        }
        String discordID = Config.getKeyFromValue(LinkCommand.getDiscordIDCode(), this.arg);
        if (discordID == null) {
            return;
        }
        Member member = Config.getGuild().getMemberById(discordID);
        if (member == null) {
            return;
        }
        Config.linkUser(player.getUniqueId().toString(), discordID);
        LinkCommand.removeValueFromDiscordIDCode(this.arg);
        member.getUser().openPrivateChannel().complete()
                .sendMessageEmbeds(new EmbedBuilder().setTitle(MenuEmoji.LINK + DiscordLang.LINKING_PROCESS_EMBED_TITLE.toString())
                        .addField(MenuEmoji.HEAVY_CHECK_MARK + DiscordLang.ACCOUNT_LINKED_FIELD_TITLE.toString(),
                                DiscordLang.ACCOUNT_LINKED_FIELD_DESCRIPTION.toString(), false)
                        .setColor(Color.GREEN).build())
                .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
        player.sendMessage(MinecraftLang.ACCOUNT_LINKED.toString());
        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState != null) {
            VoiceChannel voiceChannel = voiceState.getChannel();
            if (voiceChannel != null && voiceChannel.equals(Config.getLobby())) {
                player.sendMessage(MinecraftLang.CONNECTED_TO_PROXIMITY_VOICE_CHAT.toString());
            }
        }
    }

}
