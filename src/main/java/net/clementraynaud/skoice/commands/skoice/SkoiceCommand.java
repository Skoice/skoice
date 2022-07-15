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

package net.clementraynaud.skoice.commands.skoice;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.commands.skoice.arguments.ArgumentInfo;
import net.clementraynaud.skoice.commands.skoice.arguments.ConfigureArgument;
import net.clementraynaud.skoice.commands.skoice.arguments.LinkArgument;
import net.clementraynaud.skoice.commands.skoice.arguments.TokenArgument;
import net.clementraynaud.skoice.commands.skoice.arguments.UnlinkArgument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SkoiceCommand implements CommandExecutor, TabCompleter {

    private final Skoice plugin;

    public SkoiceCommand(Skoice plugin) {
        this.plugin = plugin;
    }

    public void init() {
        PluginCommand skoiceCommand = this.plugin.getCommand("skoice");
        if (skoiceCommand != null) {
            skoiceCommand.setExecutor(this);
            skoiceCommand.setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                sender.sendMessage(this.plugin.getLang()
                        .getMessage("minecraft.chat.error.no-parameter", ArgumentInfo.getJoinedList(sender.isOp())));
            } else {
                sender.sendMessage(this.plugin.getLang()
                        .getMessage("minecraft.chat.error.no-parameter", ArgumentInfo.getJoinedConsoleAllowedList()));
            }
            return true;
        }
        if (ArgumentInfo.get(args[0]) == null) {
            if (sender instanceof Player) {
                sender.sendMessage(this.plugin.getLang()
                        .getMessage("minecraft.chat.error.invalid-parameter", ArgumentInfo.getJoinedList(sender.isOp())));
            } else {
                sender.sendMessage(this.plugin.getLang()
                        .getMessage("minecraft.chat.error.invalid-parameter", ArgumentInfo.getJoinedConsoleAllowedList()));
            }
            return true;
        }
        String arg = args.length > 1 ? args[1] : "";
        switch (ArgumentInfo.get(args[0])) {
            case CONFIGURE:
                new ConfigureArgument(this.plugin, sender).run();
                break;
            case TOKEN:
                new TokenArgument(this.plugin, sender, arg).run();
                break;
            case LINK:
                new LinkArgument(this.plugin, sender, arg).run();
                break;
            case UNLINK:
                new UnlinkArgument(this.plugin, sender).run();
                break;
            default:
                return true;
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 1) {
            return ArgumentInfo.getList(sender.isOp()).stream()
                    .filter(arg -> arg.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
