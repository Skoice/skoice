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

package net.clementraynaud.skoice.storage;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.api.events.account.AccountLinkEvent;
import net.clementraynaud.skoice.api.events.account.AccountUnlinkEvent;
import net.clementraynaud.skoice.system.LinkedPlayer;
import net.clementraynaud.skoice.system.Networks;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class LinksYamlFile extends YamlFile {

    public static final String LINKS_FIELD = "links";

    public LinksYamlFile(Skoice plugin) {
        super(plugin, "links");
    }

    public void linkUser(String minecraftId, String discordId) {
        this.linkUserDirectly(minecraftId, discordId);
        super.plugin.getDiscordSRVHook().linkUser(minecraftId, discordId);
//        AccountLinkEvent event = new AccountLinkEvent(minecraftId, discordId);
//        this.plugin.getServer().getPluginManager().callEvent(event);
    }

    public void unlinkUser(String minecraftId) {
        this.unlinkUserDirectly(minecraftId);
        super.plugin.getDiscordSRVHook().unlinkUser(minecraftId);
//        AccountUnlinkEvent event = new AccountUnlinkEvent(minecraftId);
//        this.plugin.getServer().getPluginManager().callEvent(event);
    }

    public void linkUserDirectly(String minecraftId, String discordId) {
        super.set(LinksYamlFile.LINKS_FIELD + "." + minecraftId, discordId);
        Player player = this.plugin.getServer().getPlayer(UUID.fromString(minecraftId));
        if (player != null) {
            LinkedPlayer.getOnlineLinkedPlayers().add(new LinkedPlayer(this.plugin, player, discordId));
        }
    }

    public void unlinkUserDirectly(String minecraftId) {
        super.remove(LinksYamlFile.LINKS_FIELD + "." + minecraftId);

        Player player = this.plugin.getServer().getPlayer(UUID.fromString(minecraftId));
        if (player == null) {
            return;
        }
        Networks.getAll().stream()
                .filter(network -> network.contains(player))
                .findFirst().ifPresent(playerNetwork -> playerNetwork.remove(player));

        LinkedPlayer.getOnlineLinkedPlayers().removeIf(p -> p.getBukkitPlayer().equals(player));
    }

    public Map<String, String> getLinks() {
        Map<String, String> castedLinks = new HashMap<>();
        ConfigurationSection linksSection = super.getConfigurationSection(LinksYamlFile.LINKS_FIELD);
        if (linksSection != null) {
            Map<String, Object> links = new HashMap<>(linksSection.getValues(false));
            for (Map.Entry<String, Object> entry : links.entrySet()) {
                castedLinks.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return castedLinks;
    }

    public boolean retrieveMember(UUID minecraftId, Consumer<Member> success, Consumer<ErrorResponseException> failure) {
        String discordId = this.getLinks().get(minecraftId.toString());
        if (discordId == null) {
            return false;
        }

        Guild guild = super.plugin.getBot().getGuild();
        if (guild == null) {
            return false;
        }

        guild.retrieveMemberById(discordId).queue(
                success,
                failure != null
                        ? new ErrorHandler().handle(ErrorResponse.UNKNOWN_MEMBER, failure)
                        : new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MEMBER)
        );
        return true;
    }

    public boolean retrieveMember(UUID minecraftId, Consumer<Member> success) {
        return this.retrieveMember(minecraftId, success, null);
    }
}
