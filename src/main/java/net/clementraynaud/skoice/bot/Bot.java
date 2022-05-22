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
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.listeners.interaction.ButtonClickListener;
import net.clementraynaud.skoice.menus.ConfigurationMenu;
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
import net.clementraynaud.skoice.menus.MenuType;
import net.clementraynaud.skoice.tasks.UpdateNetworksTask;
import net.clementraynaud.skoice.system.Network;
import net.clementraynaud.skoice.util.MapUtil;
import net.clementraynaud.skoice.util.MessageUtil;
import net.clementraynaud.skoice.util.UpdateUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Bot {

    private static final int TICKS_BETWEEN_VERSION_CHECKING = 720000;

    private YamlConfiguration fieldsYaml;
    private final Map<String, MenuField> fields = new HashMap<>();
    private YamlConfiguration menusYaml;
    private final Map<String, Menu> menus = new LinkedHashMap<>();

    private JDA jda;
    private boolean isReady;
    private boolean isOnMultipleGuilds;

    private final Skoice plugin;

    public Bot(Skoice plugin) {
        this.plugin = plugin;
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
        if (this.plugin.readConfig().getFile().contains(ConfigField.TOKEN.get())) {
            byte[] base64TokenBytes = Base64.getDecoder().decode(this.plugin.readConfig().getFile().getString(ConfigField.TOKEN.get()));
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
                    this.plugin.readConfig().getFile().set(ConfigField.TOKEN.get(), null);
                    this.plugin.readConfig().saveFile();
                }
            } catch (IllegalStateException e) {

            } catch (ErrorResponseException e) {
                if (sender == null) {
                    this.plugin.getLogger().severe(this.plugin.getLang().getMessage("logger.error.discord-api-timed-out"));
                } else {
                    try {
                        TextComponent discordStatusPage = new TextComponent("§bpage");
                        MessageUtil.setHoverEvent(discordStatusPage, "§8☀ §bOpen in web browser: §7https://discordstatus.com");
                        discordStatusPage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discordstatus.com"));
                        sender.spigot().sendMessage(new ComponentBuilder("§dSkoice §8• §7Discord seems to §cbe experiencing an outage§7. Find more information on this ")
                                .append(discordStatusPage)
                                .append("§7.").event((HoverEvent) null).create());
                    } catch (NoSuchMethodError e2) {
                        sender.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.error.discord-api-timed-out-link"));
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
        this.jda.getGuilds().forEach(new Commands(this.plugin)::register);
        this.jda.addEventListener(new ReconnectedListener(this.plugin),
                new GuildJoinListener(this.plugin),
                new GuildLeaveListener(this.plugin),
                new PrivateMessageReceivedListener(this.plugin.readConfig(), this.plugin.getLang(), this),
                new GuildMessageReceivedListener(this.plugin.readConfig(), this.plugin.getLang(), this, configurationMenu),
                new GuildMessageDeleteListener(configurationMenu),
                new VoiceChannelDeleteListener(this.plugin),
                new VoiceChannelUpdateParentListener(this.plugin),
                new ConfigureCommand(this.plugin.readConfig(), this.plugin.getLang(), this, configurationMenu),
                new InviteCommand(this.plugin.readConfig(), this.plugin.getLang(), this),
                new LinkCommand(this.plugin.readConfig(), this.plugin.getLang(), this),
                new UnlinkCommand(this.plugin.readConfig(), this.plugin.getLang(), this),
                new ButtonClickListener(this.plugin.readConfig(), this.plugin.getLang(), this, configurationMenu),
                new SelectMenuListener(this.plugin));
        Bukkit.getScheduler().runTaskLater(this.plugin, () ->
                        Bukkit.getScheduler().runTaskTimerAsynchronously(
                                this.plugin,
                                new UpdateNetworksTask(this.plugin.readConfig(), this.plugin.getLang(), this.plugin.getEligiblePlayers())::run,
                                0,
                                10
                        ),
                0
        );
        Bukkit.getScheduler().runTaskLater(this.plugin, () ->
                        Bukkit.getScheduler().runTaskTimerAsynchronously(
                                this.plugin,
                                new UpdateUtil(this.plugin, Skoice.RESSOURCE_ID, this.plugin.getLang().getMessage("logger.warning.outdated-version"))::checkVersion,
                                Bot.TICKS_BETWEEN_VERSION_CHECKING,
                                Bot.TICKS_BETWEEN_VERSION_CHECKING
                        ),
                0
        );
        this.retrieveNetworks();
        this.loadFields();
        this.loadMenus();
        this.checkForUnlinkedUsersInLobby();
        this.plugin.updateConfigurationStatus(startup);
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
        if (this.plugin.readConfig().getLobby() == null && this.plugin.readConfig().getFile().contains(ConfigField.LOBBY_ID.get())) {
            this.plugin.readConfig().getFile().set(ConfigField.LOBBY_ID.get(), null);
            this.plugin.readConfig().saveFile();
        }
    }

    public void checkForUnlinkedUsersInLobby() {
        VoiceChannel lobby = this.plugin.readConfig().getLobby();
        if (lobby != null) {
            for (Member member : lobby.getMembers()) {
                String minecraftID = new MapUtil().getKeyFromValue(this.plugin.readConfig().getLinks(), member.getId());
                if (minecraftID == null) {
                    member.getUser().openPrivateChannel().complete()
                            .sendMessage(new Menu(this.menusYaml.getConfigurationSection("linking-process"),
                                    Collections.singleton(this.fields.get("account-not-linked").toField(this.plugin.getLang())),
                                    MenuType.ERROR)
                                    .toMessage(this.plugin.readConfig(), this.plugin.getLang(), this))
                            .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
                }
            }
        }
    }

    private void retrieveNetworks() {
        Category category = this.plugin.readConfig().getCategory();
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
                    .forEach(channel -> Network.networks.add(new Network(this.plugin.readConfig(), channel.getId())));
        }
    }

    private void loadFields() {
        InputStreamReader fieldsFile = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("menus/fields.yml"));
        this.fieldsYaml = YamlConfiguration.loadConfiguration(fieldsFile);
        for (String field : this.fieldsYaml.getConfigurationSection("startup").getKeys(false)) {
            this.fields.put(field, new MenuField(this.fieldsYaml.getConfigurationSection("startup." + field)));
        }
    }

    public YamlConfiguration getFieldsYaml() {
        return this.fieldsYaml;
    }

    private void loadMenus() {
        InputStreamReader menusFile = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("menus/menus.yml"));
        this.menusYaml = YamlConfiguration.loadConfiguration(menusFile);
        Set<MessageEmbed.Field> menuFields = new LinkedHashSet<>();
        for (String menu : this.menusYaml.getConfigurationSection("startup").getKeys(false)) {
            for (String field : this.menusYaml.getStringList("startup." + menu + ".fields")) {
                menuFields.add(this.fields.get(field).toField(this.plugin.getLang()));
            }
            this.menus.put(menu, new Menu(this.menusYaml.getConfigurationSection("startup." + menu), new LinkedHashSet<>(menuFields)));
            menuFields.clear();
        }
    }

    public YamlConfiguration getMenusYaml() {
        return this.menusYaml;
    }
}
