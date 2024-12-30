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

package net.clementraynaud.skoice.common.commands.skoice.arguments;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.bot.BotStatus;
import net.clementraynaud.skoice.common.model.minecraft.BasePlayer;
import net.clementraynaud.skoice.common.model.minecraft.SkoiceCommandSender;
import net.clementraynaud.skoice.common.storage.config.ConfigField;

public class ConfigureArgument extends Argument {

    public ConfigureArgument(Skoice plugin, SkoiceCommandSender sender) {
        super(plugin, sender, ArgumentInfo.CONFIGURE.isAllowedInConsole(), ArgumentInfo.CONFIGURE.isPermissionRequired(), ArgumentInfo.CONFIGURE.isHidden());
    }

    @Override
    public void run() {
        BasePlayer player = (BasePlayer) this.sender;
        if (super.plugin.getBot().getStatus() == BotStatus.NOT_CONNECTED) {
            if (super.plugin.getConfigYamlFile().getBoolean(ConfigField.TOOLTIPS.toString())) {
                this.plugin.getScheduler().runTaskAsynchronously(() -> {
                    player.sendMessage(this.plugin.getLang()
                            .getInteractiveMessage("chat.configuration.bot-creation-interactive"));
                });
            } else {
                player.sendMessage(super.plugin.getLang().getMessage("chat.configuration.bot-creation"));
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
