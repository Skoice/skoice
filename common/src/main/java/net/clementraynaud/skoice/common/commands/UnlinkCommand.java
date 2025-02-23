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

package net.clementraynaud.skoice.common.commands;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.menus.EmbeddedMenu;
import net.clementraynaud.skoice.common.model.minecraft.BasePlayer;
import net.clementraynaud.skoice.common.util.MapUtil;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.UUID;

public class UnlinkCommand extends Command {

    public UnlinkCommand(Skoice plugin, CommandExecutor executor, SlashCommandInteraction interaction) {
        super(plugin, executor, CommandInfo.UNLINK.isServerManagerRequired(), CommandInfo.UNLINK.isBotReadyRequired(), interaction);
    }

    @Override
    public void run() {
        String minecraftId = MapUtil.getKeyFromValue(super.plugin.getLinksYamlFile().getLinks(), super.executor.getUser().getId());
        if (minecraftId == null) {
            new EmbeddedMenu(super.plugin.getBot()).setContent("account-not-linked")
                    .reply(super.interaction);
            return;
        }

        super.plugin.getLinksYamlFile().unlinkUser(minecraftId);
        new EmbeddedMenu(super.plugin.getBot()).setContent("account-unlinked")
                .reply(super.interaction);

        BasePlayer player = super.plugin.getPlayer(UUID.fromString(minecraftId));
        if (player != null) {
            player.sendMessage(super.plugin.getLang().getMessage("chat.player.account-unlinked"));

            super.plugin.getBot().getGuild().retrieveMember(super.executor.getUser()).queue(member -> {
                GuildVoiceState voiceState = member.getVoiceState();
                if (voiceState != null) {
                    AudioChannel voiceChannel = voiceState.getChannel();
                    if (voiceChannel != null && voiceChannel.equals(super.plugin.getConfigYamlFile().getVoiceChannel())) {
                        player.sendMessage(super.plugin.getLang().getMessage("chat.player.disconnected"));
                    }
                }
            }, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MEMBER));
        }
    }
}
