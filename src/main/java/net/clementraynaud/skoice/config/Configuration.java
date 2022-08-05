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

package net.clementraynaud.skoice.config;

import net.clementraynaud.skoice.Skoice;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Base64;

public class Configuration extends YamlConfiguration {

    private final Skoice plugin;

    public Configuration(Skoice plugin) {
        this.plugin = plugin;
    }

    public void init() {
        this.setDefaults(this.plugin.getConfig());
        this.options().copyDefaults(true);
        this.save();
    }

    public void save() {
        this.plugin.saveConfig();
    }

    public void setToken(String token) {
        byte[] tokenBytes = token.getBytes();
        for (int i = 0; i < tokenBytes.length; i++) {
            tokenBytes[i]++;
        }
        this.set(ConfigurationField.TOKEN.toString(), Base64.getEncoder().encodeToString(tokenBytes));
        this.save();
    }

    public VoiceChannel getVoiceChannel() {
        if (this.plugin.getBot().getJDA() == null) {
            return null;
        }
        String voiceChannelId = this.getString(ConfigurationField.VOICE_CHANNEL_ID.toString());
        if (voiceChannelId == null) {
            return null;
        }
        VoiceChannel voiceChannel = this.plugin.getBot().getJDA().getVoiceChannelById(voiceChannelId);
        return voiceChannel != null && voiceChannel.getParentCategory() != null ? voiceChannel : null;
    }

    public void eraseInvalidVoiceChannelId() {
        if (this.getVoiceChannel() == null
                && this.contains(ConfigurationField.VOICE_CHANNEL_ID.toString())) {
            this.set(ConfigurationField.VOICE_CHANNEL_ID.toString(), null);
            this.save();
        }
    }

    public Category getCategory() {
        if (this.plugin.getBot().getJDA() == null) {
            return null;
        }
        VoiceChannel voiceChannel = this.getVoiceChannel();
        return voiceChannel != null ? voiceChannel.getParentCategory() : null;
    }
}
