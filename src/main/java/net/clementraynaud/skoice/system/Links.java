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

package net.clementraynaud.skoice.system;

import net.clementraynaud.skoice.Skoice;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Links {

    public static final String FIELD = "links";

    private File linksFile;
    private final YamlConfiguration file = new YamlConfiguration();

    private final Skoice plugin;

    public Links(Skoice plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.linksFile = new File(this.plugin.getDataFolder() + File.separator + "links.yml");
        if (!this.linksFile.exists()) {
            this.plugin.saveResource("links.yml", false);
        }
        try {
            this.file.load(this.linksFile);
        } catch (IOException | InvalidConfigurationException ignored) {
        }
    }

    public FileConfiguration getFile() {
        return this.file;
    }

    public void saveFile() {
        try {
            this.file.save(this.linksFile);
        } catch (IOException ignored) {
        }
    }

    public void linkUser(String minecraftId, String discordId) {
        this.file.set(Links.FIELD + "." + minecraftId, discordId);
        this.saveFile();
    }

    public void unlinkUser(String minecraftId) {
        this.file.set(Links.FIELD + "." + minecraftId, null);
        this.saveFile();
    }

    public Map<String, String> getMap() {
        Map<String, String> castedLinks = new HashMap<>();
        if (this.file.isSet(Links.FIELD)) {
            Map<String, Object> links = new HashMap<>(this.file.getConfigurationSection(Links.FIELD)
                    .getValues(false));
            for (Map.Entry<String, Object> entry : links.entrySet()) {
                castedLinks.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return castedLinks;
    }

    public Member getMember(UUID minecraftId) {
        String discordId = this.getMap().get(minecraftId.toString());
        if (discordId == null) {
            return null;
        }
        Guild guild = this.plugin.getConfiguration().getGuild();
        if (guild == null) {
            return null;
        }
        Member member = null;
        try {
            member = guild.retrieveMemberById(discordId).complete();
        } catch (ErrorResponseException ignored) {
        }
        return member;
    }
}
