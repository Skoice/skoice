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
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.util.MessageUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
        if (player.isOp()) {
            if (!this.plugin.readConfig().getFile().contains(ConfigField.TOKEN.get()) || this.plugin.getBot().getJda() == null) {
                try {
                    TextComponent configureCommand = new TextComponent(this.plugin.getLang().getMessage("minecraft.interaction.here"));
                    MessageUtil.setHoverEvent(configureCommand, this.plugin.getLang().getMessage("minecraft.interaction.execute", "/skoice configure"));
                    configureCommand.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skoice configure"));
                    player.spigot().sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.incomplete-configuration-operator-interactive", configureCommand));
                } catch (NoSuchMethodError e) {
                    player.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.incomplete-configuration-operator-command"));
                }
            } else if (!this.plugin.getBot().isReady()) {
                player.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.incomplete-configuration-operator-discord"));
            }
        }
    }
}
