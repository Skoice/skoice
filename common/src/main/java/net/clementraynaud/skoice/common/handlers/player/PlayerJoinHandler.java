/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.common.handlers.player;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.bot.BotStatus;
import net.clementraynaud.skoice.common.menus.selectors.LoginNotificationSelector;
import net.clementraynaud.skoice.common.model.minecraft.BasePlayer;
import net.clementraynaud.skoice.common.storage.LoginNotificationYamlFile;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.clementraynaud.skoice.common.system.LinkedPlayer;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

import java.util.List;

public class PlayerJoinHandler {

    private final Skoice plugin;

    public PlayerJoinHandler(Skoice plugin) {
        this.plugin = plugin;
    }

    public void onPlayerJoin(BasePlayer player, boolean chatAlert) {
        if (this.plugin.getBot().getStatus() != BotStatus.READY) {
            this.plugin.getBot().sendIncompleteConfigurationAlert(player, false, false);
        } else {
            if (!this.plugin.getLinksYamlFile().retrieveMember(player.getUniqueId(), member -> {
                new LinkedPlayer(this.plugin, this.plugin.getFullPlayer(player), member.getId());
                if (chatAlert) {
                    GuildVoiceState voiceState = member.getVoiceState();
                    if (voiceState != null) {
                        AudioChannel audioChannel = voiceState.getChannel();
                        if (audioChannel != null && audioChannel.equals(this.plugin.getConfigYamlFile().getVoiceChannel())) {
                            player.sendMessage(this.plugin.getLang().getMessage("chat.player.connected"));
                            this.callPlayerProximityConnectEvent(player.getUniqueId().toString(), member.getId());
                        }
                    }
                }
            }, e -> {
                if (chatAlert) {
                    this.sendLoginNotification(player);
                }
            })) {
                if (chatAlert) {
                    this.sendLoginNotification(player);
                }
            }
        }
    }

    protected void callPlayerProximityConnectEvent(String minecraftId, String memberId) {
    }

    private void sendLoginNotification(BasePlayer player) {
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
