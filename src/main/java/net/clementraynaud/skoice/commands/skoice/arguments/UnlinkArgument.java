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

package net.clementraynaud.skoice.commands.skoice.arguments;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.menus.EmbeddedMenu;
import net.clementraynaud.skoice.model.minecraft.BasePlayer;
import net.clementraynaud.skoice.model.minecraft.SkoiceCommandSender;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.clementraynaud.skoice.system.ProximityChannels;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class UnlinkArgument extends Argument {

    public UnlinkArgument(Skoice plugin, SkoiceCommandSender sender) {
        super(plugin, sender, ArgumentInfo.UNLINK.isAllowedInConsole(), ArgumentInfo.UNLINK.isPermissionRequired(), ArgumentInfo.UNLINK.isHidden());
    }

    @Override
    public void run() {
        BasePlayer player = (BasePlayer) this.sender;
        if (super.plugin.getBot().getStatus() != BotStatus.READY) {
            super.plugin.getBot().sendIncompleteConfigurationAlert(player, true, false);
            return;
        }

        String discordId = super.plugin.getLinksYamlFile().getLinks().get(player.getUniqueId().toString());
        if (discordId == null) {
            player.sendMessage(super.plugin.getLang().getMessage("chat.player.account-not-linked"));
            return;
        }

        super.plugin.getLinksYamlFile().unlinkUser(player.getUniqueId().toString());
        super.plugin.getBot().getGuild().retrieveMemberById(discordId).queue(member -> {
            new EmbeddedMenu(this.plugin.getBot()).setContent("account-unlinked").message(member.getUser());
            GuildVoiceState voiceState = member.getVoiceState();
            if (voiceState != null) {
                AudioChannel audioChannel = voiceState.getChannel();
                if (audioChannel != null
                        && (audioChannel.getId().equals(super.plugin.getConfigYamlFile().getString(ConfigField.VOICE_CHANNEL_ID.toString()))
                        || ProximityChannels.getInitialized().stream().anyMatch(proximityChannel -> proximityChannel.getChannelId().equals(audioChannel.getId())))) {
                    player.sendMessage(super.plugin.getLang().getMessage("chat.player.disconnected"));
                }
            }
        }, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MEMBER));
        player.sendMessage(super.plugin.getLang().getMessage("chat.player.account-unlinked"));
    }
}
