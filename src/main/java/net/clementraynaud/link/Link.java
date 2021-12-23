// Copyright 2020, 2021 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
// Copyright 2016, 2017, 2018, 2019, 2020, 2021 Austin "Scarsz" Shapiro

// This file is part of Skoice.

// Skoice is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Skoice is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Skoice.  If not, see <https://www.gnu.org/licenses/>.


package net.clementraynaud.link;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
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
import java.util.*;

import static net.clementraynaud.Bot.getJda;
import static net.clementraynaud.Skoice.getPlugin;
import static net.clementraynaud.util.DataGetters.getGuild;
import static net.clementraynaud.util.DataGetters.getKeyFromValue;

public class Link extends ListenerAdapter implements CommandExecutor {

    private static Map<String, String> discordIDCodeMap;

    public static void initializeDiscordIDCodeMap() {
        discordIDCodeMap = new HashMap<>();
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (event.getName().equals("link")) {
            EmbedBuilder embed = new EmbedBuilder().setTitle(":link: Linking Process");
            if (!getPlugin().isBotConfigured()) {
                event.replyEmbeds(embed.addField(":warning: Error", "Skoice is not configured correctly.", false)
                                .setColor(Color.RED).build())
                        .setEphemeral(true).queue();
                return;
            }
            boolean isLinked = getPlugin().getConfigFile().contains("link." + event.getUser().getId());
            if (isLinked) {
                event.replyEmbeds(embed.addField(":warning: Error", "Your Discord account is already linked to Minecraft.\nType `/unlink` to unlink it.", false)
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
            event.replyEmbeds(embed.addField(":key: Verification Code", "Type `/link " + code + "` in game to complete the process.", false)
                            .setColor(Color.GREEN).build())
                    .setEphemeral(true).queue();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§dSkoice §8• §7This command is §conly executable §7by players.");
            return true;
        }
        Player player = (Player) sender;
        if (!getPlugin().isBotConfigured() || getJda() == null) {
            player.sendMessage("§dSkoice §8• §7Skoice is §cnot configured correctly§7.");
            return true;
        }
        boolean isLinked = getPlugin().getConfigFile().contains("link." + player.getUniqueId());
        if (isLinked) {
            player.sendMessage("§dSkoice §8• §7You have §calready linked your Minecraft account §7to Discord. Type \"§e/unlink§7\" to unlink it.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("§dSkoice §8• §7You have §cnot provided a code§7. Type \"§e/link§7\" on our Discord server to receive a code.");
            return true;
        }
        if (!discordIDCodeMap.containsValue(args[0])) {
            player.sendMessage("§dSkoice §8• §7You have §cprovided an invalid code§7. Type \"§e/link§7\" on our Discord server to receive a code.");
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
                    .sendMessageEmbeds(new EmbedBuilder().setTitle(":link: Linking Process")
                            .addField(":heavy_check_mark: Account Linked", "Your Discord account has been linked to Minecraft.", false)
                            .setColor(Color.GREEN).build()).queue();
        } catch (ErrorResponseException e) {}
        player.sendMessage("§dSkoice §8• §7You have §alinked your Minecraft account §7to Discord.");
        return true;
    }
}
