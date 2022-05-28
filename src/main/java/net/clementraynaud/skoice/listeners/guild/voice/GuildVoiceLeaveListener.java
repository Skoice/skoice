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
import net.clementraynaud.skoice.util.MapUtil;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class GuildVoiceLeaveListener extends ListenerAdapter {

    private final Skoice plugin;

    public GuildVoiceLeaveListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (event.getChannelLeft().getParent() == null
                || !event.getChannelLeft().getParent().equals(this.plugin.readConfig().getCategory())) {
            return;
        }
        String minecraftId = MapUtil.getKeyFromValue(this.plugin.readConfig().getLinks(), event.getMember().getId());
        if (minecraftId == null) {
            return;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(minecraftId));
        if (player.isOnline() && player.getPlayer() != null) {
            Network.getNetworks().stream()
                    .filter(network -> network.contains(player.getPlayer()))
                    .forEach(network -> network.remove(player.getPlayer()));
            if (event.getChannelLeft().equals(this.plugin.readConfig().getLobby())
                    || Network.getNetworks().stream().anyMatch(network -> network.getChannel().equals(event.getChannelLeft()))) {
                player.getPlayer().sendMessage(this.plugin.getLang().getMessage("minecraft.chat.player.disconnected-from-proximity-voice-chat"));
            }
        }
    }
}
