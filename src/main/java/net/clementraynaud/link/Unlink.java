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
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.UUID;

import static net.clementraynaud.Skoice.getPlugin;
import static net.clementraynaud.util.DataGetters.getGuild;

public class Unlink extends ListenerAdapter implements CommandExecutor {

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (event.getName().equals("unlink")) {
            EmbedBuilder embed = new EmbedBuilder().setTitle(":link: Linking Process");
            String minecraftID = getPlugin().getConfigFile().getString("Data." + event.getUser().getId());
            if (minecraftID == null) {
                event.replyEmbeds(embed.addField(":warning: Error", "Your Discord account is not linked to Minecraft.\nType `/link` to link it.", false)
                                .setColor(Color.RED).build())
                        .setEphemeral(true).queue();
            } else {
                unlinkUser(event.getUser().getId(), minecraftID);
                event.replyEmbeds(embed.addField(":heavy_check_mark: Account Unlinked", "Your Discord account has been unlinked from Minecraft.", false)
                                .setColor(Color.GREEN).build())
                        .setEphemeral(true).queue();
                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(minecraftID));
                if (player.isOnline()) {
                    player.getPlayer().sendMessage("§dSkoice §8• §7You have §aunlinked your Minecraft account §7from Discord.");
                }
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§dSkoice §8• §7This command is §conly executable §7by players.");
            return true;
        }
        Player player = (Player) sender;
        String discordID = getPlugin().getConfigFile().getString("Data." + player.getUniqueId());
        if (discordID == null) {
            player.sendMessage("§dSkoice §8• §7You have §cnot linked your Minecraft account §7to Discord. Type \"§e/link§7\" on our Discord server to link it.");
            return true;
        }
        unlinkUser(discordID, player.getUniqueId().toString());
        try {
            getGuild().retrieveMemberById(discordID).complete().getUser().openPrivateChannel().complete()
                    .sendMessageEmbeds(new EmbedBuilder().setTitle(":link: Linking Process")
                            .addField(":heavy_check_mark: Account Unlinked", "Your Discord account has been unlinked from Minecraft.", false)
                            .setColor(Color.GREEN).build()).queue();
        } catch (ErrorResponseException e) {
            getPlugin().getLogger().warning("A player attempted to unlink but his Discord account was not found.");
        }
        player.sendMessage("§dSkoice §8• §7You have §aunlinked your Minecraft account §7from Discord.");
        return true;
    }

    private void unlinkUser(String discordID, String minecraftID) {
        getPlugin().getConfigFile().set("link." + minecraftID, null);
        getPlugin().getConfigFile().set("link." + discordID, null);
        getPlugin().saveConfig();
    }
}
