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

package net.clementraynaud.skoice.velocity.commands.skoice;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.commands.skoice.SkoiceCommand;
import net.clementraynaud.skoice.velocity.SkoiceVelocity;
import net.clementraynaud.skoice.velocity.minecraft.VelocityBasePlayer;
import net.clementraynaud.skoice.velocity.minecraft.VelocityCommandSender;

import java.util.List;

public class SkoiceCommandVelocity extends SkoiceCommand implements SimpleCommand {

    private final SkoiceVelocity plugin;

    public SkoiceCommandVelocity(Skoice plugin) {
        super(plugin);
        this.plugin = (SkoiceVelocity) plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (invocation.source() instanceof Player player) {
            super.onCommand(new VelocityBasePlayer(player), invocation.arguments());

        } else {
            super.onCommand(new VelocityCommandSender(invocation.source()), invocation.arguments());
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return super.onTabComplete(new VelocityCommandSender(invocation.source()), invocation.arguments());
    }

    @Override
    public void init() {
        CommandManager commandManager = this.plugin.getPlugin().getProxy().getCommandManager();
        commandManager.register(commandManager.metaBuilder("skoice")
                .aliases("skoice:skoice")
                .plugin(this.plugin.getPlugin())
                .build(), this);
    }
}
