/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

import net.clementraynaud.skoice.storage.config.ConfigYamlFile;
import net.clementraynaud.skoice.system.Network;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.concurrent.CompletableFuture;

public class InterruptSystemTask {

    private final ConfigYamlFile configYamlFile;

    public InterruptSystemTask(ConfigYamlFile configYamlFile) {
        this.configYamlFile = configYamlFile;
    }

    public void run() {
        for (Pair<String, CompletableFuture<Void>> value : UpdateNetworksTask.getAwaitingMoves().values()) {
            value.getRight().cancel(true);
        }
        boolean isVoiceChannelSet = this.configYamlFile.getVoiceChannel() != null;
        for (Network network : Network.getNetworks()) {
            if (isVoiceChannelSet) {
                for (int i = 0; i < network.getChannel().getMembers().size(); i++) {
                    Member member = network.getChannel().getMembers().get(i);
                    if (i + 1 < network.getChannel().getMembers().size()) {
                        member.getGuild().moveVoiceMember(member, this.configYamlFile.getVoiceChannel()).queue();
                    } else {
                        member.getGuild().moveVoiceMember(member, this.configYamlFile.getVoiceChannel()).complete();
                    }
                }
            }
            network.getChannel().delete().queue();
            network.clear();
        }
        Network.getNetworks().clear();
    }
}
