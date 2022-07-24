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

package net.clementraynaud.skoice.storage;

import net.clementraynaud.skoice.Skoice;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LinksFileStorage extends FileStorage {

    public static final String LINKS_FIELD = "links";

    public LinksFileStorage(Skoice plugin) {
        super(plugin, "links");
    }

    public void linkUser(String minecraftId, String discordId) {
        super.yaml.set(LinksFileStorage.LINKS_FIELD + "." + minecraftId, discordId);
        this.saveFile();
    }

    public void unlinkUser(String minecraftId) {
        super.yaml.set(LinksFileStorage.LINKS_FIELD + "." + minecraftId, null);
        this.saveFile();
    }

    public Map<String, String> getLinks() {
        Map<String, String> castedLinks = new HashMap<>();
        ConfigurationSection linksSection = super.yaml.getConfigurationSection(LinksFileStorage.LINKS_FIELD);
        if (linksSection != null) {
            Map<String, Object> links = new HashMap<>(linksSection.getValues(false));
            for (Map.Entry<String, Object> entry : links.entrySet()) {
                castedLinks.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return castedLinks;
    }

    public Member getMember(UUID minecraftId) {
        String discordId = this.getLinks().get(minecraftId.toString());
        if (discordId == null) {
            return null;
        }
        Guild guild = super.plugin.getBot().getGuild();
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
