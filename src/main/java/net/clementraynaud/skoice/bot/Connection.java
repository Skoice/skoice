/*
 * Copyright 2020, 2021 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
 * Copyright 2016, 2017, 2018, 2019, 2020, 2021 Austin "Scarsz" Shapiro
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

import net.clementraynaud.skoice.configuration.discord.LobbySelection;
import net.clementraynaud.skoice.configuration.discord.MessageManagement;
import net.clementraynaud.skoice.link.Link;
import net.clementraynaud.skoice.link.Unlink;
import net.clementraynaud.skoice.system.ChannelManagement;
import net.clementraynaud.skoice.system.Network;
import net.clementraynaud.skoice.util.Lang;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReconnectedEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.Base64;
import java.util.UUID;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.configuration.discord.MessageManagement.deleteConfigurationMessage;
import static net.clementraynaud.skoice.configuration.discord.MessageManagement.initializeDiscordIDDistanceMap;
import static net.clementraynaud.skoice.link.Link.initializeDiscordIDCodeMap;
import static net.clementraynaud.skoice.system.ChannelManagement.networks;
import static net.clementraynaud.skoice.util.DataGetters.*;

public class Connection extends ListenerAdapter {

    private static final int TICKS_BETWEEN_VERSION_CHECKING = 720000;

    private static JDA jda;

    public Connection() {
        connectBot(true, null);
    }

    public static JDA getJda() {
        return jda;
    }

    public static void setJda(JDA jda) {
        Connection.jda = jda;
    }

    public void connectBot(boolean startup, CommandSender sender) {
        if (getPlugin().isTokenSet()) {
            byte[] base64TokenBytes = Base64.getDecoder().decode(getPlugin().getConfigFile().getString("token"));
            for (int i = 0; i < base64TokenBytes.length; i++) {
                base64TokenBytes[i]--;
            }
            try {
                setJda(JDABuilder.createDefault(new String(base64TokenBytes))
                        .enableIntents(GatewayIntent.GUILD_MEMBERS)
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
                        .build()
                        .awaitReady());
                getPlugin().getLogger().info(Lang.Console.BOT_CONNECTED_INFO.print());
            } catch (LoginException e) {
                if (sender == null) {
                    getPlugin().getLogger().severe(Lang.Console.BOT_COULD_NOT_CONNECT_ERROR.print());
                } else {
                    sender.sendMessage(Lang.Minecraft.BOT_COULD_NOT_CONNECT.print());
                    getPlugin().getConfigFile().set("token", null);
                    getPlugin().saveConfig();
                }
            } catch (IllegalStateException e) {

            } catch (ErrorResponseException e) {

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (jda != null) {
                deleteConfigurationMessage();
                updateGuildUniquenessStatus();
                checkForValidLobby();
                checkForUnlinkedUsersInLobby();
                initializeDiscordIDCodeMap();
                initializeDiscordIDDistanceMap();
                jda.getGuilds().forEach(CommandRegistration::registerCommands);
                jda.addEventListener(this, new CommandRegistration(), new InviteCommand(), new LobbySelection(), new MessageManagement(), new Link(), new Unlink());
                Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
                                Bukkit.getScheduler().runTaskTimerAsynchronously(
                                        getPlugin(),
                                        ChannelManagement::tick,
                                        0,
                                        5
                                ),
                        0
                );
                Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
                                Bukkit.getScheduler().runTaskTimerAsynchronously(
                                        getPlugin(),
                                        getPlugin()::checkVersion,
                                        TICKS_BETWEEN_VERSION_CHECKING, // Delay before first run
                                        TICKS_BETWEEN_VERSION_CHECKING // Delay between every run
                                ),
                        0
                );
                if (getPlugin().getConfigFile().contains("lobby-id")) {
                    Category category = getDedicatedCategory();
                    if (category != null) {
                        category.getVoiceChannels().stream()
                                .filter(channel -> {
                                    try {
                                        //noinspection ResultOfMethodCallIgnored
                                        UUID.fromString(channel.getName());
                                        return true;
                                    } catch (Exception e) {
                                        return false;
                                    }
                                })
                                // temporarily add it as a network so it can be emptied and deleted
                                .forEach(channel -> networks.add(new Network(channel.getId())));
                    }
                }
            }
        }
        getPlugin().updateConfigurationStatus(startup);
        if (sender != null && jda != null) {
            if (getPlugin().isBotReady()) {
                sender.sendMessage(Lang.Minecraft.BOT_CONNECTED.print());
            } else {
                sender.sendMessage(Lang.Minecraft.BOT_CONNECTED_INCOMPLETE_CONFIGURATION_DISCORD.print());
            }
        }
    }

    public void updateGuildUniquenessStatus() {
        getPlugin().setGuildUnique(getJda().getGuilds().size() == 1);
    }

    private void checkForValidLobby() {
        if (getLobby() == null && getPlugin().getConfigFile().contains("lobby-id")) {
            getPlugin().getConfigFile().set("lobby-id", null);
            getPlugin().saveConfig();
        }
    }

    private void checkForUnlinkedUsersInLobby() {
        VoiceChannel lobby = getLobby();
        if (lobby != null) {
            for (Member member : lobby.getMembers()) {
                UUID minecraftID = getMinecraftID(member);
                if (minecraftID == null) {
                    EmbedBuilder embed = new EmbedBuilder().setTitle(":link: " + Lang.Discord.LINKING_PROCESS_EMBED_TITLE.print())
                            .setColor(Color.RED);
                    Guild guild = getGuild();
                    if (guild != null) {
                        embed.addField(":warning: " + Lang.Discord.ACCOUNT_NOT_LINKED_FIELD_TITLE.print(), Lang.Discord.ACCOUNT_NOT_LINKED_FIELD_ALTERNATIVE_DESCRIPTION.print().replace("{discordServer}", guild.getName()), false);
                    } else {
                        embed.addField(":warning: " + Lang.Discord.ACCOUNT_NOT_LINKED_FIELD_TITLE.print(), Lang.Discord.ACCOUNT_NOT_LINKED_FIELD_GENERIC_ALTERNATIVE_DESCRIPTION.print(), false);
                    }
                    try {
                        member.getUser().openPrivateChannel().complete()
                                .sendMessageEmbeds(embed.build()).queue(success -> {}, failure -> {});
                    } catch (ErrorResponseException ignored) {
                    }
                }
            }
        }
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
        if (!event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
            event.getMessage().replyEmbeds(new EmbedBuilder().setTitle(":warning: " + Lang.Discord.ERROR_EMBED_TITLE.print())
                    .addField(":no_entry: " + Lang.Discord.ILLEGAL_INTERACTION_FIELD_TITLE.print(), Lang.Discord.ILLEGAL_INTERACTION_FIELD_DESCRIPTION.print(), false)
                    .setColor(Color.RED).build()).queue();
        }
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        updateGuildUniquenessStatus();
        getPlugin().updateConfigurationStatus(false);
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        updateGuildUniquenessStatus();
        getPlugin().updateConfigurationStatus(false);
    }

    @Override
    public void onReconnected(@NotNull ReconnectedEvent event) {
        deleteConfigurationMessage();
        updateGuildUniquenessStatus();
        checkForValidLobby();
        checkForUnlinkedUsersInLobby();
        getPlugin().updateConfigurationStatus(false);
    }
}
