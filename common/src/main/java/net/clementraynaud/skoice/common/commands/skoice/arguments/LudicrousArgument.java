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

package net.clementraynaud.skoice.common.commands.skoice.arguments;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.bot.BotStatus;
import net.clementraynaud.skoice.common.lang.LangInfo;
import net.clementraynaud.skoice.common.model.minecraft.SkoiceCommandSender;
import net.clementraynaud.skoice.common.storage.config.ConfigField;

public class LudicrousArgument extends Argument {

    public LudicrousArgument(Skoice plugin, SkoiceCommandSender sender) {
        super(plugin, sender, ArgumentInfo.LUDICROUS.isAllowedInConsole(), ArgumentInfo.LUDICROUS.isPermissionRequired(), ArgumentInfo.LUDICROUS.isHidden());
    }

    @Override
    public void run() {
        if (super.plugin.getConfigYamlFile().getBoolean(ConfigField.LUDICROUS.toString())) {
            super.plugin.getConfigYamlFile().set(ConfigField.LUDICROUS.toString(), false);
            this.sender.sendMessage(super.plugin.getLang().getMessage("chat.configuration.ludicrous-disabled"));
        } else {
            super.plugin.getConfigYamlFile().set(ConfigField.LUDICROUS.toString(), true);
            this.sender.sendMessage(super.plugin.getLang().getMessage("chat.configuration.ludicrous-enabled"));
        }
    }

}
