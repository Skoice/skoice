/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.menus.selectmenus.LoginNotificationSelectMenu;
import net.clementraynaud.skoice.storage.LoginNotificationYamlFile;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.clementraynaud.skoice.system.LinkedPlayer;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.kyori.adventure.text.event.HoverEvent;
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
            if (player.isOp()) {
                if (this.plugin.getBot().getStatus() == BotStatus.NOT_CONNECTED) {
                    if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.TOOLTIPS.toString())) {
                        this.plugin.adventure().player(player).sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.incomplete-configuration-operator-interactive", this.plugin.getLang().getComponentMessage("minecraft.interaction.here")
                                        .hoverEvent(HoverEvent.showText(this.plugin.getLang().getComponentMessage("minecraft.interaction.execute", "/skoice configure")))
                                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/skoice configure"))
                                )
                        );
                    } else {
                        player.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.incomplete-configuration-operator"));
                    }
                } else if (this.plugin.getBot().getStatus() != BotStatus.READY) {
                    if (this.plugin.getBot().getStatus() == BotStatus.NO_GUILD) {
                        this.plugin.getBot().sendNoGuildAlert(player);
                    } else {
                        player.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.incomplete-configuration-operator-discord"));
                    }
                }
            }
        } else {
            if (!this.plugin.getLinksYamlFile().retrieveMember(player.getUniqueId(), member -> {
                LinkedPlayer.getOnlineLinkedPlayers().add(new LinkedPlayer(this.plugin, player, member));
                GuildVoiceState voiceState = member.getVoiceState();
                if (voiceState != null) {
                    AudioChannel audioChannel = voiceState.getChannel();
                    if (audioChannel != null && audioChannel.equals(this.plugin.getConfigYamlFile().getVoiceChannel())) {
                        player.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.player.connected"));
                    }
                }
            }, e -> this.sendLoginNotification(player))) {
                this.sendLoginNotification(player);
            }
        }
    }

    private void sendLoginNotification(Player player) {
        String loginNotificationStatus = this.plugin.getConfigYamlFile().getString(ConfigField.LOGIN_NOTIFICATION.toString());
        if (LoginNotificationSelectMenu.ALWAYS_REMIND.equals(loginNotificationStatus)) {
            player.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.player.account-not-linked",
                    this.plugin.getBot().getGuild().getName()));
        } else if (LoginNotificationSelectMenu.REMIND_ONCE.equals(loginNotificationStatus)) {
            List<String> notifiedPlayers = this.plugin.getLoginNotificationYamlFile().getStringList(LoginNotificationYamlFile.NOTIFIED_PLAYERS_ID_FIELD);
            if (!notifiedPlayers.contains(player.getUniqueId().toString())) {
                notifiedPlayers.add(player.getUniqueId().toString());
                this.plugin.getLoginNotificationYamlFile().set(LoginNotificationYamlFile.NOTIFIED_PLAYERS_ID_FIELD, notifiedPlayers);
                player.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.player.account-not-linked-remind-once",
                        this.plugin.getBot().getGuild().getName()));
            }
        }
    }
}
