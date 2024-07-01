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

package net.clementraynaud.skoice.commands;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.api.events.player.PlayerProximityDisconnectEvent;
import net.clementraynaud.skoice.menus.EmbeddedMenu;
import net.clementraynaud.skoice.util.MapUtil;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.entity.Player;

import java.util.UUID;

public class UnlinkCommand extends Command {

    public UnlinkCommand(Skoice plugin, CommandExecutor executor, SlashCommandInteraction interaction) {
        super(plugin, executor, CommandInfo.UNLINK.isServerManagerRequired(), CommandInfo.UNLINK.isBotReadyRequired(), interaction);
    }

    @Override
    public void run() {
        String minecraftId = MapUtil.getKeyFromValue(super.plugin.getLinksYamlFile().getLinks(), super.executor.getUser().getId());
        if (minecraftId == null) {
            new EmbeddedMenu(super.plugin.getBot()).setContent("account-not-linked",
                            super.plugin.getBotCommands().getAsMention(CommandInfo.LINK.toString()))
                    .reply(super.interaction);
            return;
        }

        super.plugin.getLinksYamlFile().unlinkUser(minecraftId);
        new EmbeddedMenu(super.plugin.getBot()).setContent("account-unlinked")
                .reply(super.interaction);

        Player player = super.plugin.getServer().getPlayer(UUID.fromString(minecraftId));
        if (player != null) {
            player.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.player.account-unlinked"));

            super.plugin.getBot().getGuild().retrieveMember(super.executor.getUser()).queue(member -> {
                GuildVoiceState voiceState = member.getVoiceState();
                if (voiceState != null) {
                    AudioChannel voiceChannel = voiceState.getChannel();
                    if (voiceChannel != null && voiceChannel.equals(super.plugin.getConfigYamlFile().getVoiceChannel())) {
                        player.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.player.disconnected"));
                        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                            PlayerProximityDisconnectEvent event = new PlayerProximityDisconnectEvent(minecraftId, member.getId());
                            this.plugin.getServer().getPluginManager().callEvent(event);
                        });
                    }
                }
            }, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MEMBER));
        }
    }
}
