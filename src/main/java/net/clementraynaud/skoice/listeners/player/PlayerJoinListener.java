/*
 * Copyright 2020, 2021, 2022, 2023, 2024 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.listeners.player;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.api.events.player.PlayerProximityConnectEvent;
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.menus.selectors.LoginNotificationSelector;
import net.clementraynaud.skoice.storage.LoginNotificationYamlFile;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.clementraynaud.skoice.system.LinkedPlayer;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class PlayerJoinListener implements Listener {

    private final Skoice plugin;

    public PlayerJoinListener(Skoice plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (this.plugin.getBot().getStatus() != BotStatus.READY) {
            this.plugin.getBot().sendIncompleteConfigurationAlert(player, false, false);
        } else {
            if (!this.plugin.getLinksYamlFile().retrieveMember(player.getUniqueId(), member -> {
                new LinkedPlayer(this.plugin, player, member.getId());
                GuildVoiceState voiceState = member.getVoiceState();
                if (voiceState != null) {
                    AudioChannel audioChannel = voiceState.getChannel();
                    if (audioChannel != null && audioChannel.equals(this.plugin.getConfigYamlFile().getVoiceChannel())) {
                        player.sendMessage(this.plugin.getLang().getMessage("chat.player.connected"));
                        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                            PlayerProximityConnectEvent connectEvent = new PlayerProximityConnectEvent(player.getUniqueId().toString(), member.getId());
                            this.plugin.getServer().getPluginManager().callEvent(connectEvent);
                        });
                    }
                }
            }, e -> this.sendLoginNotification(player))) {
                this.sendLoginNotification(player);
            }
        }
    }

    private void sendLoginNotification(Player player) {
        String loginNotificationStatus = this.plugin.getConfigYamlFile().getString(ConfigField.LOGIN_NOTIFICATION.toString());
        if (LoginNotificationSelector.ALWAYS_REMIND.equals(loginNotificationStatus)) {
            player.sendMessage(this.plugin.getLang().getMessage("chat.player.account-not-linked",
                    this.plugin.getBot().getGuild().getName()));
        } else if (LoginNotificationSelector.REMIND_ONCE.equals(loginNotificationStatus)) {
            List<String> notifiedPlayers = this.plugin.getLoginNotificationYamlFile().getStringList(LoginNotificationYamlFile.NOTIFIED_PLAYERS_ID_FIELD);
            if (!notifiedPlayers.contains(player.getUniqueId().toString())) {
                notifiedPlayers.add(player.getUniqueId().toString());
                this.plugin.getLoginNotificationYamlFile().set(LoginNotificationYamlFile.NOTIFIED_PLAYERS_ID_FIELD, notifiedPlayers);
                player.sendMessage(this.plugin.getLang().getMessage("chat.player.account-not-linked-remind-once",
                        this.plugin.getBot().getGuild().getName()));
            }
        }
    }
}
