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
import net.clementraynaud.skoice.commands.LinkCommand;
import net.clementraynaud.skoice.menus.EmbeddedMenu;
import net.clementraynaud.skoice.util.MapUtil;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LinkArgument extends Argument {

    private final String arg;

    public LinkArgument(Skoice plugin, CommandSender sender, String arg) {
        super(plugin, sender, ArgumentInfo.LINK.isAllowedInConsole(), ArgumentInfo.LINK.isPermissionRequired(), ArgumentInfo.LINK.isHidden());
        this.arg = arg;
    }

    @Override
    public void run() {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            Player player = (Player) this.sender;
            if (super.plugin.getBot().getStatus() != BotStatus.READY) {
                super.plugin.getBot().sendIncompleteConfigurationAlert(player, true, false);
                return;
            }
            if (super.plugin.getLinksYamlFile().getLinks().containsKey(player.getUniqueId().toString())) {
                player.sendMessage(super.plugin.getLang().getMessage("chat.player.account-already-linked"));
                return;
            }
            if (this.arg.isEmpty()) {
                player.sendMessage(super.plugin.getLang().getMessage("chat.player.no-code",
                        super.plugin.getBot().getGuild().getName()));
                return;
            }
            if (!LinkCommand.getDiscordIdCode().containsValue(this.arg)) {
                player.sendMessage(super.plugin.getLang().getMessage("chat.player.invalid-code",
                        super.plugin.getBot().getGuild().getName()));
                return;
            }
            String discordId = MapUtil.getKeyFromValue(LinkCommand.getDiscordIdCode(), this.arg);
            if (discordId == null) {
                return;
            }
            super.plugin.getBot().getGuild().retrieveMemberById(discordId).queue(member -> {
                super.plugin.getLinksYamlFile().linkUser(player.getUniqueId().toString(), discordId);
                LinkCommand.getDiscordIdCode().values().remove(this.arg);
                VoiceChannel mainVoiceChannel = super.plugin.getConfigYamlFile().getVoiceChannel();
                new EmbeddedMenu(this.plugin.getBot()).setContent("account-linked",
                                mainVoiceChannel.getAsMention())
                        .message(member.getUser());
                player.sendMessage(super.plugin.getLang().getMessage("chat.player.account-linked"));
            }, new ErrorHandler().handle(ErrorResponse.UNKNOWN_MEMBER, e ->
                    player.sendMessage(super.plugin.getLang().getMessage("chat.player.invalid-code"))));
        });
    }

}
