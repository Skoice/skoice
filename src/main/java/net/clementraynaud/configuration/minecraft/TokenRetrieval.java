// Copyright 2020, 2021 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
// Copyright 2016, 2017, 2018, 2019, 2020, 2021 Austin "Scarsz" Shapiro

// This file is part of Skoice.

// Skoice is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Skoice is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Skoice.  If not, see <https://www.gnu.org/licenses/>.


package net.clementraynaud.configuration.minecraft;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Base64;

import static net.clementraynaud.bot.Connection.getJda;
import static net.clementraynaud.Skoice.getBot;
import static net.clementraynaud.Skoice.getPlugin;

public class TokenRetrieval implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!sender.isOp()) {
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("§dSkoice §8• §7You have §cnot provided a token§7.");
            return true;
        }
        if (args[0].length() != 59 || !args[0].matches("[a-zA-Z0-9_.]+")) {
            sender.sendMessage("§dSkoice §8• §7You have §cnot provided a valid token§7.");
            return true;
        }
        byte[] tokenBytes = args[0].getBytes();
        for (int i = 0; i < tokenBytes.length; i++) {
            tokenBytes[i]++;
        }
        String base64Token = Base64.getEncoder().encodeToString(tokenBytes);
        getPlugin().getConfigFile().set("token", base64Token);
        getPlugin().saveConfig();
        if (getJda() == null) {
            getBot().connectBot(sender);
        } else {
            sender.sendMessage("§dSkoice §8• §7A bot is §calready connected§7. Restart your Minecraft server to apply the new token.");
        }
        return true;
    }
}
