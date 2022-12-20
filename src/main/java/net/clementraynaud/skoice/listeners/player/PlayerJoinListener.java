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

package net.clementraynaud.skoice.listeners.player;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.clementraynaud.skoice.tasks.UpdateNetworksTask;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

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
                if (!this.plugin.getConfigYamlFile().contains(ConfigField.TOKEN.toString()) || this.plugin.getBot().getJDA() == null) {
                    this.plugin.adventure().player(player).sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.incomplete-configuration-operator-interactive", this.plugin.getLang().getComponentMessage("minecraft.interaction.here")
                                    .hoverEvent(HoverEvent.showText(this.plugin.getLang().getComponentMessage("minecraft.interaction.execute", "/skoice configure")))
                                    .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/skoice configure"))
                            )
                    );
                } else if (this.plugin.getBot().getStatus() != BotStatus.READY) {
                    if (this.plugin.getBot().getStatus() == BotStatus.NO_GUILD) {
                        this.plugin.getBot().sendNoGuildAlert(player);
                    } else {
                        player.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.incomplete-configuration-operator-discord"));
                    }
                }
            }
        } else {
            UpdateNetworksTask.getEligiblePlayers().add(player.getUniqueId());
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                Member member = this.plugin.getLinksYamlFile().getMember(player.getUniqueId());
                if (member != null) {
                    GuildVoiceState voiceState = member.getVoiceState();
                    if (voiceState != null) {
                        AudioChannel audioChannel = voiceState.getChannel();
                        if (audioChannel != null && audioChannel.equals(this.plugin.getConfigYamlFile().getVoiceChannel())) {
                            player.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.player.connected"));
                        }
                    }
                }
            });
        }
    }
}
