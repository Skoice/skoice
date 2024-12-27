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

package net.clementraynaud.skoice.system;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.commands.CommandInfo;
import net.clementraynaud.skoice.handlers.player.PlayerJoinHandler;
import net.clementraynaud.skoice.handlers.player.PlayerQuitHandler;
import net.clementraynaud.skoice.listeners.StatusChangeListener;
import net.clementraynaud.skoice.listeners.channel.network.GenericChannelListener;
import net.clementraynaud.skoice.listeners.guild.GuildJoinListener;
import net.clementraynaud.skoice.listeners.guild.GuildLeaveListener;
import net.clementraynaud.skoice.listeners.guild.member.GuildMemberRoleAddListener;
import net.clementraynaud.skoice.listeners.guild.member.GuildMemberRoleRemoveListener;
import net.clementraynaud.skoice.listeners.guild.override.GenericPermissionOverrideListener;
import net.clementraynaud.skoice.listeners.guild.update.GuildUpdateMFALevelListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceGuildMuteListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceUpdateListener;
import net.clementraynaud.skoice.listeners.interaction.ModalInteractionListener;
import net.clementraynaud.skoice.listeners.interaction.command.SlashCommandInteractionListener;
import net.clementraynaud.skoice.listeners.interaction.component.ButtonInteractionListener;
import net.clementraynaud.skoice.listeners.interaction.component.StringSelectInteractionListener;
import net.clementraynaud.skoice.listeners.role.update.RoleUpdatePermissionsListener;
import net.clementraynaud.skoice.menus.ConfigurationMenus;
import net.clementraynaud.skoice.menus.EmbeddedMenu;
import net.clementraynaud.skoice.tasks.InterruptSystemTask;
import net.dv8tion.jda.api.entities.User;

public abstract class ListenerManager {

    protected final Skoice plugin;
    private final PlayerQuitHandler playerQuitListener;
    private final PlayerJoinHandler playerJoinListener;
    private final GuildVoiceGuildMuteListener guildVoiceGuildMuteListener;
    private final GuildVoiceUpdateListener guildVoiceUpdateListener;
    private final GenericChannelListener genericChannelListener;

    public ListenerManager(Skoice plugin) {
        this.plugin = plugin;
        this.playerQuitListener = new PlayerQuitHandler();
        this.playerJoinListener = new PlayerJoinHandler(this.plugin);
        this.guildVoiceGuildMuteListener = new GuildVoiceGuildMuteListener(this.plugin);
        this.guildVoiceUpdateListener = new GuildVoiceUpdateListener(this.plugin);
        this.genericChannelListener = new GenericChannelListener(this.plugin);
    }

    public PlayerJoinHandler getPlayerJoinListener() {
        return this.playerJoinListener;
    }

    public void update(User user) {
        BotStatus oldStatus = this.plugin.getBot().getStatus();
        this.plugin.getBot().updateStatus();
        BotStatus newStatus = this.plugin.getBot().getStatus();

        if (oldStatus != newStatus && newStatus.getMenuId() != null) {
            ConfigurationMenus.refreshAll();
        }

        if (oldStatus != BotStatus.READY && newStatus == BotStatus.READY) {
            this.registerMinecraftListeners();
            this.registerBotListeners();
            this.plugin.getLinksYamlFile().refreshOnlineLinkedPlayers();
            this.plugin.getBot().getVoiceChannel().notifyUnlinkedUsers();
            this.plugin.getBot().getVoiceChannel().setStatus();
            this.plugin.getBot().getVoiceChannel().updatePermissions();
            this.plugin.getBot().updateVoiceState();
            if (!ProximityChannels.getInitialized().isEmpty()) {
                this.plugin.getBot().retrieveProximityChannels();
            }
            this.plugin.getUpdateNetworksTask().start();
            this.plugin.getLogger().info(this.plugin.getLang().getMessage("logger.info.configuration-complete"));
            if (user != null) {
                new EmbeddedMenu(this.plugin.getBot()).setContent("configuration-complete",
                                this.plugin.getBot().getCommands().getAsMention(CommandInfo.LINK.toString()))
                        .message(user);
            }
        } else if (oldStatus == BotStatus.READY && newStatus != BotStatus.READY) {
            this.unregisterMinecraftListeners();
            if (newStatus != BotStatus.NOT_CONNECTED) {
                new InterruptSystemTask(this.plugin).run();
            }
        }
    }

    public void update() {
        this.update(null);
    }

    public abstract void registerPermanentMinecraftListeners();

    public abstract void registerMinecraftListeners();

    public abstract void unregisterMinecraftListeners();

    public void registerPermanentBotListeners() {
        this.plugin.getBot().getJDA().addEventListener(
                new GuildJoinListener(this.plugin),
                new GuildLeaveListener(this.plugin),
                new GuildUpdateMFALevelListener(this.plugin),
                new GuildMemberRoleAddListener(this.plugin),
                new GuildMemberRoleRemoveListener(this.plugin),
                new RoleUpdatePermissionsListener(this.plugin),
                new net.clementraynaud.skoice.listeners.channel.main.GenericChannelListener(this.plugin),
                new GenericPermissionOverrideListener(this.plugin),
                new SlashCommandInteractionListener(this.plugin),
                new ButtonInteractionListener(this.plugin),
                new StringSelectInteractionListener(this.plugin),
                new ModalInteractionListener(this.plugin),
                new StatusChangeListener(this.plugin)
        );
    }

    public void registerBotListeners() {
        this.plugin.getBot().getJDA().addEventListener(
                this.guildVoiceGuildMuteListener,
                this.guildVoiceUpdateListener,
                this.genericChannelListener
        );
    }

    public void unregisterBotListeners() {
        this.plugin.getBot().getJDA().removeEventListener(
                this.guildVoiceGuildMuteListener,
                this.guildVoiceUpdateListener,
                this.genericChannelListener
        );
    }

    public PlayerQuitHandler getPlayerQuitListener() {
        return this.playerQuitListener;
    }
}
