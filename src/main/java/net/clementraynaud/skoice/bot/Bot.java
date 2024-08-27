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

package net.clementraynaud.skoice.bot;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.api.events.player.PlayerProximityConnectEvent;
import net.clementraynaud.skoice.api.events.system.SystemReadyEvent;
import net.clementraynaud.skoice.commands.CommandInfo;
import net.clementraynaud.skoice.commands.skoice.arguments.Argument;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.lang.LangInfo;
import net.clementraynaud.skoice.listeners.session.ReadyListener;
import net.clementraynaud.skoice.menus.ConfigurationMenu;
import net.clementraynaud.skoice.menus.EmbeddedMenu;
import net.clementraynaud.skoice.menus.MenuFactory;
import net.clementraynaud.skoice.storage.TempYamlFile;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.clementraynaud.skoice.system.Network;
import net.clementraynaud.skoice.system.ProximityChannel;
import net.clementraynaud.skoice.tasks.UpdateVoiceStateTask;
import net.clementraynaud.skoice.util.MapUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Bot {

    private final Skoice plugin;
    private final DiscordLang lang;
    private final BotCommands commands;
    private final BotVoiceChannel voiceChannel;
    private final MenuFactory menuFactory;
    private ConfigurationMenu configurationMenu;
    private JDA jda;
    private BotStatus status = BotStatus.NOT_CONNECTED;
    private String tokenManagerId;
    private String guildId;
    private String inviteUrl;

    public Bot(Skoice plugin) {
        this.plugin = plugin;
        this.lang = new DiscordLang();
        this.lang.load(LangInfo.valueOf(this.plugin.getConfigYamlFile().getString(ConfigField.LANG.toString())));
        this.commands = new BotCommands(this);
        this.voiceChannel = new BotVoiceChannel(this.plugin);
        this.menuFactory = new MenuFactory();
    }

    public void connect() {
        this.connect(null);
    }

    public void connect(CommandSender sender) {
        if (!this.plugin.getConfigYamlFile().contains(ConfigField.TOKEN.toString())) {
            this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.no-token"));
            return;
        }

        this.plugin.getLogger().info(this.plugin.getLang().getMessage("logger.info.bot-connecting"));
        Player tokenManager = null;
        if (sender instanceof Player) {
            tokenManager = (Player) sender;
            this.tokenManagerId = tokenManager.getUniqueId().toString();
            tokenManager.sendMessage(this.plugin.getLang().getMessage("chat.configuration.bot-connecting"));
        }

        byte[] base64TokenBytes;
        try {
            base64TokenBytes = Base64.getDecoder()
                    .decode(this.plugin.getConfigYamlFile().getString(ConfigField.TOKEN.toString()));
            for (int i = 0; i < base64TokenBytes.length; i++) {
                base64TokenBytes[i]--;
            }
        } catch (IllegalArgumentException e) {
            base64TokenBytes = new byte[0];
        }

        try {
            this.jda = JDABuilder.createDefault(new String(base64TokenBytes))
                    .addEventListeners(new ReadyListener(this.plugin))
                    .build();
        } catch (InvalidTokenException | IllegalArgumentException e) {
            this.plugin.getLogger().severe(this.plugin.getLang().getMessage("logger.error.bot-could-not-connect"));
            this.plugin.getConfigYamlFile().remove(ConfigField.TOKEN.toString());
            if (tokenManager != null) {
                tokenManager.sendMessage(this.plugin.getLang().getMessage("chat.configuration.bot-could-not-connect"));
            }
        }
    }

    public boolean isAdministrator() {
        Guild guild = this.plugin.getBot().getGuild();
        if (guild == null) {
            return false;
        }
        return guild.getSelfMember().hasPermission(Permission.ADMINISTRATOR);
    }

    public void setDefaultAvatar() {
        if (this.jda.getSelfUser().getDefaultAvatarUrl().equals(this.jda.getSelfUser().getEffectiveAvatarUrl())) {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                try (InputStream inputStream = new URL("https://clementraynaud.net/Skoice.jpeg").openStream()) {
                    Icon icon = Icon.from(inputStream);
                    this.jda.getSelfUser().getManager().setAvatar(icon).queue();
                } catch (IOException ignored) {
                }
            });
        }
    }

    public void notifyIfUnlinked(Member member) {
        String minecraftId = MapUtil.getKeyFromValue(this.plugin.getLinksYamlFile().getLinks(), member.getId());
        if (minecraftId == null) {
            new EmbeddedMenu(this).setContent("account-not-linked",
                            this.plugin.getBot().getCommands().getAsMention(CommandInfo.LINK.toString()))
                    .message(member.getUser());
        } else {
            Player player = this.plugin.getServer().getPlayer(UUID.fromString(minecraftId));
            if (player != null) {
                player.sendMessage(this.plugin.getLang().getMessage("chat.player.connected"));
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                    PlayerProximityConnectEvent event = new PlayerProximityConnectEvent(minecraftId, member.getId());
                    this.plugin.getServer().getPluginManager().callEvent(event);
                });
            }
        }
    }

    public void updateGuild() {
        List<Guild> guilds = this.jda.getGuilds();
        this.guildId = guilds.size() == 1 ? guilds.get(0).getId() : null;
    }

    public void retrieveMutedUsers() {
        UpdateVoiceStateTask.getMutedUsers().clear();
        UpdateVoiceStateTask.getMutedUsers().addAll(this.plugin.getTempYamlFile().getStringList(TempYamlFile.MUTED_USERS_ID_FIELD));
    }

    public void updateVoiceState() {
        Guild guild = this.plugin.getBot().getGuild();
        if (guild == null) {
            return;
        }
        for (VoiceChannel channel : guild.getVoiceChannels()) {
            for (Member member : channel.getMembers()) {
                new UpdateVoiceStateTask(this.plugin, member, channel).run();
            }
        }
    }

    public void retrieveNetworks() {
        Guild guild = this.getGuild();
        if (guild == null) {
            return;
        }

        List<String> voiceChannels = this.plugin.getTempYamlFile().getStringList(TempYamlFile.VOICE_CHANNELS_ID_FIELD);
        this.plugin.getTempYamlFile().remove(TempYamlFile.VOICE_CHANNELS_ID_FIELD);

        guild.getVoiceChannels().stream()
                .filter(channel -> voiceChannels.contains(channel.getId()))
                .forEach(channel -> new ProximityChannel(this.plugin, channel.getId()));
    }

    public void updateStatus() {
        if (this.jda == null) {
            this.status = BotStatus.NOT_CONNECTED;
            if (!this.plugin.getConfigYamlFile().contains(ConfigField.TOKEN.toString())) {
                this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.no-token"));
            }
        } else {
            if (this.guildId == null) {
                List<Guild> guilds = this.jda.getGuilds();
                if (guilds.isEmpty()) {
                    this.status = BotStatus.NO_GUILD;
                    this.jda.retrieveApplicationInfo().queue(applicationInfo -> {
                        applicationInfo.setRequiredScopes("applications.commands");
                        this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.no-guild", applicationInfo.getInviteUrl(Permission.ADMINISTRATOR)));
                    });
                } else if (guilds.size() > 1) {
                    this.status = BotStatus.MULTIPLE_GUILDS;
                    this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.multiple-guilds"));
                }
            } else if (!this.isAdministrator()) {
                this.status = BotStatus.MISSING_PERMISSION;
                this.jda.retrieveApplicationInfo().queue(applicationInfo -> {
                    applicationInfo.setRequiredScopes("applications.commands");
                    this.plugin.getLogger().severe(this.plugin.getLang().getMessage("logger.error.missing-permission", applicationInfo.getInviteUrl(Permission.ADMINISTRATOR)));
                });
            } else if (!this.plugin.getConfigYamlFile().contains(ConfigField.VOICE_CHANNEL_ID.toString())) {
                this.status = BotStatus.NO_VOICE_CHANNEL;
                this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.no-voice-channel"));
            } else if (!this.plugin.getConfigYamlFile().contains(ConfigField.HORIZONTAL_RADIUS.toString())
                    || !this.plugin.getConfigYamlFile().contains(ConfigField.VERTICAL_RADIUS.toString())) {
                this.status = BotStatus.NO_RADIUS;
                this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.no-radius"));
            } else {
                this.status = BotStatus.READY;
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                    SystemReadyEvent event = new SystemReadyEvent();
                    this.plugin.getServer().getPluginManager().callEvent(event);
                });
            }
            this.updateActivity();
        }
    }

    public void updateActivity() {
        Activity activity = this.jda.getPresence().getActivity();
        if (this.status == BotStatus.READY && !Objects.equals(activity, Activity.listening("/link"))) {
            this.jda.getPresence().setActivity(Activity.listening("/link"));
        } else if (this.status != BotStatus.READY && !Objects.equals(activity, Activity.listening("/configure"))) {
            this.jda.getPresence().setActivity(Activity.listening("/configure"));
        }
    }

    public void sendIncompleteConfigurationAlert(Player player, boolean sendIfPermissionMissing, boolean force) {
        if (player.hasPermission(Argument.MANAGE_PERMISSION) || force) {
            if (this.status == BotStatus.NOT_CONNECTED) {
                if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.TOOLTIPS.toString())) {
                    this.plugin.adventure().player(player).sendMessage(this.plugin.getLang().getMessage("chat.configuration.incomplete-configuration-operator-interactive",
                                    this.plugin.getLang().getComponentMessage("interaction.here")
                                            .hoverEvent(HoverEvent.showText(this.plugin.getLang().getComponentMessage("interaction.execute", "/skoice configure")))
                                            .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/skoice configure")),
                                    this.plugin.getLang().getComponentMessage("interaction.here")
                                            .hoverEvent(HoverEvent.showText(this.plugin.getLang().getComponentMessage("interaction.shortcut", "/skoice language")))
                                            .clickEvent(net.kyori.adventure.text.event.ClickEvent.suggestCommand("/skoice language "))
                            )
                    );
                } else {
                    player.sendMessage(this.plugin.getLang().getMessage("chat.configuration.incomplete-configuration-operator"));
                }
            } else if (this.status == BotStatus.NO_GUILD) {
                this.sendNoGuildAlert(player);
            } else {
                player.sendMessage(this.plugin.getLang().getMessage("chat.configuration.incomplete-configuration-operator-discord"));
            }
        } else if (sendIfPermissionMissing) {
            player.sendMessage(this.plugin.getLang().getMessage("chat.configuration.incomplete-configuration"));
        }
    }

    public void sendNoGuildAlert(Player player) {
        if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.TOOLTIPS.toString())) {
            this.jda.retrieveApplicationInfo().queue(applicationInfo -> {
                applicationInfo.setRequiredScopes("applications.commands");
                String inviteUrl = applicationInfo.getInviteUrl(Permission.ADMINISTRATOR);
                this.plugin.adventure().player(player).sendMessage(
                        this.plugin.getLang().getMessage("chat.configuration.no-guild-interactive",
                                this.plugin.getLang().getComponentMessage("interaction.this-page")
                                        .hoverEvent(HoverEvent.showText(this.plugin.getLang().getComponentMessage("interaction.link", inviteUrl)))
                                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.openUrl(inviteUrl))
                        )
                );
            });
        } else {
            this.jda.retrieveApplicationInfo().queue(applicationInfo -> {
                applicationInfo.setRequiredScopes("applications.commands");
                player.sendMessage(this.plugin.getLang().getMessage("chat.configuration.no-guild",
                        applicationInfo.getInviteUrl(Permission.ADMINISTRATOR)));
            });
        }
    }

    public JDA getJDA() {
        return this.jda;
    }

    public BotStatus getStatus() {
        if (this.jda == null && this.status != BotStatus.NOT_CONNECTED) {
            this.status = BotStatus.NOT_CONNECTED;
        }
        return this.status;
    }

    public DiscordLang getLang() {
        return this.lang;
    }

    public BotCommands getCommands() {
        return this.commands;
    }

    public Player getTokenManager() {
        if (this.tokenManagerId == null) {
            return null;
        }
        return this.plugin.getServer().getPlayer(UUID.fromString(this.tokenManagerId));
    }

    public Guild getGuild() {
        if (this.guildId == null) {
            return null;
        }
        return this.jda.getGuildById(this.guildId);
    }

    public Guild getGuild(Interaction interaction) {
        if (interaction.isFromGuild()) {
            return interaction.getGuild();
        }
        return this.getGuild();
    }

    public BotVoiceChannel getVoiceChannel() {
        return this.voiceChannel;
    }

    public MenuFactory getMenuFactory() {
        return this.menuFactory;
    }

    public void generateConfigurationMenu(IReplyCallback interaction) {
        this.configurationMenu = new ConfigurationMenu(this);
        this.configurationMenu.reply(interaction);
    }

    public Optional<ConfigurationMenu> getConfigurationMenu() {
        return Optional.ofNullable(this.configurationMenu);
    }

    public String getInviteUrl() {
        return this.inviteUrl;
    }

    public void setInviteUrl(String inviteUrl) {
        this.inviteUrl = inviteUrl;
    }
}
