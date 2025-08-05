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

package net.clementraynaud.skoice.common.system;

import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TemporaryChannelManager {

    private final Set<TemporaryChannel> temporaryChannels = ConcurrentHashMap.newKeySet();

    private TemporaryChannelManager() {
    }

    public Set<TemporaryChannel> getAll() {
        return this.temporaryChannels;
    }

    public Set<TemporaryChannel> getInitialized() {
        return this.temporaryChannels.stream()
                .filter(TemporaryChannel::isInitialized)
                .collect(Collectors.toSet());
    }

    public void add(TemporaryChannel temporaryChannel) {
        this.temporaryChannels.add(temporaryChannel);
    }

    public void delete(TemporaryChannel temporaryChannel) {
        VoiceChannel channel = temporaryChannel.getChannel();
        if (channel != null) {
            channel.delete().queue(null, new ErrorHandler().handle(ErrorResponse.UNKNOWN_CHANNEL, e ->
                    this.remove(temporaryChannel)
            ));
        } else {
            this.remove(temporaryChannel);
        }
    }

    public void remove(TemporaryChannel temporaryChannel) {
        this.temporaryChannels.remove(temporaryChannel);
    }

    public void remove(String channelId) {
        this.temporaryChannels.removeIf(temporaryChannel ->
                temporaryChannel.getChannelId().equals(channelId));
    }

    public boolean contains(String voiceChannelId) {
        return this.temporaryChannels.stream()
                .map(TemporaryChannel::getChannelId)
                .anyMatch(channelId -> channelId.equals(voiceChannelId));
    }

    public void clean(int possibleUsers, int possibleChannels, int extraChannels) {
        this.temporaryChannels.stream()
                .filter(temporaryChannel -> temporaryChannel.getChannel() != null
                        && temporaryChannel.getChannel().getMembers().isEmpty())
                .forEach(temporaryChannel -> {
                    if (this.temporaryChannels.size() > possibleChannels + extraChannels
                            || possibleUsers == 0) {
                        this.delete(temporaryChannel);
                    }
                });
    }

    public void clean(int possibleUsers) {
        this.clean(possibleUsers, possibleUsers, 0);
    }

    public void clear() {
        this.temporaryChannels.clear();
    }
}
