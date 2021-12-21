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


package net.clementraynaud.system;

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static net.clementraynaud.system.ChannelManagement.refreshMutedUsers;
import static net.clementraynaud.util.DataGetters.getLobby;
import static net.clementraynaud.util.DataGetters.getMinecraftID;

public class MarkPlayersDirty extends ListenerAdapter implements Listener {

    private static Set<UUID> dirtyPlayers = new HashSet<>();

    public static Set<UUID> getDirtyPlayers() {
        return dirtyPlayers;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        markDirty(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        markDirty(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        markDirty(player);
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        refreshMutedUsers(event.getChannelJoined(), event.getMember());
        if (!event.getChannelJoined().equals(getLobby())) return;
        UUID minecraftID = getMinecraftID(event.getMember());
        if (minecraftID == null)
            return;
        OfflinePlayer player = Bukkit.getOfflinePlayer(minecraftID);
        if (player.isOnline())
            markDirty(player.getPlayer());
    }

    public void markDirty(Player player) {
        dirtyPlayers.add(player.getUniqueId());
    }

    public static void clearDirtyPlayers() {
        dirtyPlayers = new HashSet<>();
    }
}
