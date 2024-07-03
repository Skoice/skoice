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

package net.clementraynaud.skoice.storage.config;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.storage.YamlFile;
import net.clementraynaud.skoice.util.ConfigurationUtil;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.WorldInfo;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigYamlFile extends YamlFile {

    public ConfigYamlFile(Skoice plugin) {
        super(plugin, "config");
    }

    public void saveDefaultValues() {
        YamlConfiguration defaultConfiguration = ConfigurationUtil.loadResource(this.getClass().getName(), "config.yml");
        if (defaultConfiguration == null) {
            return;
        }
        Map<String, Object> defaultValues = new HashMap<>(defaultConfiguration.getValues(false));
        for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
            this.setDefault(entry.getKey(), entry.getValue());
        }
        if (!this.contains(ConfigField.ACTIVE_WORLDS.toString())) {
            this.set(ConfigField.ACTIVE_WORLDS.toString(),
                    this.plugin.getServer().getWorlds().stream().map(WorldInfo::getName).collect(Collectors.toList()));
        }
    }

    public void setToken(String token) {
        byte[] tokenBytes = token.getBytes();
        for (int i = 0; i < tokenBytes.length; i++) {
            tokenBytes[i]++;
        }
        this.set(ConfigField.TOKEN.toString(), Base64.getEncoder().encodeToString(tokenBytes));
    }

    public VoiceChannel getVoiceChannel() {
        if (this.plugin.getBot().getJDA() == null) {
            return null;
        }
        String voiceChannelId = this.getString(ConfigField.VOICE_CHANNEL_ID.toString());
        if (voiceChannelId == null) {
            return null;
        }
        VoiceChannel voiceChannel = this.plugin.getBot().getJDA().getVoiceChannelById(voiceChannelId);
        return voiceChannel != null && voiceChannel.getParentCategory() != null ? voiceChannel : null;
    }

    public void removeInvalidVoiceChannelId() {
        if (this.getVoiceChannel() == null
                && this.contains(ConfigField.VOICE_CHANNEL_ID.toString())) {
            this.remove(ConfigField.VOICE_CHANNEL_ID.toString());
        }
    }

    public Category getCategory() {
        if (this.plugin.getBot().getStatus() == BotStatus.NOT_CONNECTED) {
            return null;
        }
        VoiceChannel voiceChannel = this.getVoiceChannel();
        return voiceChannel != null ? voiceChannel.getParentCategory() : null;
    }
}
