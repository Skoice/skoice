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

import net.clementraynaud.skoice.lang.Discord;
import net.clementraynaud.skoice.lang.Minecraft;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.awt.*;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.bot.Bot.getJda;
import static net.clementraynaud.skoice.commands.LinkCommand.getDiscordIDCode;
import static net.clementraynaud.skoice.commands.LinkCommand.removeValueFromDiscordIDCode;
import static net.clementraynaud.skoice.config.Config.*;

public class LinkArgument {

    public void execute(CommandSender sender, String arg) {
        Player player = (Player) sender;
        if (!getPlugin().isBotReady() || getJda() == null) {
            player.sendMessage(Minecraft.INCOMPLETE_CONFIGURATION.toString());
            return;
        }
        boolean isLinked = getPlugin().getConfig().contains("link." + player.getUniqueId());
        if (isLinked) {
            player.sendMessage(Minecraft.ACCOUNT_ALREADY_LINKED.toString());
            return;
        }
        if (arg == null) {
            player.sendMessage(Minecraft.NO_CODE.toString());
            return;
        }
        if (!getDiscordIDCode().containsValue(arg)) {
            player.sendMessage(Minecraft.INVALID_CODE.toString());
            return;
        }
        String discordID = getKeyFromValue(getDiscordIDCode(), arg);
        if (discordID == null) {
            return;
        }
        Member member = getGuild().getMemberById(discordID);
        if (member == null) {
            return;
        }
        getPlugin().getConfig().set("link." + player.getUniqueId(), discordID);
        getPlugin().getConfig().set("link." + discordID, player.getUniqueId().toString());
        getPlugin().saveConfig();
        removeValueFromDiscordIDCode(arg);
        try {
            member.getUser().openPrivateChannel().complete()
                    .sendMessageEmbeds(new EmbedBuilder().setTitle(":link: " + Discord.LINKING_PROCESS_EMBED_TITLE)
                            .addField(":heavy_check_mark: " + Discord.ACCOUNT_LINKED_FIELD_TITLE, Discord.ACCOUNT_LINKED_FIELD_DESCRIPTION.toString(), false)
                            .setColor(Color.GREEN).build()).queue(success -> {
                    }, failure -> {
                    });
        } catch (ErrorResponseException ignored) {
        }
        player.sendMessage(Minecraft.ACCOUNT_LINKED.toString());
        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState != null) {
            VoiceChannel voiceChannel = voiceState.getChannel();
            if (voiceChannel != null && voiceChannel.equals(getLobby())) {
                player.sendMessage(Minecraft.CONNECTED_TO_PROXIMITY_VOICE_CHAT.toString());
            }
        }
    }

}
