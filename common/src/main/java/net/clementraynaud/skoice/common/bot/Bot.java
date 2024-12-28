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

package net.clementraynaud.skoice.common.bot;

import net.clementraynaud.skoice.common.Skoice;
import net.clementraynaud.skoice.common.commands.CommandInfo;
import net.clementraynaud.skoice.common.commands.skoice.arguments.Argument;
import net.clementraynaud.skoice.common.lang.DiscordLang;
import net.clementraynaud.skoice.common.lang.LangInfo;
import net.clementraynaud.skoice.common.listeners.session.ReadyListener;
import net.clementraynaud.skoice.common.menus.EmbeddedMenu;
import net.clementraynaud.skoice.common.menus.MenuFactory;
import net.clementraynaud.skoice.common.model.minecraft.BasePlayer;
import net.clementraynaud.skoice.common.model.minecraft.SkoiceCommandSender;
import net.clementraynaud.skoice.common.storage.TempYamlFile;
import net.clementraynaud.skoice.common.storage.config.ConfigField;
import net.clementraynaud.skoice.common.system.ProximityChannel;
import net.clementraynaud.skoice.common.system.ProximityChannels;
import net.clementraynaud.skoice.common.tasks.InterruptSystemTask;
import net.clementraynaud.skoice.common.tasks.UpdateVoiceStateTask;
import net.clementraynaud.skoice.common.util.MapUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.interactions.Interaction;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Bot {

    private final Skoice plugin;
    private final DiscordLang lang;
    private final BotCommands commands;
    private final BotVoiceChannel voiceChannel;
    private final MenuFactory menuFactory;
    private JDA jda;
    private BotStatus status = BotStatus.NOT_CONNECTED;
    private boolean isStatusAcknowledged = false;
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

    public void connect(SkoiceCommandSender sender) {
        if (!this.plugin.getConfigYamlFile().contains(ConfigField.TOKEN.toString())) {
            this.acknowledgeStatus();
            this.plugin.log(Level.WARNING, "logger.warning.no-token");
            return;
        }

        this.plugin.log(Level.INFO, "chat.configuration.bot-connecting");
        BasePlayer tokenManager;
        if (sender instanceof BasePlayer) {
            tokenManager = (BasePlayer) sender;
            this.tokenManagerId = tokenManager.getUniqueId().toString();
            tokenManager.sendMessage(this.plugin.getLang().getMessage("chat.configuration.bot-connecting"));
        } else {
            tokenManager = null;
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

        byte[] finalBase64TokenBytes = base64TokenBytes;
        CompletableFuture.runAsync(() -> {
            try {
                this.jda = JDABuilder.createDefault(new String(finalBase64TokenBytes))
                        .addEventListeners(new ReadyListener(this.plugin))
                        .build();
            } catch (InvalidTokenException | IllegalArgumentException | ErrorResponseException e) {
                this.acknowledgeStatus();
                this.plugin.log(Level.WARNING, "chat.configuration.bot-could-not-connect");
                this.plugin.getConfigYamlFile().remove(ConfigField.TOKEN.toString());
                if (tokenManager != null) {
                    tokenManager.sendMessage(this.plugin.getLang().getMessage("chat.configuration.bot-could-not-connect"));
                }
            }
        });
    }

    public boolean isAvailable() {
        return this.jda != null
                && this.jda.getStatus() == JDA.Status.CONNECTED;
    }

    public boolean canShutdown() {
        return this.jda != null
                && this.jda.getStatus() != JDA.Status.SHUTTING_DOWN
                && this.jda.getStatus() != JDA.Status.SHUTDOWN;
    }

    public void shutdown() {
        if (this.canShutdown()) {
            if (this.isAvailable()) {
                this.runInterruptSystemTask();
            }

            this.jda.shutdown();
            try {
                if (!this.jda.awaitShutdown(Duration.ofSeconds(5))) {
                    this.jda.shutdownNow();
                    this.jda.awaitShutdown();
                }
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void runInterruptSystemTask() {
        new InterruptSystemTask(this.plugin).run();
    }

    public boolean isAdministrator() {
        return this.status.ordinal() > BotStatus.MISSING_PERMISSION.ordinal();
    }

    public void allowApplicationCommands(Guild guild) {
        if (guild.getSelfMember().hasPermission(Permission.ADMINISTRATOR)
                && (guild.getRequiredMFALevel() != Guild.MFALevel.TWO_FACTOR_AUTH
                || this.plugin.getBot().getJDA().getSelfUser().isMfaEnabled())
                && !guild.getPublicRole().hasPermission(Permission.USE_APPLICATION_COMMANDS)) {
            guild.getPublicRole().getManager().givePermissions(Permission.USE_APPLICATION_COMMANDS).queue();
        }
    }

    public void setDefaultAvatar() {
        if (this.jda.getSelfUser().getDefaultAvatarUrl().equals(this.jda.getSelfUser().getEffectiveAvatarUrl())) {
            CompletableFuture.runAsync(() -> {
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
            new EmbeddedMenu(this).setContent("account-not-linked").message(member.getUser());
        } else {
            BasePlayer player = this.plugin.getPlayer(UUID.fromString(minecraftId));
            if (player != null) {
                player.sendMessage(this.plugin.getLang().getMessage("chat.player.connected"));
                this.callPlayerProximityConnectEvent(minecraftId, member.getId());
            }
        }
    }

    protected void callPlayerProximityConnectEvent(String minecraftId, String memberId) {
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

    public void retrieveProximityChannels() {
        ProximityChannels.clear();

        Guild guild = this.getGuild();
        if (guild == null) {
            return;
        }

        List<String> storedChannels = this.plugin.getTempYamlFile().getStringList(TempYamlFile.VOICE_CHANNELS_ID_FIELD);
        Set<VoiceChannel> remainingChannels = guild.getVoiceChannels().stream()
                .filter(channel -> storedChannels.contains(channel.getId()))
                .collect(Collectors.toSet());
        remainingChannels.forEach(channel -> new ProximityChannel(this.plugin, channel.getId()));
        this.plugin.getTempYamlFile().set(TempYamlFile.VOICE_CHANNELS_ID_FIELD,
                remainingChannels.stream()
                        .map(ISnowflake::getId)
                        .collect(Collectors.toList())
        );
    }

    public void updateStatus() {
        if (!this.isAvailable()) {
            this.status = BotStatus.NOT_CONNECTED;
            if (!this.plugin.getConfigYamlFile().contains(ConfigField.TOKEN.toString())) {
                this.plugin.log(Level.WARNING, "logger.warning.no-token");
            }

        } else {
            List<Guild> guilds = this.jda.getGuilds();

            if (guilds.isEmpty()) {
                this.status = BotStatus.NO_GUILD;
                this.plugin.log(Level.WARNING, "chat.configuration.no-guild");
            } else if (guilds.size() > 1) {
                this.status = BotStatus.MULTIPLE_GUILDS;
                this.plugin.log(Level.WARNING, "logger.warning.multiple-guilds");
            } else {
                this.guildId = guilds.get(0).getId();
                this.plugin.getLang().getFormatter().set("guild",
                        this.getGuild().getName().replace(Character.toString('&'), ""));

                if (this.getGuild().getRequiredMFALevel() == Guild.MFALevel.TWO_FACTOR_AUTH
                        && !this.jda.getSelfUser().isMfaEnabled()) {
                    this.status = BotStatus.MFA_REQUIRED;
                    this.plugin.log(Level.WARNING, "logger.warning.two-factor-authentication");

                } else if (!this.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
                    this.status = BotStatus.MISSING_PERMISSION;
                    this.plugin.log(Level.WARNING, "logger.error.missing-permission");

                } else if (!this.plugin.getConfigYamlFile().contains(ConfigField.VOICE_CHANNEL_ID.toString())) {
                    this.status = BotStatus.NO_VOICE_CHANNEL;
                    this.plugin.log(Level.WARNING, "logger.warning.no-voice-channel");

                } else if (!this.plugin.getConfigYamlFile().contains(ConfigField.HORIZONTAL_RADIUS.toString())
                        || !this.plugin.getConfigYamlFile().contains(ConfigField.VERTICAL_RADIUS.toString())) {
                    this.status = BotStatus.NO_RADIUS;
                    this.plugin.log(Level.WARNING, "logger.warning.no-radius");

                } else {
                    this.status = BotStatus.READY;
                    this.callSystemReadyEvent();
                }
            }

            this.updateActivity();
        }

        this.isStatusAcknowledged = true;
    }

    protected void callSystemReadyEvent() {
    }

    public void updateActivity() {
        Activity activity = this.jda.getPresence().getActivity();
        if (this.status == BotStatus.READY && !Objects.equals(activity, Activity.listening("/link"))) {
            this.jda.getPresence().setActivity(Activity.listening("/link"));
        } else if (this.status != BotStatus.READY && !Objects.equals(activity, Activity.listening("/configure"))) {
            this.jda.getPresence().setActivity(Activity.listening("/configure"));
        }
    }

    public void sendIncompleteConfigurationAlert(BasePlayer player, boolean sendIfPermissionMissing, boolean force) {
        if (!this.isStatusAcknowledged) {
            return;
        }

        if (player.hasPermission(Argument.MANAGE_PERMISSION) || force) {
            if (this.status == BotStatus.NOT_CONNECTED) {
                if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.TOOLTIPS.toString())) {
                    CompletableFuture.runAsync(() -> {
                        player.sendMessage(this.plugin.getLang()
                                .getInteractiveMessage("chat.configuration.incomplete-configuration-operator-interactive"));
                    });
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

    public void sendNoGuildAlert(BasePlayer player) {
        if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.TOOLTIPS.toString())) {
            player.sendMessage(
                    this.plugin.getLang().getInteractiveMessage("chat.configuration.no-guild-interactive"));
        } else {
            player.sendMessage(this.plugin.getLang().getMessage("chat.configuration.no-guild"));
        }
    }

    public JDA getJDA() {
        return this.jda;
    }

    public BotStatus getStatus() {
        if (!this.isAvailable() && this.status != BotStatus.NOT_CONNECTED) {
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

    public void acknowledgeStatus() {
        this.isStatusAcknowledged = true;
    }

    public BasePlayer getTokenManager() {
        if (this.tokenManagerId == null) {
            return null;
        }
        return this.plugin.getPlayer(UUID.fromString(this.tokenManagerId));
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

    public String getInviteUrl() {
        return this.inviteUrl;
    }

    public void setInviteUrl(String inviteUrl) {
        this.inviteUrl = inviteUrl;
        this.plugin.getLang().getFormatter().set("bot-invite-url", inviteUrl);
    }
}
