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
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Config {

    private final Skoice plugin;
    private final FileConfiguration file;

    public Config(Skoice plugin) {
        this.plugin = plugin;
        this.file = this.plugin.getConfig();
    }

    public FileConfiguration getFile() {
        return this.file;
    }

    public void saveFile() {
        this.plugin.saveConfig();
    }

    public void setToken(String token) {
        byte[] tokenBytes = token.getBytes();
        for (int i = 0; i < tokenBytes.length; i++) {
            tokenBytes[i]++;
        }
        this.file.set(ConfigField.TOKEN.get(), Base64.getEncoder().encodeToString(tokenBytes));
        this.saveFile();
    }

    public void linkUser(String minecraftID, String discordID) {
        this.file.set(ConfigField.LINKS.get() + "." + minecraftID, discordID);
        this.saveFile();
    }

    public void unlinkUser(String minecraftID) {
        this.file.set(ConfigField.LINKS.get() + "." + minecraftID, null);
        this.saveFile();
    }

    public Map<String, String> getLinks() {
        Map<String, String> castedLinks = new HashMap<>();
        if (this.file.isSet(ConfigField.LINKS.get())) {
            Map<String, Object> links = new HashMap<>(this.file.getConfigurationSection(ConfigField.LINKS.get()).getValues(false));
            for (Map.Entry<String, Object> entry : links.entrySet()) {
                castedLinks.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return castedLinks;
    }

    public Member getMember(UUID minecraftID) {
        String discordID = this.getLinks().get(minecraftID.toString());
        if (discordID == null) {
            return null;
        }
        Guild guild = this.getGuild();
        if (guild == null) {
            return null;
        }
        Member member = null;
        try {
            member = guild.retrieveMemberById(discordID).complete();
        } catch (ErrorResponseException ignored) {
        }
        return member;
    }

    public VoiceChannel getLobby() {
        if (this.plugin.getBot().getJda() == null) {
            return null;
        }
        String lobbyID = this.file.getString(ConfigField.LOBBY_ID.get());
        if (lobbyID == null) {
            return null;
        }
        VoiceChannel lobby = this.plugin.getBot().getJda().getVoiceChannelById(lobbyID);
        return lobby != null && lobby.getParent() != null ? lobby : null;
    }

    public Category getCategory() {
        if (this.plugin.getBot().getJda() == null) {
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
