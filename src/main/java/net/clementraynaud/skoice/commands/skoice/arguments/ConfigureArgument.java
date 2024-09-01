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

package net.clementraynaud.skoice.commands.skoice.arguments;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfigureArgument extends Argument {

    public ConfigureArgument(Skoice plugin, CommandSender sender) {
        super(plugin, sender, ArgumentInfo.CONFIGURE.isAllowedInConsole(), ArgumentInfo.CONFIGURE.isPermissionRequired(), ArgumentInfo.CONFIGURE.isHidden());
    }

    @Override
    public void run() {
        Player player = (Player) this.sender;
        if (super.plugin.getBot().getStatus() == BotStatus.NOT_CONNECTED) {
            if (super.plugin.getConfigYamlFile().getBoolean(ConfigField.TOOLTIPS.toString())) {
                this.plugin.adventure().player(player).sendMessage(this.plugin.getLang().getMessage("chat.configuration.bot-creation-interactive", this.plugin.getLang().getComponentMessage("interaction.this-page")
                                        .hoverEvent(HoverEvent.showText(this.plugin.getLang().getComponentMessage("interaction.link", "https://github.com/Skoice/skoice/wiki/Creating-a-Discord-Bot-for-Skoice")))
                                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.openUrl("https://github.com/Skoice/skoice/wiki/Creating-a-Discord-Bot-for-Skoice")),
                                this.plugin.getLang().getComponentMessage("interaction.here")
                                        .hoverEvent(HoverEvent.showText(this.plugin.getLang().getComponentMessage("interaction.shortcut", "/skoice token")))
                                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.suggestCommand("/skoice token "))
                        )
                );
            } else {
                player.sendMessage(super.plugin.getLang().getMessage("chat.configuration.bot-creation",
                        "https://github.com/Skoice/skoice/wiki/Creating-a-Discord-Bot-for-Skoice")
                );
            }
        } else {
            if (super.plugin.getBot().getStatus() == BotStatus.READY) {
                player.sendMessage(super.plugin.getLang().getMessage("chat.configuration.already-configured"));
            } else if (super.plugin.getBot().getStatus() == BotStatus.NO_GUILD) {
                super.plugin.getBot().sendNoGuildAlert(player);
            } else {
                player.sendMessage(super.plugin.getLang().getMessage("chat.configuration.incomplete-configuration-operator-discord"));
            }
        }
    }
}
