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
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.commands.ConfigureCommand;
import net.clementraynaud.skoice.commands.InviteCommand;
import net.clementraynaud.skoice.commands.LinkCommand;
import net.clementraynaud.skoice.commands.UnlinkCommand;
import net.clementraynaud.skoice.listeners.ReconnectedListener;
import net.clementraynaud.skoice.listeners.channel.main.update.ChannelUpdateParentListener;
import net.clementraynaud.skoice.listeners.channel.network.ChannelDeleteListener;
import net.clementraynaud.skoice.listeners.guild.GuildJoinListener;
import net.clementraynaud.skoice.listeners.guild.GuildLeaveListener;
import net.clementraynaud.skoice.listeners.guild.member.GuildMemberRoleAddListener;
import net.clementraynaud.skoice.listeners.guild.member.GuildMemberRoleRemoveListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceJoinListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceLeaveListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceMoveListener;
import net.clementraynaud.skoice.listeners.interaction.ModalInteractionListener;
import net.clementraynaud.skoice.listeners.interaction.component.ButtonInteractionListener;
import net.clementraynaud.skoice.listeners.interaction.component.SelectMenuInteractionListener;
import net.clementraynaud.skoice.listeners.message.MessageDeleteListener;
import net.clementraynaud.skoice.listeners.message.MessageReceivedListener;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerJoinListener;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerMoveListener;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerQuitListener;
import net.clementraynaud.skoice.listeners.player.eligible.PlayerTeleportListener;
import net.clementraynaud.skoice.listeners.role.update.RoleUpdatePermissionsListener;
import net.clementraynaud.skoice.tasks.InterruptSystemTask;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

public class ListenerManager {

    private boolean startup = true;

    private final Skoice plugin;

    public ListenerManager(Skoice plugin) {
        this.plugin = plugin;
    }

    public void update(User user) {
        boolean wasBotReady = this.plugin.getBot().getStatus() == BotStatus.READY;
        this.plugin.getBot().updateStatus();
        if (this.startup) {
            this.startup = false;
            if (this.plugin.getBot().getStatus() == BotStatus.READY) {
                this.registerEligiblePlayerListeners();
                this.registerBotListeners();
            } else {
                Bukkit.getPluginManager().registerEvents(new net.clementraynaud.skoice.listeners.player.PlayerJoinListener(this.plugin), this.plugin);
            }
        } else if (!wasBotReady && this.plugin.getBot().getStatus() == BotStatus.READY) {
            HandlerList.unregisterAll(new net.clementraynaud.skoice.listeners.player.PlayerJoinListener(this.plugin));
            this.registerEligiblePlayerListeners();
            this.registerBotListeners();
            this.plugin.getLogger().info(this.plugin.getLang().getMessage("logger.info.configuration-complete"));
            if (user != null) {
                user.openPrivateChannel().complete()
                        .sendMessage(this.plugin.getBot().getMenu("configuration-complete").build())
                        .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
            }
        } else if (wasBotReady && this.plugin.getBot().getStatus() != BotStatus.READY) {
            this.plugin.getConfigurationMenu().delete();
            this.unregisterEligiblePlayerListeners();
            Bukkit.getPluginManager().registerEvents(new net.clementraynaud.skoice.listeners.player.PlayerJoinListener(this.plugin), this.plugin);
            if (this.plugin.getBot().getJDA() != null) {
                this.unregisterBotListeners();
            }
            new InterruptSystemTask(this.plugin.getConfiguration()).run();
        }
    }

    public void update() {
        this.update(null);
    }

    private void registerEligiblePlayerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(this.plugin), this.plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(this.plugin), this.plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveListener(this.plugin.getEligiblePlayers()), this.plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerTeleportListener(this.plugin.getEligiblePlayers()), this.plugin);
    }

    private void unregisterEligiblePlayerListeners() {
        HandlerList.unregisterAll(new PlayerJoinListener(this.plugin));
        HandlerList.unregisterAll(new PlayerQuitListener(this.plugin));
        HandlerList.unregisterAll(new PlayerMoveListener(this.plugin.getEligiblePlayers()));
        HandlerList.unregisterAll(new PlayerTeleportListener(this.plugin.getEligiblePlayers()));
    }

    public void registerPermanentBotListeners() {
        this.plugin.getBot().getJDA().addEventListener(
                new ReconnectedListener(this.plugin),
                new GuildJoinListener(this.plugin),
                new GuildLeaveListener(this.plugin),
                new GuildMemberRoleAddListener(this.plugin),
                new GuildMemberRoleRemoveListener(this.plugin),
                new RoleUpdatePermissionsListener(this.plugin),
                new MessageReceivedListener(this.plugin),
                new MessageDeleteListener(this.plugin.getConfigurationMenu()),
                new net.clementraynaud.skoice.listeners.channel.main.ChannelDeleteListener(this.plugin),
                new ChannelUpdateParentListener(this.plugin),
                new ConfigureCommand(this.plugin),
                new InviteCommand(this.plugin),
                new LinkCommand(this.plugin),
                new UnlinkCommand(this.plugin),
                new ButtonInteractionListener(this.plugin),
                new SelectMenuInteractionListener(this.plugin),
                new ModalInteractionListener(this.plugin)
        );
    }

    public void registerBotListeners() {
        this.plugin.getBot().getJDA().addEventListener(
                new GuildVoiceJoinListener(this.plugin),
                new GuildVoiceLeaveListener(this.plugin),
                new GuildVoiceMoveListener(this.plugin),
                new ChannelDeleteListener()
        );
    }

    private void unregisterBotListeners() {
        this.plugin.getBot().getJDA().removeEventListener(
                new GuildVoiceJoinListener(this.plugin),
                new GuildVoiceLeaveListener(this.plugin),
                new GuildVoiceMoveListener(this.plugin),
                new ChannelDeleteListener()
        );
    }
}
