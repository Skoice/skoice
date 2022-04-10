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

import net.clementraynaud.skoice.bot.Bot;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConfigReader {

    private final Config config;
    private final Bot bot;

    public ConfigReader(Config config, Bot bot) {
        this.config = config;
        this.bot = bot;
    }

    public Map<String, String> getLinkMap() {
        Map<String, String> castedLinkMap = new HashMap<>();
        if (this.config.getFile().isSet(ConfigField.LINK_MAP.get())) {
            Map<String, Object> linkMap = new HashMap<>(this.config.getFile().getConfigurationSection(ConfigField.LINK_MAP.get()).getValues(false));
            for (Map.Entry<String, Object> entry : linkMap.entrySet()) {
                castedLinkMap.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return castedLinkMap;
    }

    public Member getMember(UUID minecraftID) {
        String discordID = this.getLinkMap().get(minecraftID);
        Guild guild = this.getGuild();
        if (guild == null) {
            return null;
        }
        return discordID != null ? guild.getMemberById(discordID) : null;
    }

    public VoiceChannel getLobby() {
        if (this.bot.getJda() == null) {
            return null;
        }
        String lobbyID = this.config.getFile().getString(ConfigField.LOBBY_ID.get());
        if (lobbyID == null) {
            return null;
        }
        VoiceChannel lobby = this.bot.getJda().getVoiceChannelById(lobbyID);
        return lobby != null && lobby.getParent() != null ? lobby : null;
    }

    public Category getCategory() {
        if (this.bot.getJda() == null) {
            return null;
        }
        VoiceChannel lobby = this.getLobby();
        return lobby != null ? lobby.getParent() : null;
    }

    public Guild getGuild() {
        VoiceChannel lobby = this.getLobby();
        return lobby != null ? lobby.getGuild() : null;
    }
}
