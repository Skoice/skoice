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

package net.clementraynaud.skoice.bot;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.commands.skoice.arguments.Argument;
import net.clementraynaud.skoice.menus.Menu;
import net.clementraynaud.skoice.menus.MenuField;
import net.clementraynaud.skoice.storage.config.ConfigField;
import net.clementraynaud.skoice.system.LinkedPlayer;
import net.clementraynaud.skoice.system.Network;
import net.clementraynaud.skoice.system.Networks;
import net.clementraynaud.skoice.tasks.UpdateNetworksTask;
import net.clementraynaud.skoice.tasks.UpdateVoiceStateTask;
import net.clementraynaud.skoice.util.ConfigurationUtil;
import net.clementraynaud.skoice.util.MapUtil;
import net.clementraynaud.skoice.util.PlayerUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.PermissionOverride;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Bot {

    private final Map<String, MenuField> fields = new HashMap<>();
    private final Map<String, Menu> menus = new LinkedHashMap<>();
    private final Skoice plugin;
    private JDA jda;
    private BotStatus status;
    private String guildId;

    public Bot(Skoice plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        this.connect(null);
    }

    public void connect(CommandSender sender) {
        if (this.plugin.getConfigYamlFile().contains(ConfigField.TOKEN.toString())) {
            this.plugin.getLogger().info(this.plugin.getLang().getMessage("logger.info.bot-connecting"));
            if (sender != null) {
                sender.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.bot-connecting"));
            }
            byte[] base64TokenBytes;
            try {
                base64TokenBytes = Base64.getDecoder().decode(this.plugin.getConfigYamlFile().getString(ConfigField.TOKEN.toString()));
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
            } catch (InvalidTokenException e) {
                this.plugin.getLogger().severe(this.plugin.getLang().getMessage("logger.error.bot-could-not-connect"));
                if (sender != null) {
                    sender.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.bot-could-not-connect"));
                    this.plugin.getConfigYamlFile().remove(ConfigField.TOKEN.toString());
                }
            } catch (ErrorResponseException e) {
                this.plugin.getLogger().severe(this.plugin.getLang().getMessage("logger.error.bot-timed-out"));
                if (sender != null) {
                    if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.TOOLTIPS.toString())) {
                        this.plugin.adventure().sender(sender).sendMessage(this.plugin.getLang().getMessage("minecraft.chat.error.bot-timed-out-interactive", this.plugin.getLang().getComponentMessage("minecraft.interaction.this-page")
                                        .hoverEvent(HoverEvent.showText(this.plugin.getLang().getComponentMessage("minecraft.interaction.link", "https://discordstatus.com")))
                                        .clickEvent(net.kyori.adventure.text.event.ClickEvent.openUrl("https://discordstatus.com"))
                                )
                        );
                    } else {
                        sender.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.error.bot-timed-out"));
                    }
                }
            } catch (IllegalStateException | InterruptedException ignored) {
            }
        }
    }

    public void setup(CommandSender sender) {
        this.setDefaultAvatar();
        this.plugin.getConfigurationMenu().delete();
        this.plugin.getConfigYamlFile().removeInvalidVoiceChannelId();
        this.updateGuild();
        this.plugin.getBotCommands().clearGuildCommands();
        this.plugin.getBotCommands().register();
        this.plugin.getBot().getJDA().getGuilds().forEach(guild -> {
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
        this.retrieveNetworks();
        this.loadMenus();
        this.updateVoiceState();
        this.plugin.getListenerManager().update();
        this.muteMembers();
        this.checkForUnlinkedUsers();
        this.refreshOnlineLinkedPlayers();
        if (sender != null) {
            if (this.getStatus() == BotStatus.READY) {
                sender.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.bot-connected"));
            } else if (this.getStatus() == BotStatus.NO_GUILD && sender instanceof Player) {
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
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                try (InputStream inputStream = new URL("https://clementraynaud.net/Skoice.jpeg").openStream()) {
                    Icon icon = Icon.from(inputStream);
                    this.jda.getSelfUser().getManager().setAvatar(icon).queue();
                } catch (IOException ignored) {
                }
            });
        }
    }

    public void muteMembers() {
        VoiceChannel voiceChannel = this.plugin.getConfigYamlFile().getVoiceChannel();
        if (voiceChannel == null) {
            return;
        }
        voiceChannel.getRolePermissionOverrides().forEach(override -> {
            if (!override.getDenied().contains(Permission.VOICE_SPEAK)) {
                override.getManager().deny(Permission.VOICE_SPEAK).queue();
            }
        });
        Role publicRole = voiceChannel.getGuild().getPublicRole();
        PermissionOverride permissionOverride = voiceChannel.getPermissionOverride(publicRole);
        if (permissionOverride == null) {
            voiceChannel.upsertPermissionOverride(publicRole).deny(Permission.VOICE_SPEAK).queue();
        }
    }

    public void checkForUnlinkedUsers() {
        if (this.getStatus() == BotStatus.READY) {
            VoiceChannel voiceChannel = this.plugin.getConfigYamlFile().getVoiceChannel();
            if (voiceChannel == null) {
                return;
            }
            for (Member member : voiceChannel.getMembers()) {
                this.checkMemberStatus(member);
            }
        }
    }

    public void checkMemberStatus(Member member) {
        String minecraftId = MapUtil.getKeyFromValue(this.plugin.getLinksYamlFile().getLinks(), member.getId());
        if (minecraftId == null) {
            member.getUser().openPrivateChannel().queue(channel ->
                    channel.sendMessage(this.menus.get("account-not-linked").build(this.plugin.getBot().getGuild().getName()))
                            .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER))
            );
        } else {
            OfflinePlayer player = this.plugin.getServer().getOfflinePlayer(UUID.fromString(minecraftId));
            if (player.isOnline() && player.getPlayer() != null) {
                player.getPlayer().sendMessage(this.plugin.getLang().getMessage("minecraft.chat.player.connected"));
            }
        }
    }

    public void refreshOnlineLinkedPlayers() {
        LinkedPlayer.getOnlineLinkedPlayers().clear();

        List<Player> onlinePlayers = PlayerUtil.getOnlinePlayers();
        for (Player player : onlinePlayers) {
            this.plugin.getLinksYamlFile().retrieveMember(player.getUniqueId(),
                    member -> LinkedPlayer.getOnlineLinkedPlayers().add(new LinkedPlayer(this.plugin, player, member.getId())));
        }
    }

    public void updateGuild() {
        List<Guild> guilds = this.jda.getGuilds();
        this.guildId = guilds.size() == 1 ? guilds.get(0).getId() : null;
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

    private void retrieveNetworks() {
        Category category = this.plugin.getConfigYamlFile().getCategory();
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
                    .forEach(channel -> Networks.add(new Network(this.plugin, channel.getId())));
        }
    }

    public void updateStatus() {
        if (this.jda == null) {
            this.status = BotStatus.NOT_CONNECTED;
            if (!this.plugin.getConfigYamlFile().contains(ConfigField.TOKEN.toString())) {
                this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.no-token"));
            }
        } else {
            if (this.guildId == null) {
                List<Guild> guilds = this.getJDA().getGuilds();
                if (guilds.isEmpty()) {
                    this.status = BotStatus.NO_GUILD;
                    this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.no-guild",
                            this.getJDA().getSelfUser().getApplicationId()));
                } else if (guilds.size() > 1) {
                    this.status = BotStatus.MULTIPLE_GUILDS;
                    this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.multiple-guilds"));
                }
            } else if (!this.getGuild().getSelfMember().hasPermission(Permission.ADMINISTRATOR)) {
                this.status = BotStatus.MISSING_PERMISSION;
                this.plugin.getLogger().severe(this.plugin.getLang().getMessage("logger.error.missing-permission",
                        this.getJDA().getSelfUser().getApplicationId()));
            } else if (!this.plugin.getConfigYamlFile().contains(ConfigField.VOICE_CHANNEL_ID.toString())) {
                this.status = BotStatus.NO_VOICE_CHANNEL;
                this.plugin.getLogger().warning(this.plugin.getLang().getMessage("logger.warning.no-voice-channel"));
            } else if (!this.plugin.getConfigYamlFile().contains(ConfigField.HORIZONTAL_RADIUS.toString())
                    || !this.plugin.getConfigYamlFile().contains(ConfigField.VERTICAL_RADIUS.toString())) {
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

    public void sendIncompleteConfigurationAlert(Player player, boolean sendIfPermissionMissing) {
        if (player.hasPermission(Argument.MANAGE_PERMISSION)) {
            if (this.plugin.getBot().getStatus() == BotStatus.NOT_CONNECTED) {
                if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.TOOLTIPS.toString())) {
                    this.plugin.adventure().player(player).sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.incomplete-configuration-operator-interactive",
                                    this.plugin.getLang().getComponentMessage("minecraft.interaction.here")
                                            .hoverEvent(HoverEvent.showText(this.plugin.getLang().getComponentMessage("minecraft.interaction.execute", "/skoice configure")))
                                            .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/skoice configure")),
                                    this.plugin.getLang().getComponentMessage("minecraft.interaction.here")
                                            .hoverEvent(HoverEvent.showText(this.plugin.getLang().getComponentMessage("minecraft.interaction.shortcut", "/skoice language")))
                                            .clickEvent(net.kyori.adventure.text.event.ClickEvent.suggestCommand("/skoice language "))
                            )
                    );
                } else {
                    player.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.incomplete-configuration-operator"));
                }
            } else if (this.plugin.getBot().getStatus() == BotStatus.NO_GUILD) {
                this.plugin.getBot().sendNoGuildAlert(player);
            } else {
                player.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.incomplete-configuration-operator-discord"));
            }
        } else if (sendIfPermissionMissing) {
            player.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.incomplete-configuration"));
        }
    }

    public void sendNoGuildAlert(Player player) {
        if (this.plugin.getConfigYamlFile().getBoolean(ConfigField.TOOLTIPS.toString())) {
            this.plugin.adventure().player(player).sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.no-guild-interactive", this.plugin.getLang().getComponentMessage("minecraft.interaction.this-page")
                            .hoverEvent(HoverEvent.showText(this.plugin.getLang().getComponentMessage("minecraft.interaction.link",
                                    "https://discord.com/api/oauth2/authorize?client_id="
                                            + this.plugin.getBot().getJDA().getSelfUser().getApplicationId()
                                            + "&permissions=8&scope=bot%20applications.commands"))
                            )
                            .clickEvent(net.kyori.adventure.text.event.ClickEvent.openUrl("https://discord.com/api/oauth2/authorize?client_id="
                                    + this.plugin.getBot().getJDA().getSelfUser().getApplicationId()
                                    + "&permissions=8&scope=bot%20applications.commands")
                            )
                    )
            );
        } else {
            player.sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.no-guild",
                    this.plugin.getBot().getJDA().getSelfUser().getApplicationId()));
        }
    }

    private void loadMenus() {
        this.loadMenuFields();
        YamlConfiguration menusYaml = ConfigurationUtil.loadResource(this.getClass().getName(), "menus/menus.yml");
        if (menusYaml == null) {
            return;
        }
        for (String menu : menusYaml.getKeys(false)) {
            ConfigurationSection menuSection = menusYaml.getConfigurationSection(menu);
            if (menuSection != null) {
                if ("configuration".equals(menu) || "linking-process".equals(menu) || "error".equals(menu)) {
                    for (String subMenu : menuSection.getKeys(false)) {
                        if (!"emoji".equals(subMenu)) {
                            ConfigurationSection subMenuSection = menusYaml.getConfigurationSection(menu + "." + subMenu);
                            if (subMenuSection != null) {
                                this.menus.put(subMenu, new Menu(this.plugin, subMenuSection));
                            }
                        }
                    }
                } else {
                    this.menus.put(menu, new Menu(this.plugin, menuSection));
                }
            }
        }
    }

    private void loadMenuFields() {
        YamlConfiguration fieldsYaml = ConfigurationUtil.loadResource(this.getClass().getName(), "menus/fields.yml");
        if (fieldsYaml == null) {
            return;
        }
        for (String field : fieldsYaml.getKeys(false)) {
            ConfigurationSection fieldSection = fieldsYaml.getConfigurationSection(field);
            if (fieldSection != null) {
                this.fields.put(field, new MenuField(this.plugin, fieldSection));
            }
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
