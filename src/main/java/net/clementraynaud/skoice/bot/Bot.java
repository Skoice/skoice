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
import net.clementraynaud.skoice.config.ConfigurationField;
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.menus.MenuField;
import net.clementraynaud.skoice.system.Network;
import net.clementraynaud.skoice.tasks.UpdateNetworksTask;
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
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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
    private final Skoice plugin;
    private JDA jda;
    private BotStatus status;

    public Bot(Skoice plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        this.connect(null);
    }

    public void connect(CommandSender sender) {
        if (this.plugin.getConfiguration().getFile().contains(ConfigurationField.TOKEN.toString())) {
            byte[] base64TokenBytes;
            try {
                base64TokenBytes = Base64.getDecoder().decode(this.plugin.getConfiguration().getFile().getString(ConfigurationField.TOKEN.toString()));
                for (int i = 0; i < base64TokenBytes.length; i++) {
                    base64TokenBytes[i]--;
                }
            } catch (IllegalArgumentException e) {
                base64TokenBytes = new byte[0];
            }
            try {
                this.jda = JDABuilder.createDefault(new String(base64TokenBytes))
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

    public void setup(CommandSender sender) {
        this.setDefaultAvatar();
        this.plugin.getConfigurationMenu().delete();
        this.checkForValidVoiceChannel();
        this.jda.getGuilds().forEach(guild -> {
            new BotCommands(this.plugin).register(guild);
            if (guild.getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
                guild.getPublicRole().getManager().givePermissions(Permission.USE_APPLICATION_COMMANDS).queue();
            }
        });
        this.plugin.getListenerManager().registerPermanentBotListeners();
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () ->
                        this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                                this.plugin,
                                new UpdateNetworksTask(this.plugin)::run,
                                0,
                                10
                        ),
                0
        );
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () ->
                        this.plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                                this.plugin,
                                this.plugin.getUpdater()::checkVersion,
                                Bot.TICKS_BETWEEN_VERSION_CHECKING,
                                Bot.TICKS_BETWEEN_VERSION_CHECKING
                        ),
                0
        );
        this.retrieveNetworks();
        this.loadMenus();
        this.checkForUnlinkedUsers();
        this.updateVoiceState();
        this.plugin.getListenerManager().update();
        if (sender != null && this.jda != null) {
            if (this.getStatus() == BotStatus.READY) {
                sender.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.bot-connected"));
            } else if (this.getStatus() == BotStatus.NO_GUILD) {
                this.sendNoGuildAlert((Player) sender);
            } else {
                sender.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.bot-connected-incomplete-configuration-discord"));
            }
        }
    }

    public void setup() {
        this.setup(null);
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

    public void checkForValidVoiceChannel() {
        if (this.plugin.getConfiguration().getVoiceChannel() == null
                && this.plugin.getConfiguration().getFile().contains(ConfigurationField.VOICE_CHANNEL_ID.toString())) {
            this.plugin.getConfiguration().getFile().set(ConfigurationField.VOICE_CHANNEL_ID.toString(), null);
            this.plugin.getConfiguration().saveFile();
        }
    }

    public void checkForUnlinkedUsers() {
        VoiceChannel voiceChannel = this.plugin.getConfiguration().getVoiceChannel();
        if (voiceChannel != null) {
            for (Member member : voiceChannel.getMembers()) {
                this.checkMemberStatus(member);
            }
        }
    }

    public void checkMemberStatus(Member member) {
        String minecraftId = MapUtil.getKeyFromValue(this.plugin.getLinksFileStorage().getLinks(), member.getId());
        if (minecraftId == null) {
            member.getUser().openPrivateChannel().queue(channel ->
                    channel.sendMessage(this.menus.get("account-not-linked").build())
                            .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER))
            );
        } else {
            OfflinePlayer player = this.plugin.getServer().getOfflinePlayer(UUID.fromString(minecraftId));
            if (player.isOnline() && player.getPlayer() != null) {
                UpdateNetworksTask.getEligiblePlayers().add(player.getUniqueId());
                player.getPlayer().sendMessage(this.plugin.getLang().getMessage("minecraft.chat.player.connected"));
            }
        }
    }

    public void updateVoiceState() {
        Guild guild = this.plugin.getConfiguration().getGuild();
        if (guild != null) {
            for (VoiceChannel channel : guild.getVoiceChannels()) {
                for (Member member : channel.getMembers()) {
                    new UpdateVoiceStateTask(this.plugin.getConfiguration(), this.plugin.getTempFileStorage(), member, channel).run();
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

    public void updateStatus() {
        this.status = BotStatus.UNCHECKED;
        if (!this.plugin.getConfiguration().getFile().contains(ConfigurationField.TOKEN.toString())) {
            this.status = BotStatus.NO_TOKEN;
            this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.no-token"));
        } else if (this.getJDA() != null) {
            if (this.getJDA().getGuilds().isEmpty()) {
                this.status = BotStatus.NO_GUILD;
                this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.no-guild",
                        this.getJDA().getSelfUser().getApplicationId()));
            } else if (this.getJDA().getGuilds().size() > 1) {
                this.status = BotStatus.MULTIPLE_GUILDS;
                this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.multiple-guilds"));
            } else if (!this.getJDA().getGuilds().get(0).getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
                this.status = BotStatus.MISSING_PERMISSION;
                this.plugin.getLogger().severe(this.plugin.getLang().getMessage("logger.error.missing-permission",
                        this.getJDA().getSelfUser().getApplicationId()));
            } else if (!this.plugin.getConfiguration().getFile().contains(ConfigurationField.VOICE_CHANNEL_ID.toString())) {
                this.status = BotStatus.NO_VOICE_CHANNEL;
                this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.no-voice-channel"));
            } else if (!this.plugin.getConfiguration().getFile().contains(ConfigurationField.HORIZONTAL_RADIUS.toString())
                    || !this.plugin.getConfiguration().getFile().contains(ConfigurationField.VERTICAL_RADIUS.toString())) {
                this.status = BotStatus.NO_RADIUS;
                this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.no-radius"));
            } else {
                this.status = BotStatus.READY;
            }
            this.updateActivity();
        }
    }

    public void updateActivity() {
        Activity activity = this.getJDA().getPresence().getActivity();
        if (this.getStatus() == BotStatus.READY && !Objects.equals(activity, Activity.listening("/link"))) {
            this.getJDA().getPresence().setActivity(Activity.listening("/link"));
        } else if (this.getStatus() != BotStatus.READY && !Objects.equals(activity, Activity.listening("/configure"))) {
            this.getJDA().getPresence().setActivity(Activity.listening("/configure"));
        }
    }

    public void sendNoGuildAlert(Player player) {
        try {
            TextComponent invitePage = new TextComponent(this.plugin.getLang().getMessage("minecraft.interaction.this-page"));
            MessageUtil.setHoverEvent(invitePage,
                    this.plugin.getLang().getMessage("minecraft.interaction.link",
                            "https://discord.com/api/oauth2/authorize?client_id="
                                    + this.plugin.getBot().getJDA().getSelfUser().getApplicationId()
                                    + "&permissions=8&scope=bot%20applications.commands"));
            invitePage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                    "https://discord.com/api/oauth2/authorize?client_id="
                            + this.plugin.getBot().getJDA().getSelfUser().getApplicationId()
                            + "&permissions=8&scope=bot%20applications.commands"));
            player.spigot().sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.no-guild-interactive", invitePage));
        } catch (NoSuchMethodError e) {
            player.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.no-guild"),
                    this.plugin.getBot().getJDA().getSelfUser().getApplicationId());
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

    public JDA getJDA() {
        return this.jda;
    }

    public BotStatus getStatus() {
        return this.status;
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
