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
import net.clementraynaud.skoice.model.minecraft.SkoiceCommandSender;

public class ArgumentFactory {

    public Argument getArgument(Skoice plugin, ArgumentInfo argumentInfo, SkoiceCommandSender sender, String arg) {
        switch (argumentInfo) {
            case CONFIGURE:
                return new ConfigureArgument(plugin, sender);
            case TOOLTIPS:
                return new TooltipsArgument(plugin, sender);
            case TOKEN:
                return new TokenArgument(plugin, sender, arg);
            case LANGUAGE:
                return new LanguageArgument(plugin, sender, arg);
            case LINK:
                return new LinkArgument(plugin, sender, arg);
            case UNLINK:
                return new UnlinkArgument(plugin, sender);
            default:
                return null;
        }
    }
}
