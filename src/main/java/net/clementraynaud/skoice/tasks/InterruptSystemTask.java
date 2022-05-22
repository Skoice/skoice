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

package net.clementraynaud.skoice.tasks;

import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.system.Network;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.concurrent.CompletableFuture;

public class InterruptSystemTask implements Task {

    private final Config config;

    public InterruptSystemTask(Config config) {
        this.config = config;
    }

    @Override
    public void run() {
        for (Pair<String, CompletableFuture<Void>> value : UpdateNetworksTask.awaitingMoves.values()) {
            value.getRight().cancel(true);
        }
        boolean isLobbySet = this.config.getLobby() != null;
        for (Network network : Network.networks) {
            if (isLobbySet) {
                for (int i = 0; i < network.getChannel().getMembers().size(); i++) {
                    Member member = network.getChannel().getMembers().get(i);
                    if (i + 1 < network.getChannel().getMembers().size()) {
                        member.getGuild().moveVoiceMember(member, this.config.getLobby()).queue();
                    } else {
                        member.getGuild().moveVoiceMember(member, this.config.getLobby()).complete();
                    }
                }
            }
            network.getChannel().delete().queue();
            network.clear();
        }
        Network.networks.clear();
    }
}
