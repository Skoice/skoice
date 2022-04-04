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

import net.clementraynaud.skoice.commands.ConfigureCommand;
import net.clementraynaud.skoice.commands.InviteCommand;
import net.clementraynaud.skoice.commands.interaction.ButtonInteraction;
import net.clementraynaud.skoice.commands.interaction.Response;
import net.clementraynaud.skoice.commands.interaction.LobbySelection;
import net.clementraynaud.skoice.commands.interaction.SelectMenuInteraction;
import net.clementraynaud.skoice.listeners.ReconnectedListener;
import net.clementraynaud.skoice.listeners.guild.GuildJoinListener;
import net.clementraynaud.skoice.listeners.guild.GuildLeaveListener;
import net.clementraynaud.skoice.listeners.message.guild.GuildMessageDeleteListener;
import net.clementraynaud.skoice.listeners.message.guild.GuildMessageReceivedListener;
import net.clementraynaud.skoice.lang.LoggerLang;
import net.clementraynaud.skoice.lang.DiscordLang;
import net.clementraynaud.skoice.lang.MinecraftLang;
import net.clementraynaud.skoice.commands.LinkCommand;
import net.clementraynaud.skoice.commands.UnlinkCommand;
import net.clementraynaud.skoice.listeners.message.priv.PrivateMessageReceivedListener;
import net.clementraynaud.skoice.tasks.UpdateNetworksTask;
import net.clementraynaud.skoice.system.Network;
import net.clementraynaud.skoice.util.MessageUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.system.Network.networks;
import static net.clementraynaud.skoice.config.Config.*;

public class Bot {

    private static final List<ListenerAdapter> LISTENERS = Arrays.asList(
            new ReconnectedListener(), new GuildJoinListener(), new GuildLeaveListener(), new PrivateMessageReceivedListener(),
            new GuildMessageReceivedListener(), new GuildMessageDeleteListener(), new LobbySelection(),
            new ConfigureCommand(), new InviteCommand(), new LinkCommand(), new UnlinkCommand(),
            new ButtonInteraction(), new SelectMenuInteraction());
    private static final int TICKS_BETWEEN_VERSION_CHECKING = 720000;

    private static JDA jda;

    public Bot() {
        connectBot(true, null);
    }

    public static JDA getJda() {
        return jda;
    }

    public static void setJda(JDA jda) {
        Bot.jda = jda;
    }

