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

package net.clementraynaud.skoice.commands;

import net.clementraynaud.skoice.bot.Bot;
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.lang.Lang;
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.menus.MenuType;
import net.clementraynaud.skoice.util.MapUtil;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.UUID;

public class UnlinkCommand extends ListenerAdapter {

    private final Config config;
    private final Lang lang;
    private final Bot bot;

    public UnlinkCommand(Config config, Lang lang, Bot bot) {
        this.config = config;
        this.lang = lang;
        this.bot = bot;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if ("unlink".equals(event.getName())) {
            String minecraftID = new MapUtil().getKeyFromValue(this.config.getLinks(), event.getUser().getId());
            if (minecraftID == null) {
                event.reply(new Menu(this.bot.getMenusYaml().getConfigurationSection("linking-process"),
                        Collections.singleton(this.bot.getFields().get("account-not-linked").toField(this.lang)),
                        MenuType.ERROR)
                        .toMessage(this.config, this.lang, this.bot)).setEphemeral(true).queue();
            } else {
                this.config.unlinkUser(minecraftID);
                event.reply(new Menu(this.bot.getMenusYaml().getConfigurationSection("linking-process"),
                        Collections.singleton(this.bot.getFields().get("account-unlinked").toField(this.lang)),
                        MenuType.SUCCESS)
                        .toMessage(this.config, this.lang, this.bot)).setEphemeral(true).queue();
                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(minecraftID));
                if (player.isOnline() && player.getPlayer() != null) {
                    player.getPlayer().sendMessage(this.lang.getMessage("minecraft.chat.player.account-unlinked"));
                    GuildVoiceState voiceState = event.getMember().getVoiceState();
                    if (voiceState != null) {
                        VoiceChannel voiceChannel = voiceState.getChannel();
                        if (voiceChannel != null && voiceChannel.equals(this.config.getLobby())) {
                            player.getPlayer().sendMessage(this.lang.getMessage("minecraft.chat.player.disconnected-from-proximity-voice-chat"));
                        }
                    }
                }
            }
        }
    }
}
