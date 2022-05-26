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
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.menus.MenuType;
import net.clementraynaud.skoice.tasks.UpdateVoiceStateTask;
import net.clementraynaud.skoice.util.MapUtil;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.UUID;

public class GuildVoiceJoinListener extends ListenerAdapter {

    private final Skoice plugin;

    public GuildVoiceJoinListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        new UpdateVoiceStateTask(this.plugin.readConfig(), event.getMember(), event.getChannelJoined()).run();
        if (!event.getChannelJoined().equals(this.plugin.readConfig().getLobby())) {
            return;
        }
        String minecraftID = MapUtil.getKeyFromValue(this.plugin.readConfig().getLinks(), event.getMember().getId());
        if (minecraftID == null) {
            event.getMember().getUser().openPrivateChannel().complete()
                    .sendMessage(new Menu(this.plugin, "linking-process",
                            Collections.singleton(this.plugin.getBot().getFields().get("account-not-linked")),
                            MenuType.ERROR)
                            .toMessage())
                    .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
        } else {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(minecraftID));
            if (player.isOnline() && player.getPlayer() != null) {
                this.plugin.getEligiblePlayers().add(player.getUniqueId());
                player.getPlayer().sendMessage(this.plugin.getLang().getMessage("minecraft.chat.player.connected-to-proximity-voice-chat"));
            }
        }
    }
}
