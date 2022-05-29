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

package net.clementraynaud.skoice.commands;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.util.MapUtil;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class UnlinkCommand extends ListenerAdapter {

    private final Skoice plugin;

    public UnlinkCommand(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if ("unlink".equals(event.getName())) {
            String minecraftId = MapUtil.getKeyFromValue(this.plugin.getConfiguration().getLinks(), event.getUser().getId());
            if (minecraftId == null) {
                event.reply(this.plugin.getBot().getMenu("account-not-linked").toMessage()).setEphemeral(true).queue();
            } else {
                this.plugin.getConfiguration().unlinkUser(minecraftId);
                event.reply(this.plugin.getBot().getMenu("account-unlinked").toMessage()).setEphemeral(true).queue();
                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(minecraftId));
                if (player.isOnline() && player.getPlayer() != null) {
                    player.getPlayer().sendMessage(this.plugin.getLang().getMessage("minecraft.chat.player.account-unlinked"));
                    GuildVoiceState voiceState = event.getMember().getVoiceState();
                    if (voiceState != null) {
                        VoiceChannel voiceChannel = voiceState.getChannel();
                        if (voiceChannel != null && voiceChannel.equals(this.plugin.getConfiguration().getLobby())) {
                            player.getPlayer().sendMessage(this.plugin.getLang().getMessage("minecraft.chat.player.disconnected-from-proximity-voice-chat"));
                        }
                    }
                }
            }
        }
    }
}
