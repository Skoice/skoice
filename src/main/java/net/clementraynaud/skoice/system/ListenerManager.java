/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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
import net.clementraynaud.skoice.listeners.SessionRecreateListener;
import net.clementraynaud.skoice.listeners.channel.main.GenericChannelListener;
import net.clementraynaud.skoice.listeners.channel.network.ChannelDeleteListener;
import net.clementraynaud.skoice.listeners.guild.GuildJoinListener;
import net.clementraynaud.skoice.listeners.guild.GuildLeaveListener;
import net.clementraynaud.skoice.listeners.guild.member.GuildMemberRoleAddListener;
import net.clementraynaud.skoice.listeners.guild.member.GuildMemberRoleRemoveListener;
import net.clementraynaud.skoice.listeners.guild.override.GenericPermissionOverrideListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceGuildMuteListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceUpdateListener;
import net.clementraynaud.skoice.listeners.interaction.ModalInteractionListener;
import net.clementraynaud.skoice.listeners.interaction.component.ButtonInteractionListener;
import net.clementraynaud.skoice.listeners.interaction.component.StringSelectInteractionListener;
import net.clementraynaud.skoice.listeners.message.MessageDeleteListener;
import net.clementraynaud.skoice.listeners.message.MessageReceivedListener;
import net.clementraynaud.skoice.listeners.player.PlayerJoinListener;
import net.clementraynaud.skoice.listeners.player.PlayerMoveListener;
import net.clementraynaud.skoice.listeners.player.PlayerQuitListener;
import net.clementraynaud.skoice.listeners.player.PlayerTeleportListener;
import net.clementraynaud.skoice.listeners.role.update.RoleUpdatePermissionsListener;
import net.clementraynaud.skoice.tasks.InterruptSystemTask;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.event.HandlerList;

public class ListenerManager {

    private final Skoice plugin;
    private final PlayerQuitListener playerQuitListener;
    private final PlayerMoveListener playerMoveListener;
    private final PlayerTeleportListener playerTeleportListener;
    private final GuildVoiceGuildMuteListener guildVoiceGuildMuteListener;
    private final GuildVoiceUpdateListener guildVoiceUpdateListener;
    private final ChannelDeleteListener channelDeleteListener;

    private boolean startup = true;

    public ListenerManager(Skoice plugin) {
        this.plugin = plugin;
        this.playerQuitListener = new PlayerQuitListener(this.plugin);
        this.playerMoveListener = new PlayerMoveListener();
        this.playerTeleportListener = new PlayerTeleportListener();
        this.guildVoiceGuildMuteListener = new GuildVoiceGuildMuteListener(this.plugin);
        this.guildVoiceUpdateListener = new GuildVoiceUpdateListener(this.plugin);
        this.channelDeleteListener = new ChannelDeleteListener();
    }

    public void update(User user) {
        boolean wasBotReady = this.plugin.getBot().getStatus() == BotStatus.READY;
        this.plugin.getBot().updateStatus();
        if (this.startup) {
            this.startup = false;
            this.plugin.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this.plugin), this.plugin);
            if (this.plugin.getBot().getStatus() == BotStatus.READY) {
                this.registerEligiblePlayerListeners();
                this.registerBotListeners();
            }
        } else if (!wasBotReady && this.plugin.getBot().getStatus() == BotStatus.READY) {
            this.registerEligiblePlayerListeners();
            this.registerBotListeners();
            this.plugin.getLogger().info(this.plugin.getLang().getMessage("logger.info.configuration-complete"));
            if (user != null) {
                user.openPrivateChannel().queue(channel ->
                        channel.sendMessage(this.plugin.getBot().getMenu("configuration-complete").build())
                                .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER))
                );
            }
        } else if (wasBotReady && this.plugin.getBot().getStatus() != BotStatus.READY) {
            this.plugin.getConfigurationMenu().delete();
            this.unregisterEligiblePlayerListeners();
            if (this.plugin.getBot().getStatus() != BotStatus.NOT_CONNECTED) {
                this.unregisterBotListeners();
            }
            new InterruptSystemTask(this.plugin.getConfigYamlFile()).run();
        }
    }

    public void update() {
        this.update(null);
    }

    private void registerEligiblePlayerListeners() {
        this.plugin.getServer().getPluginManager().registerEvents(this.playerQuitListener, this.plugin);
        this.plugin.getServer().getPluginManager().registerEvents(this.playerMoveListener, this.plugin);
        this.plugin.getServer().getPluginManager().registerEvents(this.playerTeleportListener, this.plugin);
    }

    private void unregisterEligiblePlayerListeners() {
        HandlerList.unregisterAll(this.playerQuitListener);
        HandlerList.unregisterAll(this.playerMoveListener);
        HandlerList.unregisterAll(this.playerTeleportListener);
    }

    public void registerPermanentBotListeners() {
        this.plugin.getBot().getJDA().addEventListener(
                new SessionRecreateListener(this.plugin),
                new GuildJoinListener(this.plugin),
                new GuildLeaveListener(this.plugin),
                new GuildMemberRoleAddListener(this.plugin),
                new GuildMemberRoleRemoveListener(this.plugin),
                new RoleUpdatePermissionsListener(this.plugin),
                new MessageReceivedListener(this.plugin),
                new MessageDeleteListener(this.plugin.getConfigurationMenu()),
                new GenericChannelListener(this.plugin),
                new GenericPermissionOverrideListener(this.plugin),
                new ConfigureCommand(this.plugin),
                new InviteCommand(this.plugin),
                new LinkCommand(this.plugin),
                new UnlinkCommand(this.plugin),
                new ButtonInteractionListener(this.plugin),
                new StringSelectInteractionListener(this.plugin),
                new ModalInteractionListener(this.plugin)
        );
    }

    public void registerBotListeners() {
        this.plugin.getBot().getJDA().addEventListener(
                this.guildVoiceGuildMuteListener,
                this.guildVoiceUpdateListener,
                this.channelDeleteListener
        );
    }

    private void unregisterBotListeners() {
        this.plugin.getBot().getJDA().removeEventListener(
                this.guildVoiceGuildMuteListener,
                this.guildVoiceUpdateListener,
                this.channelDeleteListener
        );
    }
}
