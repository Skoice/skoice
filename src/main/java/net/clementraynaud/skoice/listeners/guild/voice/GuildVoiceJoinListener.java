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

import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.lang.Lang;
import net.clementraynaud.skoice.menus.MenuEmoji;
import net.clementraynaud.skoice.system.EligiblePlayers;
import net.clementraynaud.skoice.tasks.UpdateVoiceStateTask;
import net.clementraynaud.skoice.util.MapUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.awt.*;
import java.util.UUID;

public class GuildVoiceJoinListener extends ListenerAdapter {

    private final Config config;
    private final Lang lang;
    private final EligiblePlayers eligiblePlayers;

    public GuildVoiceJoinListener(Config config, Lang lang, EligiblePlayers eligiblePlayers) {
        this.config = config;
        this.lang = lang;
        this.eligiblePlayers = eligiblePlayers;
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        new UpdateVoiceStateTask(this.config, event.getMember(), event.getChannelJoined()).run();
        if (!event.getChannelJoined().equals(this.config.getReader().getLobby())) {
            return;
        }
        String minecraftID = new MapUtil().getKeyFromValue(this.config.getReader().getLinks(), event.getMember().getId());
        if (minecraftID == null) {
            event.getMember().getUser().openPrivateChannel().complete()
                    .sendMessageEmbeds(new EmbedBuilder().setTitle(MenuEmoji.LINK + this.lang.getMessage("discord.menu.linking-process.title"))
                            .addField(MenuEmoji.WARNING + this.lang.getMessage("discord.menu.linking-process.field.account-not-linked.title"),
                                    this.lang.getMessage("discord.menu.linking-process.field.account-not-linked.alternative-description", event.getGuild().getName()), false)
                            .setColor(Color.RED).build())
                    .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
        } else {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(minecraftID));
            if (player.isOnline() && player.getPlayer() != null) {
                this.eligiblePlayers.add(player.getUniqueId());
                player.getPlayer().sendMessage(this.lang.getMessage("minecraft.chat.player.connected-to-proximity-voice-chat"));
            }
        }
    }
}
