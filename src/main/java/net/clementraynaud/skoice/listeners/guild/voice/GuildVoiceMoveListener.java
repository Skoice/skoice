/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.listeners.guild.voice;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.system.Network;
import net.clementraynaud.skoice.tasks.UpdateVoiceStateTask;
import net.clementraynaud.skoice.util.MapUtil;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class GuildVoiceMoveListener extends ListenerAdapter {

    private final Skoice plugin;

    public GuildVoiceMoveListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        if (event.getChannelJoined().getType() != ChannelType.VOICE) {
            return;
        }
        VoiceChannel voiceChannelJoined = (VoiceChannel) event.getChannelJoined();
        new UpdateVoiceStateTask(this.plugin, event.getMember(), voiceChannelJoined).run();

        if (event.getChannelLeft().getType() != ChannelType.VOICE) {
            return;
        }
        VoiceChannel voiceChannelLeft = (VoiceChannel) event.getChannelLeft();

        VoiceChannel mainVoiceChannel = this.plugin.getConfigYamlFile().getVoiceChannel();

        if (Network.getNetworks().stream().noneMatch(network -> network.getChannel().equals(voiceChannelJoined))) {
            String minecraftId = MapUtil.getKeyFromValue(this.plugin.getLinksYamlFile().getLinks(), event.getMember().getId());
            if (minecraftId == null) {
                return;
            }
            OfflinePlayer player = this.plugin.getServer().getOfflinePlayer(UUID.fromString(minecraftId));
            if (player.isOnline() && player.getPlayer() != null) {
                if (Network.getNetworks().stream().anyMatch(network -> network.getChannel().equals(voiceChannelLeft))) {
                    Network.getNetworks().stream()
                            .filter(network -> network.contains(player.getPlayer().getUniqueId()))
                            .forEach(network -> network.remove(player.getPlayer()));
                    if (!voiceChannelJoined.equals(mainVoiceChannel)) {
                        player.getPlayer().sendMessage(this.plugin.getLang().getMessage("minecraft.chat.player.disconnected"));
                    }
                } else if (voiceChannelLeft.equals(mainVoiceChannel)) {
                    player.getPlayer().sendMessage(this.plugin.getLang().getMessage("minecraft.chat.player.disconnected"));
                }
            }
        }

        if (voiceChannelJoined.equals(mainVoiceChannel)
                && Network.getNetworks().stream().noneMatch(network -> network.getChannel().equals(voiceChannelLeft))) {
            this.plugin.getBot().checkMemberStatus(event.getMember());
        }
    }
}
