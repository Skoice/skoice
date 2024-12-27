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
import net.clementraynaud.skoice.common.model.minecraft.BasePlayer;
import net.clementraynaud.skoice.common.model.minecraft.SkoiceCommandSender;

public abstract class Argument {

    public static final String MANAGE_PERMISSION = "skoice.manage";

    protected final Skoice plugin;
    protected final SkoiceCommandSender sender;
    protected final boolean allowedInConsole;
    protected final boolean permissionRequired;
    protected final boolean hidden;

    protected Argument(Skoice plugin, SkoiceCommandSender sender, boolean allowedInConsole, boolean permissionRequired, boolean hidden) {
        this.plugin = plugin;
        this.sender = sender;
        this.allowedInConsole = allowedInConsole;
        this.permissionRequired = permissionRequired;
        this.hidden = hidden;
    }

    public abstract void run();

    public boolean canBeExecuted() {
        if (!(this.sender instanceof BasePlayer) && !this.allowedInConsole) {
            this.sender.sendMessage(this.plugin.getLang().getMessage("chat.error.illegal-executor"));
            return false;
        }
        if (this.permissionRequired && !this.sender.hasPermission(Argument.MANAGE_PERMISSION)) {
            this.sender.sendMessage(this.plugin.getLang().getMessage("chat.error.missing-permission"));
            return false;
        }
        return true;
    }
}
