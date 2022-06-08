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

package net.clementraynaud.skoice.bot;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.commands.ConfigureCommand;
import net.clementraynaud.skoice.commands.InviteCommand;
import net.clementraynaud.skoice.config.ConfigurationField;
import net.clementraynaud.skoice.listeners.guild.member.GuildMemberRoleAddListener;
import net.clementraynaud.skoice.listeners.guild.member.GuildMemberRoleRemoveListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceJoinListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceLeaveListener;
import net.clementraynaud.skoice.listeners.guild.voice.GuildVoiceMoveListener;
import net.clementraynaud.skoice.listeners.interaction.ButtonClickListener;
import net.clementraynaud.skoice.listeners.role.update.RoleUpdatePermissionsListener;
import net.clementraynaud.skoice.menus.MenuField;
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.listeners.interaction.SelectMenuListener;
import net.clementraynaud.skoice.listeners.ReconnectedListener;
import net.clementraynaud.skoice.listeners.channel.voice.lobby.VoiceChannelDeleteListener;
import net.clementraynaud.skoice.listeners.channel.voice.lobby.update.VoiceChannelUpdateParentListener;
import net.clementraynaud.skoice.listeners.guild.GuildJoinListener;
import net.clementraynaud.skoice.listeners.guild.GuildLeaveListener;
import net.clementraynaud.skoice.listeners.message.guild.GuildMessageDeleteListener;
import net.clementraynaud.skoice.listeners.message.guild.GuildMessageReceivedListener;
import net.clementraynaud.skoice.commands.LinkCommand;
import net.clementraynaud.skoice.commands.UnlinkCommand;
import net.clementraynaud.skoice.listeners.message.priv.PrivateMessageReceivedListener;
import net.clementraynaud.skoice.tasks.UpdateNetworksTask;
import net.clementraynaud.skoice.system.Network;
import net.clementraynaud.skoice.tasks.UpdateVoiceStateTask;
import net.clementraynaud.skoice.util.ConfigurationUtils;
import net.clementraynaud.skoice.util.MapUtil;
import net.clementraynaud.skoice.util.MessageUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Bot {

    private static final int TICKS_BETWEEN_VERSION_CHECKING = 720000;

    private final Map<String, MenuField> fields = new HashMap<>();
    private final Map<String, Menu> menus = new LinkedHashMap<>();

    private JDA jda;
    private boolean isReady;
    private boolean isOnMultipleGuilds;

    private final Skoice plugin;

    public Bot(Skoice plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        this.connect(null);
    }

    public void connect(CommandSender sender) {
        if (this.plugin.getConfiguration().getFile().contains(ConfigurationField.TOKEN.toString())) {
            byte[] base64TokenBytes = Base64.getDecoder().decode(this.plugin.getConfiguration().getFile().getString(ConfigurationField.TOKEN.toString()));
            for (int i = 0; i < base64TokenBytes.length; i++) {
                base64TokenBytes[i]--;
            }
            try {
                this.jda = JDABuilder.createDefault(new String(base64TokenBytes))
                        .enableIntents(GatewayIntent.GUILD_MEMBERS)
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
                        .build()
                        .awaitReady();
                this.plugin.getLogger().info(this.plugin.getLang().getMessage("logger.info.bot-connected"));
            } catch (LoginException e) {
                if (sender == null) {
                    this.plugin.getLogger().severe(this.plugin.getLang().getMessage("logger.error.bot-could-not-connect"));
                } else {
                    sender.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.bot-could-not-connect"));
                    this.plugin.getConfiguration().getFile().set(ConfigurationField.TOKEN.toString(), null);
                    this.plugin.getConfiguration().saveFile();
                }
            } catch (ErrorResponseException e) {
                if (sender == null) {
                    this.plugin.getLogger().severe(this.plugin.getLang().getMessage("logger.error.discord-api-timed-out"));
                } else {
                    try {
                        TextComponent discordStatusPage = new TextComponent(this.plugin.getLang().getMessage("minecraft.interaction.this-page"));
                        MessageUtil.setHoverEvent(discordStatusPage,
                                this.plugin.getLang().getMessage("minecraft.interaction.link", "https://discordstatus.com"));
                        discordStatusPage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discordstatus.com"));
                        sender.spigot().sendMessage(this.plugin.getLang().getMessage("minecraft.chat.error.discord-api-timed-out-interactive", discordStatusPage));
                    } catch (NoSuchMethodError e2) {
                        sender.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.error.discord-api-timed-out-link"));
                    }
                }
            } catch (IllegalStateException | InterruptedException ignored) {
            }
        }
    }

    public void setup(boolean startup, CommandSender sender) {
        this.setDefaultAvatar();
        this.plugin.getConfigurationMenu().delete();
        this.updateGuildUniquenessStatus();
        this.checkForValidLobby();
        this.jda.getGuilds().forEach(guild -> {
            new BotCommands(this.plugin).register(guild);
            if (guild.getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
                guild.getPublicRole().getManager().givePermissions(Permission.USE_SLASH_COMMANDS).queue();
            }
        });
        this.registerPermanentListeners();
        Bukkit.getScheduler().runTaskLater(this.plugin, () ->
                        Bukkit.getScheduler().runTaskTimerAsynchronously(
                                this.plugin,
                                new UpdateNetworksTask(this.plugin)::run,
                                0,
                                10
                        ),
                0
        );
        Bukkit.getScheduler().runTaskLater(this.plugin, () ->
                        Bukkit.getScheduler().runTaskTimerAsynchronously(
                                this.plugin,
                                this.plugin.getUpdater()::checkVersion,
                                Bot.TICKS_BETWEEN_VERSION_CHECKING,
                                Bot.TICKS_BETWEEN_VERSION_CHECKING
                        ),
                0
        );
        this.retrieveNetworks();
        this.loadMenus();
        this.checkForUnlinkedUsersInLobby();
        this.updateVoiceState();
        this.plugin.updateStatus(startup);
        if (sender != null && this.jda != null) {
            if (this.isReady) {
                sender.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.bot-connected"));
            } else {
                sender.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.bot-connected-incomplete-configuration-discord"));
            }
        }
    }

    private void setDefaultAvatar() {
        if (this.jda.getSelfUser().getDefaultAvatarUrl().equals(this.jda.getSelfUser().getEffectiveAvatarUrl())) {
            try {
                this.jda.getSelfUser().getManager()
                        .setAvatar(Icon.from(new URL("https://www.spigotmc.org/data/resource_icons/82/82861.jpg?1597701409")
                                .openStream())).queue();
            } catch (IOException ignored) {
            }
        }
    }

    public void updateGuildUniquenessStatus() {
        this.isOnMultipleGuilds = this.jda.getGuilds().size() > 1;
    }

    public void checkForValidLobby() {
        if (this.plugin.getConfiguration().getLobby() == null
                && this.plugin.getConfiguration().getFile().contains(ConfigurationField.LOBBY_ID.toString())) {
            this.plugin.getConfiguration().getFile().set(ConfigurationField.LOBBY_ID.toString(), null);
            this.plugin.getConfiguration().saveFile();
        }
    }

    private void registerPermanentListeners() {
        this.jda.addEventListener(
                new ReconnectedListener(this.plugin),
                new GuildJoinListener(this.plugin),
                new GuildLeaveListener(this.plugin),
                new GuildMemberRoleAddListener(this.plugin),
                new GuildMemberRoleRemoveListener(this.plugin),
                new RoleUpdatePermissionsListener(this.plugin),
                new PrivateMessageReceivedListener(this.plugin),
                new GuildMessageReceivedListener(this.plugin),
                new GuildMessageDeleteListener(this.plugin.getConfigurationMenu()),
                new VoiceChannelDeleteListener(this.plugin),
                new VoiceChannelUpdateParentListener(this.plugin),
                new ConfigureCommand(this.plugin),
                new InviteCommand(this.plugin),
                new LinkCommand(this.plugin),
                new UnlinkCommand(this.plugin),
                new ButtonClickListener(this.plugin),
                new SelectMenuListener(this.plugin)
        );
    }

    public void registerListeners() {
        this.getJda().addEventListener(
                new GuildVoiceJoinListener(this.plugin),
                new GuildVoiceLeaveListener(this.plugin),
                new GuildVoiceMoveListener(this.plugin),
                new net.clementraynaud.skoice.listeners.channel.voice.network.VoiceChannelDeleteListener()
        );
    }

    public void unregisterListeners() {
        this.getJda().removeEventListener(
                new GuildVoiceJoinListener(this.plugin),
                new GuildVoiceLeaveListener(this.plugin),
                new GuildVoiceMoveListener(this.plugin),
                new net.clementraynaud.skoice.listeners.channel.voice.network.VoiceChannelDeleteListener()
        );
    }

    public void checkForUnlinkedUsersInLobby() {
        VoiceChannel lobby = this.plugin.getConfiguration().getLobby();
        if (lobby != null) {
            for (Member member : lobby.getMembers()) {
                this.checkMemberStatus(member);
            }
        }
    }

    public void checkMemberStatus(Member member) {
        String minecraftId = MapUtil.getKeyFromValue(this.plugin.getConfiguration().getLinks(), member.getId());
        if (minecraftId == null) {
            member.getUser().openPrivateChannel().complete()
                    .sendMessage(this.menus.get("account-not-linked").build())
                    .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
        } else {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(minecraftId));
            if (player.isOnline() && player.getPlayer() != null) {
                this.plugin.getEligiblePlayers().add(player.getUniqueId());
                player.getPlayer().sendMessage(this.plugin.getLang().getMessage("minecraft.chat.player.connected-to-proximity-voice-chat"));
            }
        }
    }

    public void updateVoiceState() {
        Guild guild = this.plugin.getConfiguration().getGuild();
        if (guild != null) {
            for (VoiceChannel channel : guild.getVoiceChannels()) {
                for (Member member : channel.getMembers()) {
                    new UpdateVoiceStateTask(this.plugin.getConfiguration(), member, channel).run();
                }
            }
        }
    }

    private void retrieveNetworks() {
        Category category = this.plugin.getConfiguration().getCategory();
        if (category != null) {
            category.getVoiceChannels().stream()
                    .filter(channel -> {
                        try {
                            UUID.fromString(channel.getName());
                            return true;
                        } catch (IllegalArgumentException e) {
                            return false;
                        }
                    })
                    .forEach(channel -> Network.getNetworks().add(new Network(this.plugin.getConfiguration(), channel.getId())));
        }
    }

    public void updateActivity() {
        Activity activity = this.getJda().getPresence().getActivity();
        if (this.isReady() && !Objects.equals(activity, Activity.listening("/link"))) {
            this.getJda().getPresence().setActivity(Activity.listening("/link"));
        } else if (!this.isReady() && !Objects.equals(activity, Activity.listening("/configure"))) {
            this.getJda().getPresence().setActivity(Activity.listening("/configure"));
        }
    }

    private void loadMenus() {
        this.loadMenuFields();
        YamlConfiguration menusYaml = ConfigurationUtils.loadResource(this.getClass().getName(), "menus/menus.yml");
        for (String menu : menusYaml.getKeys(false)) {
            if ("configuration".equals(menu) || "linking-process".equals(menu) || "error".equals(menu)) {
                for (String subMenu : menusYaml.getConfigurationSection(menu).getKeys(false)) {
                    if (!"emoji".equals(subMenu)) {
                        this.menus.put(subMenu, new Menu(this.plugin,
                                menusYaml.getConfigurationSection(menu + "." + subMenu)));
                    }
                }
            } else {
                this.menus.put(menu, new Menu(this.plugin, menusYaml.getConfigurationSection(menu)));
            }
        }
    }

    private void loadMenuFields() {
        YamlConfiguration fieldsYaml = ConfigurationUtils.loadResource(this.getClass().getName(), "menus/fields.yml");
        for (String field : fieldsYaml.getKeys(false)) {
            this.fields.put(field, new MenuField(this.plugin, fieldsYaml.getConfigurationSection(field)));
        }
    }

    public JDA getJda() {
        return this.jda;
    }

    public void setReady(boolean ready) {
        this.isReady = ready;
    }

    public boolean isReady() {
        return this.isReady;
    }

    public boolean isOnMultipleGuilds() {
        return this.isOnMultipleGuilds;
    }

    public MenuField getField(String field) {
        return this.fields.get(field);
    }

    public Map<String, Menu> getMenus() {
        return this.menus;
    }

    public Menu getMenu(String menu) {
        return this.menus.get(menu);
    }
}
