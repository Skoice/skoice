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

package net.clementraynaud.skoice.common.storage;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.api.events.account.AccountLinkEvent;
import net.clementraynaud.skoice.common.api.events.account.AccountUnlinkEvent;
import net.clementraynaud.skoice.common.api.events.player.PlayerProximityConnectEvent;
import net.clementraynaud.skoice.common.api.events.player.PlayerProximityDisconnectEvent;
import net.clementraynaud.skoice.common.bot.BotStatus;
import net.clementraynaud.skoice.common.model.minecraft.BasePlayer;
import net.clementraynaud.skoice.common.model.minecraft.FullPlayer;
import net.clementraynaud.skoice.common.system.LinkedPlayer;
import net.clementraynaud.skoice.common.system.Networks;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.simpleyaml.configuration.ConfigurationSection;

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
        this.additionalLinkProcessing(minecraftId, discordId);
        Skoice.eventBus().fireAsync(new AccountLinkEvent(minecraftId, discordId));
    }

    protected void additionalLinkProcessing(String minecraftId, String discordId) {
    }

    public void unlinkUser(String minecraftId) {
        this.unlinkUserDirectly(minecraftId);
        this.additionalUnlinkProcessing(minecraftId);
        Skoice.eventBus().fireAsync(new AccountUnlinkEvent(minecraftId));
    }

    protected void additionalUnlinkProcessing(String minecraftId) {
    }

    public void linkUserDirectly(String minecraftId, String discordId) {
        super.set(LinksYamlFile.LINKS_FIELD + "." + minecraftId, discordId);
        BasePlayer player = this.plugin.getPlayer(UUID.fromString(minecraftId));
        if (player == null) {
            return;
        }
        this.plugin.getScheduler().runTaskAsynchronously(() -> new LinkedPlayer(this.plugin, this.plugin.getFullPlayer(player), discordId));
        if (this.plugin.getBot().getStatus() == BotStatus.READY) {
            this.plugin.getBot().getGuild().retrieveMemberById(discordId).queue(member -> {
                VoiceChannel mainVoiceChannel = super.plugin.getConfigYamlFile().getVoiceChannel();
                GuildVoiceState voiceState = member.getVoiceState();
                if (voiceState != null) {
                    AudioChannel audioChannel = voiceState.getChannel();
                    if (audioChannel != null && audioChannel.equals(this.plugin.getConfigYamlFile().getVoiceChannel())) {
                        player.sendMessage(this.plugin.getLang().getMessage("chat.player.connected"));
                        Skoice.eventBus().fireAsync(new PlayerProximityConnectEvent(minecraftId, discordId));
                    } else {
                        player.sendMessage(super.plugin.getLang().getMessage("chat.player.not-connected",
                                mainVoiceChannel.getName(),
                                this.plugin.getBot().getGuild().getName()));
                    }
                }
            });
        }
    }

    public void unlinkUserDirectly(String minecraftId) {
        super.remove(LinksYamlFile.LINKS_FIELD + "." + minecraftId);

        BasePlayer player = this.plugin.getPlayer(UUID.fromString(minecraftId));
        if (player == null) {
            return;
        }
        this.plugin.getScheduler().runTaskAsynchronously(() -> {
            Networks.getAll().stream()
                    .filter(network -> network.contains(this.plugin.getFullPlayer(player)))
                    .findFirst().ifPresent(playerNetwork -> playerNetwork.remove(this.plugin.getFullPlayer(player)));

            LinkedPlayer.getOnlineLinkedPlayers().removeIf(p -> p.getFullPlayer().equals(player));
        });
        if (Skoice.api().isProximityConnected(UUID.fromString(minecraftId))) {
            Skoice.eventBus().fireAsync(new PlayerProximityDisconnectEvent(minecraftId));
        }
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

    public void refreshOnlineLinkedPlayers() {
        LinkedPlayer.getOnlineLinkedPlayers().clear();

        for (FullPlayer player : this.plugin.getOnlinePlayers()) {
            this.retrieveMember(player.getUniqueId(),
                    member -> new LinkedPlayer(this.plugin, player, member.getId()));
        }
    }
}
