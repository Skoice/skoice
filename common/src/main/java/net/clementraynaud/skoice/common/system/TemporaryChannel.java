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

import net.clementraynaud.skoice.common.Skoice;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

public abstract class TemporaryChannel {

    private final Skoice plugin;
    private boolean initialized = false;
    private String channelId;

    public TemporaryChannel(Skoice plugin) {
        this.plugin = plugin;

        this.initialize();
    }

    public TemporaryChannel(Skoice plugin, String channelId) {
        this.plugin = plugin;
        this.channelId = channelId;
        this.initialized = true;
    }

    public abstract void initialize();

    public String getChannelId() {
        return this.channelId;
    }

    public VoiceChannel getChannel() {
        if (this.channelId == null) {
            return null;
        }
        Guild guild = this.plugin.getBot().getGuild();
        if (guild != null) {
            return guild.getVoiceChannelById(this.channelId);
        }
        return null;
    }

    public boolean isInitialized() {
        return this.initialized;
    }
}