    public void connectBot(boolean startup, CommandSender sender) {
        if (getPlugin().isTokenSet()) {
            byte[] base64TokenBytes = Base64.getDecoder().decode(getPlugin().getConfig().getString(TOKEN_FIELD));
            for (int i = 0; i < base64TokenBytes.length; i++) {
                base64TokenBytes[i]--;
            }
            try {
                setJda(JDABuilder.createDefault(new String(base64TokenBytes))
                        .enableIntents(GatewayIntent.GUILD_MEMBERS)
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
                        .build()
                        .awaitReady());
                getPlugin().getLogger().info(LoggerLang.BOT_CONNECTED_INFO.toString());
            } catch (LoginException e) {
                if (sender == null) {
                    getPlugin().getLogger().severe(LoggerLang.BOT_COULD_NOT_CONNECT_ERROR.toString());
                } else {
                    sender.sendMessage(MinecraftLang.BOT_COULD_NOT_CONNECT.toString());
                    getPlugin().getConfig().set(TOKEN_FIELD, null);
                    getPlugin().saveConfig();
                }
            } catch (IllegalStateException e) {

            } catch (ErrorResponseException e) {
                if (sender == null) {
                    getPlugin().getLogger().severe(LoggerLang.DISCORD_API_TIMED_OUT_ERROR.toString());
                } else {
                    try {
                        TextComponent discordStatusPage = new TextComponent("§bpage");
                        MessageUtil.setHoverEvent(discordStatusPage, "§8☀ §bOpen in web browser: §7https://discordstatus.com");
                        discordStatusPage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discordstatus.com"));
                        sender.spigot().sendMessage(new ComponentBuilder("§dSkoice §8• §7Discord seems to §cbe experiencing an outage§7. Find more information on this ")
                                .append(discordStatusPage)
                                .append("§7.").event((HoverEvent) null).create());
                    } catch (NoSuchMethodError e2) {
                        sender.sendMessage(MinecraftLang.DISCORD_API_TIMED_OUT_LINK.toString());
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (jda != null) {
                setDefaultAvatar();
                new Response().deleteMessage();
                updateGuildUniquenessStatus();
                checkForValidLobby();
                checkForUnlinkedUsersInLobby();
                jda.getGuilds().forEach(new Commands()::register);
                jda.addEventListener(LISTENERS.toArray());
                Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
                                Bukkit.getScheduler().runTaskTimerAsynchronously(
                                        getPlugin(),
                                        new UpdateNetworksTask()::run,
                                        0,
                                        10
                                ),
                        0
                );
                Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
                                Bukkit.getScheduler().runTaskTimerAsynchronously(
                                        getPlugin(),
                                        getPlugin()::checkVersion,
                                        TICKS_BETWEEN_VERSION_CHECKING,
                                        TICKS_BETWEEN_VERSION_CHECKING
                                ),
                        0
                );
                retrieveNetworks();
            }
        }
        getPlugin().updateConfigurationStatus(startup);
        if (sender != null && jda != null) {
            if (getPlugin().isBotReady()) {
                sender.sendMessage(MinecraftLang.BOT_CONNECTED.toString());
            } else {
                sender.sendMessage(MinecraftLang.BOT_CONNECTED_INCOMPLETE_CONFIGURATION_DISCORD.toString());
            }
        }
    }

    private void setDefaultAvatar() {
        if (jda.getSelfUser().getDefaultAvatarUrl().equals(jda.getSelfUser().getEffectiveAvatarUrl()))
            try {
                jda.getSelfUser().getManager()
                        .setAvatar(Icon.from(new URL("https://www.spigotmc.org/data/resource_icons/82/82861.jpg?1597701409").openStream())).queue();
            } catch (IOException ignored) {
            }
    }

    public void updateGuildUniquenessStatus() {
        getPlugin().setGuildUnique(getJda().getGuilds().size() <= 1);
    }

    public void checkForValidLobby() {
        if (getLobby() == null && getPlugin().getConfig().contains(LOBBY_ID_FIELD)) {
            getPlugin().getConfig().set(LOBBY_ID_FIELD, null);
            getPlugin().saveConfig();
        }
    }

    public void checkForUnlinkedUsersInLobby() {
        VoiceChannel lobby = getLobby();
        if (lobby != null) {
            for (Member member : lobby.getMembers()) {
                String minecraftID = getKeyFromValue(getLinkMap(), member.getId());
                if (minecraftID == null) {
                    EmbedBuilder embed = new EmbedBuilder().setTitle(":link: " + DiscordLang.LINKING_PROCESS_EMBED_TITLE)
                            .setColor(Color.RED);
                    Guild guild = getGuild();
                    if (guild != null) {
                        embed.addField(":warning: " + DiscordLang.ACCOUNT_NOT_LINKED_FIELD_TITLE,
                                String.format(DiscordLang.ACCOUNT_NOT_LINKED_FIELD_ALTERNATIVE_DESCRIPTION.toString(), guild.getName()), false);
                    } else {
                        embed.addField(":warning: " + DiscordLang.ACCOUNT_NOT_LINKED_FIELD_TITLE, DiscordLang.ACCOUNT_NOT_LINKED_FIELD_GENERIC_ALTERNATIVE_DESCRIPTION.toString(), false);
                    }
                    member.getUser().openPrivateChannel().complete()
                            .sendMessageEmbeds(embed.build())
                            .queue(null, new ErrorHandler().ignore(ErrorResponse.CANNOT_SEND_TO_USER));
                }
            }
        }
    }

    private void retrieveNetworks() {
        Category category = getCategory();
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
                    .forEach(channel -> networks.add(new Network(channel.getId())));
        }
    }
}
