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

package net.clementraynaud.skoice.spigot.commands.skoice;

import net.clementraynaud.skoice.common.commands.skoice.SkoiceCommand;
import net.clementraynaud.skoice.spigot.SkoicePluginSpigot;
import net.clementraynaud.skoice.spigot.SkoiceSpigot;
import net.clementraynaud.skoice.spigot.minecraft.SpigotBasePlayer;
import net.clementraynaud.skoice.spigot.minecraft.SpigotCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SkoiceCommandSpigot extends SkoiceCommand implements CommandExecutor, TabCompleter {

    private final SkoiceSpigot plugin;

    public SkoiceCommandSpigot(SkoiceSpigot plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public void init() {
        PluginCommand skoiceCommand = this.plugin.getPlugin().getCommand("skoice");
        if (skoiceCommand != null) {
            skoiceCommand.setExecutor(this);
            skoiceCommand.setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (SkoicePluginSpigot.isProxyMode()) {
            sender.sendMessage("Proxy mode enabled.");
            return true;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            return super.onCommand(new SpigotBasePlayer(player), args);
        } else {
            return super.onCommand(new SpigotCommandSender(sender), args);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (SkoicePluginSpigot.isProxyMode()) {
            return new ArrayList<>();
        }
        return super.onTabComplete(new SpigotCommandSender(sender), args);
    }
}
