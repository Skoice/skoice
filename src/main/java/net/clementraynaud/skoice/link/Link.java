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
import org.apache.commons.lang3.RandomStringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.bot.Connection.*;
import static net.clementraynaud.skoice.util.DataGetters.*;

public class Link extends ListenerAdapter implements CommandExecutor {

    private static Map<String, String> discordIDCodeMap;

    public static void initializeDiscordIDCodeMap() {
        discordIDCodeMap = new HashMap<>();
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (event.getName().equals("link")) {
            if (!getPlugin().isBotReady()) {
                EmbedBuilder embed = new EmbedBuilder().setTitle(":gear: " + Lang.Discord.CONFIGURATION_EMBED_TITLE.print());
                event.replyEmbeds(embed.addField(":warning: " + Lang.Discord.INCOMPLETE_CONFIGURATION_FIELD_TITLE, Lang.Discord.INCOMPLETE_CONFIGURATION_FIELD_DESCRIPTION.print(), false)
                                .setColor(Color.RED).build())
                        .setEphemeral(true).queue();
                return;
            }
            EmbedBuilder embed = new EmbedBuilder().setTitle(":link: " + Lang.Discord.LINKING_PROCESS_EMBED_TITLE.print());
            boolean isLinked = getPlugin().getConfigFile().contains("link." + event.getUser().getId());
            if (isLinked) {
                event.replyEmbeds(embed.addField(":warning: " + Lang.Discord.ACCOUNT_ALREADY_LINKED_FIELD_TITLE.print(), Lang.Discord.ACCOUNT_ALREADY_LINKED_FIELD_DESCRIPTION.print(), false)
                                .setColor(Color.RED).build())
                        .setEphemeral(true).queue();
                return;
            }
            discordIDCodeMap.remove(event.getUser().getId());
            String code;
            do {
                code = RandomStringUtils.randomAlphanumeric(10).toUpperCase();
            } while (discordIDCodeMap.containsValue(code));
            discordIDCodeMap.put(event.getUser().getId(), code);
            event.replyEmbeds(embed.addField(":key: " + Lang.Discord.VERIFICATION_CODE_FIELD_TITLE.print(), Lang.Discord.VERIFICATION_CODE_FIELD_DESCRIPTION.print().replace("{code}", code), false)
                            .setColor(Color.GREEN).build())
                    .setEphemeral(true).queue();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Lang.Minecraft.ILLEGAL_EXECUTOR.print());
            return true;
        }
        Player player = (Player) sender;
        if (!getPlugin().isBotReady() || getJda() == null) {
            player.sendMessage(Lang.Minecraft.INCOMPLETE_CONFIGURATION.print());
            return true;
        }
        boolean isLinked = getPlugin().getConfigFile().contains("link." + player.getUniqueId());
        if (isLinked) {
            player.sendMessage(Lang.Minecraft.ACCOUNT_ALREADY_LINKED.print());
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Lang.Minecraft.NO_CODE.print());
            return true;
        }
        if (!discordIDCodeMap.containsValue(args[0])) {
            player.sendMessage(Lang.Minecraft.INVALID_CODE.print());
            return true;
        }
        String discordID = getKeyFromValue(discordIDCodeMap, args[0]);
        if (discordID == null) {
            return true;
        }
        Member member = getGuild().getMemberById(discordID);
        if (member == null) {
            return true;
        }
        getPlugin().getConfigFile().set("link." + player.getUniqueId(), discordID);
        getPlugin().getConfigFile().set("link." + discordID, player.getUniqueId().toString());
        getPlugin().saveConfig();
        discordIDCodeMap.values().remove(args[0]);
        try {
            member.getUser().openPrivateChannel().complete()
                    .sendMessageEmbeds(new EmbedBuilder().setTitle(":link: " + Lang.Discord.LINKING_PROCESS_EMBED_TITLE.print())
                            .addField(":heavy_check_mark: " + Lang.Discord.ACCOUNT_LINKED_FIELD_TITLE.print(), Lang.Discord.ACCOUNT_LINKED_FIELD_DESCRIPTION.print(), false)
                            .setColor(Color.GREEN).build()).queue(success -> {}, failure -> {});
        } catch (ErrorResponseException ignored) {
        }
        player.sendMessage(Lang.Minecraft.ACCOUNT_LINKED.print());
        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState != null) {
            VoiceChannel voiceChannel = voiceState.getChannel();
            if (voiceChannel != null && voiceChannel.equals(getLobby())) {
                player.sendMessage(Lang.Minecraft.CONNECTED_TO_PROXIMITY_VOICE_CHAT.print());
            }
        }
        return true;
    }
}
