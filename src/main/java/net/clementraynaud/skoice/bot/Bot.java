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
import net.clementraynaud.skoice.config.Config;
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.lang.Lang;
import net.clementraynaud.skoice.listeners.interaction.ButtonClickListener;
import net.clementraynaud.skoice.menus.ConfigurationMenu;
import net.clementraynaud.skoice.menus.MenuEmoji;
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
import net.clementraynaud.skoice.system.EligiblePlayers;
import net.clementraynaud.skoice.tasks.UpdateNetworksTask;
import net.clementraynaud.skoice.system.Network;
import net.clementraynaud.skoice.util.MapUtil;
import net.clementraynaud.skoice.util.MessageUtil;
import net.clementraynaud.skoice.util.UpdateUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
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
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.security.auth.login.LoginException;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class Bot {

    private static final int TICKS_BETWEEN_VERSION_CHECKING = 720000;

    private final Map<String, MenuField> fields = new HashMap<>();
    private final Map<String, Menu> menus = new LinkedHashMap<>();

    private JDA jda;
    private boolean isReady;
    private boolean isOnMultipleGuilds;

    private final Skoice plugin;
    private final Config config;
    private final Lang lang;
    private final EligiblePlayers eligiblePlayers;

    public Bot(Skoice plugin, Config config, Lang lang, EligiblePlayers eligiblePlayers) {
        this.plugin = plugin;
        this.config = config;
        this.lang = lang;
        this.eligiblePlayers = eligiblePlayers;
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

    public Map<String, MenuField> getFields() {
        return this.fields;
    }

    public Map<String, Menu> getMenus() {
        return this.menus;
    }

    public void connect() {
        this.connect(null);
    }

    public void connect(CommandSender sender) {
        if (this.config.getFile().contains(ConfigField.TOKEN.get())) {
            byte[] base64TokenBytes = Base64.getDecoder().decode(this.config.getFile().getString(ConfigField.TOKEN.get()));
            for (int i = 0; i < base64TokenBytes.length; i++) {
                base64TokenBytes[i]--;
            }
            try {
                this.jda = JDABuilder.createDefault(new String(base64TokenBytes))
                        .enableIntents(GatewayIntent.GUILD_MEMBERS)
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
                        .build()
                        .awaitReady();
                this.plugin.getLogger().info(this.lang.getMessage("logger.info.bot-connected"));
            } catch (LoginException e) {
                if (sender == null) {
                    this.plugin.getLogger().severe(this.lang.getMessage("logger.error.bot-could-not-connect"));
                } else {
                    sender.sendMessage(this.lang.getMessage("minecraft.chat.configuration.bot-could-not-connect"));
                    this.config.getFile().set(ConfigField.TOKEN.get(), null);
                    this.config.saveFile();
                }
            } catch (IllegalStateException e) {

            } catch (ErrorResponseException e) {
                if (sender == null) {
                    this.plugin.getLogger().severe(this.lang.getMessage("logger.error.discord-api-timed-out"));
                } else {
                    try {
                        TextComponent discordStatusPage = new TextComponent("§bpage");
                        MessageUtil.setHoverEvent(discordStatusPage, "§8☀ §bOpen in web browser: §7https://discordstatus.com");
                        discordStatusPage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discordstatus.com"));
                        sender.spigot().sendMessage(new ComponentBuilder("§dSkoice §8• §7Discord seems to §cbe experiencing an outage§7. Find more information on this ")
                                .append(discordStatusPage)
                                .append("§7.").event((HoverEvent) null).create());
                    } catch (NoSuchMethodError e2) {
                        sender.sendMessage(this.lang.getMessage("minecraft.chat.error.discord-api-timed-out-link"));
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setup(ConfigurationMenu configurationMenu, boolean startup, CommandSender sender) {
        this.setDefaultAvatar();
        configurationMenu.deleteMessage();
        this.updateGuildUniquenessStatus();
        this.checkForValidLobby();
        this.checkForUnlinkedUsersInLobby();
        this.jda.getGuilds().forEach(new Commands(this.plugin, this.lang, this)::register);
        this.jda.addEventListener(new ReconnectedListener(this.plugin, this, configurationMenu),
                new GuildJoinListener(this.plugin, this.lang, this),
                new GuildLeaveListener(this.plugin, this),
                new PrivateMessageReceivedListener(this.config, this.lang, this),
                new GuildMessageReceivedListener(this.config, this.lang, this, configurationMenu),
                new GuildMessageDeleteListener(configurationMenu),
                new VoiceChannelDeleteListener(this.plugin, this.config, this.lang, this),
                new VoiceChannelUpdateParentListener(this.plugin, this.config, this.lang, this),
                new ConfigureCommand(this.config, this.lang, this, configurationMenu),
                new InviteCommand(this.lang),
                new LinkCommand(this.config, this.lang, this),
                new UnlinkCommand(this.config, this.lang),
                new ButtonClickListener(this.config, this.lang, this, configurationMenu),
                new SelectMenuListener(this.plugin, this.config, this.lang, this, configurationMenu));
        Bukkit.getScheduler().runTaskLater(this.plugin, () ->
                        Bukkit.getScheduler().runTaskTimerAsynchronously(
                                this.plugin,
                                new UpdateNetworksTask(this.config, this.lang, this.eligiblePlayers)::run,
                                0,
                                10
                        ),
                0
        );
        Bukkit.getScheduler().runTaskLater(this.plugin, () ->
                        Bukkit.getScheduler().runTaskTimerAsynchronously(
                                this.plugin,
                                new UpdateUtil(this.plugin, Skoice.RESSOURCE_ID, this.lang.getMessage("logger.warning.outdated-version"))::checkVersion,
                                Bot.TICKS_BETWEEN_VERSION_CHECKING,
                                Bot.TICKS_BETWEEN_VERSION_CHECKING
                        ),
                0
        );
        this.retrieveNetworks();
        this.loadFields();
        this.loadMenus();
        this.plugin.updateConfigurationStatus(startup);
        if (sender != null && this.jda != null) {
            if (this.isReady) {
                sender.sendMessage(this.lang.getMessage("minecraft.chat.configuration.bot-connected"));
            } else {
                sender.sendMessage(this.lang.getMessage("minecraft.chat.configuration.bot-connected-incomplete-configuration-discord"));
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
        if (this.config.getReader().getLobby() == null && this.config.getFile().contains(ConfigField.LOBBY_ID.get())) {
            this.config.getFile().set(ConfigField.LOBBY_ID.get(), null);
            this.config.saveFile();
        }
    }

    public void checkForUnlinkedUsersInLobby() {
        VoiceChannel lobby = this.config.getReader().getLobby();
        if (lobby != null) {
            for (Member member : lobby.getMembers()) {
                String minecraftID = new MapUtil().getKeyFromValue(this.config.getReader().getLinks(), member.getId());
                if (minecraftID == null) {
                    EmbedBuilder embed = new EmbedBuilder().setTitle(MenuEmoji.LINK + this.lang.getMessage("discord.menu.linking-process.title"))
                            .setColor(Color.RED);
                    Guild guild = this.config.getReader().getGuild();
                    if (guild != null) {
                        embed.addField(MenuEmoji.WARNING + this.lang.getMessage("discord.menu.linking-process.field.account-not-linked.title"),
                                this.lang.getMessage("discord.menu.linking-process.field.account-not-linked.alternative-description", guild.getName()), false);
                    } else {
                        embed.addField(MenuEmoji.WARNING + this.lang.getMessage("discord.menu.linking-process.field.account-not-linked.title"),
                                this.lang.getMessage("discord.menu.linking-process.field.account-not-linked.generic-alternative-description"), false);
                    }
                    member.getUser().openPrivateChannel().complete()
                            .sendMessageEmbeds(embed.build())
                            .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
                }
            }
        }
    }

    private void retrieveNetworks() {
        Category category = this.config.getReader().getCategory();
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
                    .forEach(channel -> Network.networks.add(new Network(this.config, channel.getId())));
        }
    }

    private void loadFields() {
        YamlConfiguration fieldsYaml;
        InputStreamReader fieldsFile = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("menus/fields.yml"));
        fieldsYaml = YamlConfiguration.loadConfiguration(fieldsFile);
        for (String field : fieldsYaml.getKeys(false)) {
            this.fields.put(field, new MenuField(fieldsYaml.getConfigurationSection(field)));
        }
    }

    private void loadMenus() {
        YamlConfiguration menusYaml;
        InputStreamReader menusFile = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("menus/menus.yml"));
        menusYaml = YamlConfiguration.loadConfiguration(menusFile);
        for (String menu : menusYaml.getKeys(false)) {
            this.menus.put(menu, new Menu(menusYaml.getConfigurationSection(menu)));
        }
    }
}
