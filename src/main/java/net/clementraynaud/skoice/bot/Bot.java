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
import net.clementraynaud.skoice.listeners.guild.member.GuildMemberRoleAddListener;
import net.clementraynaud.skoice.listeners.guild.member.GuildMemberRoleRemoveListener;
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
import net.clementraynaud.skoice.menus.MenuType;
import net.clementraynaud.skoice.tasks.UpdateNetworksTask;
import net.clementraynaud.skoice.system.Network;
import net.clementraynaud.skoice.tasks.UpdateVoiceStateTask;
import net.clementraynaud.skoice.util.MapUtil;
import net.clementraynaud.skoice.util.MessageUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
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
import java.util.Objects;
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
            } catch (ErrorResponseException e) {
                if (sender == null) {
                    this.plugin.getLogger().severe(this.plugin.getLang().getMessage("logger.error.discord-api-timed-out"));
                } else {
                    try {
                        TextComponent discordStatusPage = new TextComponent(this.plugin.getLang().getMessage("minecraft.interaction.this-page"));
                        MessageUtil.setHoverEvent(discordStatusPage, this.plugin.getLang().getMessage("minecraft.interaction.link", "https://discordstatus.com"));
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
        this.plugin.getConfigurationMenu().deleteMessage();
        this.updateGuildUniquenessStatus();
        this.checkForValidLobby();
        this.jda.getGuilds().forEach(new Commands(this.plugin)::register);
        this.registerListeners();
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
        this.loadFields();
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
        if (this.plugin.readConfig().getLobby() == null && this.plugin.readConfig().getFile().contains(ConfigField.LOBBY_ID.get())) {
            this.plugin.readConfig().getFile().set(ConfigField.LOBBY_ID.get(), null);
            this.plugin.readConfig().saveFile();
        }
    }

    private void registerListeners() {
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

    public void checkForUnlinkedUsersInLobby() {
        VoiceChannel lobby = this.plugin.readConfig().getLobby();
        if (lobby != null) {
            for (Member member : lobby.getMembers()) {
                String minecraftID = MapUtil.getKeyFromValue(this.plugin.readConfig().getLinks(), member.getId());
                if (minecraftID == null) {
                    member.getUser().openPrivateChannel().complete()
                            .sendMessage(new Menu(this.plugin, "linking-process",
                                    Collections.singleton(this.fields.get("account-not-linked")),
                                    MenuType.ERROR)
                                    .toMessage())
                            .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
                }
            }
        }
    }

    public void updateVoiceState() {
        Guild guild = this.plugin.readConfig().getGuild();
        if (guild != null) {
            for (VoiceChannel channel : guild.getVoiceChannels()) {
                for (Member member : channel.getMembers()) {
                    new UpdateVoiceStateTask(this.plugin.readConfig(), member, channel).run();
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
                    .forEach(channel -> Network.getNetworks().add(new Network(this.plugin.readConfig(), channel.getId())));
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

    private void loadFields() {
        InputStreamReader fieldsFile = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("menus/fields.yml"));
        this.fieldsYaml = YamlConfiguration.loadConfiguration(fieldsFile);
        for (String field : this.fieldsYaml.getConfigurationSection("startup").getKeys(false)) {
            this.fields.put(field, new MenuField(this.plugin, "startup." + field));
        }
    }

    private void loadMenus() {
        InputStreamReader menusFile = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("menus/menus.yml"));
        this.menusYaml = YamlConfiguration.loadConfiguration(menusFile);
        Set<MenuField> menuFields = new LinkedHashSet<>();
        for (String menu : this.menusYaml.getConfigurationSection("startup").getKeys(false)) {
            for (String field : this.menusYaml.getStringList("startup." + menu + ".fields")) {
                menuFields.add(this.fields.get(field));
            }
            this.menus.put(menu, new Menu(this.plugin, "startup." + menu, new LinkedHashSet<>(menuFields)));
            menuFields.clear();
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

    public YamlConfiguration getFieldsYaml() {
        return this.fieldsYaml;
    }

    public Map<String, MenuField> getFields() {
        return this.fields;
    }

    public YamlConfiguration getMenusYaml() {
        return this.menusYaml;
    }

    public Map<String, Menu> getMenus() {
        return this.menus;
    }
}
