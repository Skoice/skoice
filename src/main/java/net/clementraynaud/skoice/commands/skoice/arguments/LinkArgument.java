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

import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.commands.LinkCommand;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.lang.Lang;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.clementraynaud.skoice.util.MapUtil;
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

    private final Bot bot;
    private final String arg;

    public LinkArgument(Config config, Lang lang, CommandSender sender, Bot bot, String arg) {
        super(config, lang, sender, ArgumentName.LINK.isAllowedInConsole(), ArgumentName.LINK.isRestrictedToOperators());
        this.bot = bot;
        this.arg = arg;
    }

    @Override
    public void run() {
        if (!this.canExecuteCommand()) {
            return;
        }
        Player player = (Player) this.sender;
        if (!this.bot.isReady() || this.bot.getJda() == null) {
            player.sendMessage(super.lang.getMessage("minecraft.chat.configuration.incomplete-configuration"));
            return;
        }
        if (this.config.getReader().getLinks().containsKey(player.getUniqueId().toString())) {
            player.sendMessage(super.lang.getMessage("minecraft.chat.player.account-already-linked"));
            return;
        }
        if (this.arg.isEmpty()) {
            player.sendMessage(super.lang.getMessage("minecraft.chat.player.no-code"));
            return;
        }
        if (!LinkCommand.getDiscordIDCode().containsValue(this.arg)) {
            player.sendMessage(super.lang.getMessage("minecraft.chat.player.invalid-code"));
            return;
        }
        String discordID = new MapUtil().getKeyFromValue(LinkCommand.getDiscordIDCode(), this.arg);
        if (discordID == null) {
            return;
        }
        Member member = this.config.getReader().getGuild().getMemberById(discordID);
        if (member == null) {
            return;
        }
        this.config.getUpdater().linkUser(player.getUniqueId().toString(), discordID);
        LinkCommand.removeValueFromDiscordIDCode(this.arg);
        member.getUser().openPrivateChannel().complete()
                .sendMessageEmbeds(new EmbedBuilder().setTitle(MenuEmoji.LINK + super.lang.getMessage("discord.menu.linking-process.title"))
                        .addField(MenuEmoji.HEAVY_CHECK_MARK + super.lang.getMessage("discord.menu.linking-process.field.account-linked.title"),
                                super.lang.getMessage("discord.menu.linking-process.field.account-linked.description"), false)
                        .setColor(Color.GREEN).build())
                .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
        player.sendMessage(super.lang.getMessage("minecraft.chat.player.account-linked"));
        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState != null) {
            VoiceChannel voiceChannel = voiceState.getChannel();
            if (voiceChannel != null && voiceChannel.equals(this.config.getReader().getLobby())) {
                player.sendMessage(super.lang.getMessage("minecraft.chat.player.connected-to-proximity-voice-chat"));
            }
        }
    }

}
