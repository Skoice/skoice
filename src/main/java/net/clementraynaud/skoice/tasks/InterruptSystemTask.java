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

package net.clementraynaud.skoice.tasks;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.system.Network;
import net.clementraynaud.skoice.system.Networks;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.concurrent.CompletableFuture;

public class InterruptSystemTask {

    private final Skoice plugin;

    public InterruptSystemTask(Skoice plugin) {
        this.plugin = plugin;
    }

    public void run() {
        for (Pair<String, CompletableFuture<Void>> value : UpdateNetworksTask.getAwaitingMoves().values()) {
            value.getRight().cancel(true);
        }

        boolean isMainVoiceChannelSet = this.plugin.getConfigYamlFile().getVoiceChannel() != null;
        for (Network network : Networks.getInitialized()) {
            if (isMainVoiceChannelSet) {
                for (int i = 0; i < network.getChannel().getMembers().size(); i++) {
                    Member member = network.getChannel().getMembers().get(i);
                    if (i + 1 < network.getChannel().getMembers().size()) {
                        member.getGuild().moveVoiceMember(member, this.plugin.getConfigYamlFile().getVoiceChannel()).queue();
                    } else {
                        member.getGuild().moveVoiceMember(member, this.plugin.getConfigYamlFile().getVoiceChannel()).complete();
                    }
                }
            }
            network.clear();
            network.delete("system-interrupted");
        }

        Networks.clear();
    }
}
