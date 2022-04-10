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

package net.clementraynaud.skoice.commands.skoice.arguments;

import net.clementraynaud.skoice.Skoice;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Argument extends Skoice {

    public enum Option {
        CONFIGURE {
            @Override
            public void run(CommandSender sender, String arg) {
                new ConfigureArgument(sender).run();
            }
        },

        TOKEN {
            @Override
            public void run(CommandSender sender, String arg) {
                new TokenArgument(sender, arg).run();
            }
        },

        LINK {
            @Override
            public void run(CommandSender sender, String arg) {
                new LinkArgument(sender, arg).run();
            }
        },

        UNLINK {
            @Override
            public void run(CommandSender sender, String arg) {
                new UnlinkArgument(sender).run();
            }
        };

        public abstract void run(CommandSender sender, String arg);

        public static Option get(String option) {
            return Stream.of(Option.values())
                    .filter(value -> value.toString().equalsIgnoreCase(option))
                    .findFirst()
                    .orElse(null);
        }

        public static Collection<String> getList() {
            return Stream.of(Option.values())
                    .map(Enum::name)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
        }
    }

    protected final CommandSender sender;
    protected final String arg;
    protected final boolean allowsConsole;
    protected final boolean restrictedToOperators;

    protected Argument(CommandSender sender, String arg, boolean allowsConsole, boolean restrictedToOperators) {
        this.sender = sender;
        this.arg = arg;
        this.allowsConsole = allowsConsole;
        this.restrictedToOperators = restrictedToOperators;
    }

    protected abstract void run();

    protected boolean canExecuteCommand() {
        if (!(this.sender instanceof Player) && !this.allowsConsole) {
            this.sender.sendMessage(super.lang.getMessage("minecraft.chat.error.illegal-executor"));
            return false;
        }
        if (!this.sender.isOp() && this.restrictedToOperators) {
            this.sender.sendMessage(super.lang.getMessage("minecraft.chat.error.missing-permission"));
            return false;
        }
        return true;
    }
}
