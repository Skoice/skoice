/*
 * Copyright 2020, 2021, 2022 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class GuildVoiceMoveListener extends ListenerAdapter {

    private final Skoice plugin;

    public GuildVoiceMoveListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        new UpdateVoiceStateTask(this.plugin.getConfiguration(), event.getMember(), event.getChannelJoined()).run();
        if (event.getChannelJoined().getParent() != null && !event.getChannelJoined().getParent().equals(this.plugin.getConfiguration().getCategory())
                && event.getChannelLeft().getParent() != null && event.getChannelLeft().getParent().equals(this.plugin.getConfiguration().getCategory())) {
            String minecraftId = MapUtil.getKeyFromValue(this.plugin.getConfiguration().getLinks(), event.getMember().getId());
            if (minecraftId == null) {
                return;
            }
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(minecraftId));
            if (player.isOnline() && player.getPlayer() != null) {
                Network.getNetworks().stream()
                        .filter(network -> network.contains(player.getPlayer().getUniqueId()))
                        .forEach(network -> network.remove(player.getPlayer()));
            }
        } else if (event.getChannelJoined().equals(this.plugin.getConfiguration().getLobby())
                && Network.getNetworks().stream().noneMatch(network -> network.getChannel().equals(event.getChannelLeft()))) {
            this.plugin.getBot().checkMemberStatus(event.getMember());
        }
    }
}
