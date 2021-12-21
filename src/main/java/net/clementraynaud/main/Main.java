// Copyright 2020, 2021 Clément "carlodrift" Raynaud, rowisabeast
// Copyright 2016, 2017, 2018, 2019, 2020, 2021 Austin "Scarsz" Shapiro

// This file is part of Skoice.

// Skoice is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// Skoice is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Skoice.  If not, see <https://www.gnu.org/licenses/>.


package net.clementraynaud.main;

import net.clementraynaud.Skoice;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.channel.voice.VoiceChannelDeleteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Main extends ListenerAdapter implements CommandExecutor, Listener {

    private static final List<Permission> LOBBY_REQUIRED_PERMISSIONS = Arrays.asList(Permission.VIEW_CHANNEL, Permission.VOICE_MOVE_OTHERS);
    private static final List<Permission> CATEGORY_REQUIRED_PERMISSIONS = Arrays.asList(Permission.VIEW_CHANNEL, Permission.VOICE_MOVE_OTHERS, Permission.MANAGE_PERMISSIONS, Permission.MANAGE_CHANNEL);

    private final ReentrantLock lock = new ReentrantLock();
    private Set<UUID> dirtyPlayers = new HashSet<>();

    public Set<Network> getNetworks() {
        return networks;
    }

    private final Set<Network> networks = ConcurrentHashMap.newKeySet();
    private final Set<String> mutedUsers = ConcurrentHashMap.newKeySet();
    private final Map<String, Pair<String, CompletableFuture<Void>>> awaitingMoves = new ConcurrentHashMap<>();

    public static JDA jda;
    private static Skoice plugin;
    public HashMap<UUID, String> uuidCodeMap;
    public HashMap<UUID, String> uuidIdMap;

    public Main(Skoice plugin) {
        uuidCodeMap = new HashMap<>();
        uuidIdMap = new HashMap<>();
        this.plugin = plugin;
        try {
            jda = JDABuilder.createDefault(plugin.playerData.getString("token"))
                    .setActivity(Activity.listening("/link"))
                    .enableIntents(GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_MESSAGES)
                    .enableCache(CacheFlag.VOICE_STATE,
                            CacheFlag.MEMBER_OVERRIDES)
                    .setMemberCachePolicy(MemberCachePolicy.VOICE)
                    .setAutoReconnect(true)
                    .build()
                    .awaitReady();
            plugin.getLogger().info("JDA is running!");
        } catch (LoginException | InterruptedException e) {
            plugin.getLogger().severe("JDA ERROR: " + Arrays.toString(e.getStackTrace()));
        }
        if (Main.getGuild().retrieveCommands().complete().size() < 2) {
            Main.getGuild().upsertCommand("link", "Link your Discord account to Minecraft.")
                    .addOption(OptionType.STRING, "minecraft_username", "The username of the Minecraft account you want to link.", true).queue();
            Main.getGuild().upsertCommand("unlink", "Unlink your Discord account from Minecraft.").queue();
        }
        plugin.getCommand("link").setExecutor(this);
        plugin.getCommand("unlink").setExecutor(this);
        if (jda != null) {
            jda.addEventListener(this);
            Bukkit.getPluginManager().registerEvents(this, plugin);
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                            Bukkit.getScheduler().runTaskTimerAsynchronously(
                                    plugin,
                                    this::tick,
                                    0,
                                    5
                            ),
                    0
            );
            if (plugin.playerData.getBoolean("checkVersion.periodically.enabled")) {
                Bukkit.getScheduler().runTaskLater(plugin, () ->
                                Bukkit.getScheduler().runTaskTimerAsynchronously(
                                        plugin,
                                        plugin::checkVersion,
                                        plugin.playerData.getInt("checkVersion.periodically.delay"), // Delay before first run
                                        plugin.playerData.getInt("checkVersion.periodically.delay") // Delay between every run
                                ),
                        0
                );
            }
        } else {
            plugin.onDisable();
            return;
        }

        Category category = jda.getCategoryById(plugin.playerData.getString("categoryID"));
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
                    .forEach(channel -> {
                        // temporarily add it as a network so it can be emptied and deleted
                        networks.add(new Network(channel.getId()));
                    });
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
//            new PlaceHolderAPI(this).register();
        }
    }

    private void tick() {
        if (!lock.tryLock()) {
//            debug(Debug.VOICE, "Skipping voice module tick, a tick is already in progress");
            return;
        }
        try {
            Category category = getCategory();
            if (category == null) {
//                debug(Debug.VOICE, "Skipping voice module tick, category is null");
                return;
            }

            VoiceChannel lobbyChannel = getLobbyChannel();
            if (lobbyChannel == null) {
//                debug(Debug.VOICE, "Skipping voice module tick, lobby channel is null");
                return;
            }

            // check that the permissions are correct
            Member selfMember = lobbyChannel.getGuild().getSelfMember();
            Role publicRole = lobbyChannel.getGuild().getPublicRole();

            long currentTime = System.currentTimeMillis();


            boolean stop = false;
            for (Permission permission : LOBBY_REQUIRED_PERMISSIONS) {
                if (!selfMember.hasPermission(lobbyChannel, permission)) {
//                    if (log) .error("The bot doesn't have the \"" + permission.getName() + "\" permission in the voice lobby (" + lobbyChannel.getName() + ")");
                    stop = true;
                }
            }
            for (Permission permission : CATEGORY_REQUIRED_PERMISSIONS) {
                if (!selfMember.hasPermission(category, permission)) {
//                    if (log) .error("The bot doesn't have the \"" + permission.getName() + "\" permission in the voice category (" + category.getName() + ")");
                    stop = true;
                }
            }
            // we can't function & would throw exceptions
            if (stop) {
                return;
            }

            PermissionOverride lobbyPublicRoleOverride = lobbyChannel.getPermissionOverride(publicRole);
            if (lobbyPublicRoleOverride == null) {
                lobbyChannel.createPermissionOverride(publicRole).deny(Permission.VOICE_SPEAK).queue();
            } else if (!lobbyPublicRoleOverride.getDenied().contains(Permission.VOICE_SPEAK)) {
                lobbyPublicRoleOverride.getManager().deny(Permission.VOICE_SPEAK).queue();
            }

            // remove networks that have no voice channel
            networks.removeIf(network -> network.getChannel() == null && network.isInitialized());

            Set<Player> alivePlayers = getOnlinePlayers().stream()
                    .filter(player -> !player.isDead())
                    .collect(Collectors.toSet());

            Set<UUID> oldDirtyPlayers = dirtyPlayers;
            dirtyPlayers = new HashSet<>();
            for (UUID uuid : oldDirtyPlayers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;

                Member member = getMember(player.getUniqueId());

                if (member == null) {
//                   debug(Debug.VOICE, "Player " + player.getName() + " isn't linked, skipping voice checks");
                    continue;
                }

                if (member.getVoiceState() == null || member.getVoiceState().getChannel() == null) {
//                    .debug(Debug.VOICE, "Player " + player.getName() + " is not connected to voice");
                    continue;
                }

                VoiceChannel playerChannel = member.getVoiceState().getChannel();
                boolean isLobby = playerChannel.getId().equals(getLobbyChannel().getId());
                if (!isLobby && (playerChannel.getParent() == null || !playerChannel.getParent().getId().equals(getCategory().getId()))) {
//                    .debug(Debug.VOICE, "Player " + player.getName() + " was not in the voice lobby or category");
                    //member.mute(false).queue();
                    // cancel existing moves if they changed to a different channel
                    Pair<String, CompletableFuture<Void>> pair = awaitingMoves.get(member.getId());
                    if (pair != null) pair.getRight().cancel(false);
                    continue;
                }

                // add player to networks that they may have came into contact with
                // and combine multiple networks if the player is connecting them together
                networks.stream()
                        .filter(network -> network.isPlayerInRangeToBeAdded(player))
                        // combine multiple networks if player is bridging both of them together
                        .reduce((network1, network2) -> network1.size() > network2.size() ? network1.engulf(network2) : network2.engulf(network1))
                        // add the player to the network if they aren't in it already
                        .filter(network -> !network.contains(player.getUniqueId()))
                        .ifPresent(network -> {
//                            debug(Debug.VOICE, player.getName() + " has entered network " + network + "'s influence, connecting");
                            network.add(player.getUniqueId());
                        });

                // remove player from networks that they lost connection to
                networks.stream()
                        .filter(network -> network.contains(player.getUniqueId()))
                        .filter(network -> !network.isPlayerInRangeToStayConnected(player))
                        .forEach(network -> {
//                            .debug(Debug.VOICE, "Player " + player.getName() + " lost connection to " + network + ", disconnecting");
                            network.remove(player.getUniqueId());
                            if (network.size() == 1) network.clear();
                        });

                // create networks if two players are within activation distance
                Set<UUID> playersWithinRange = alivePlayers.stream()
                        .filter(p -> networks.stream().noneMatch(network -> network.contains(p)))
                        .filter(p -> !p.equals(player))
                        .filter(p -> p.getWorld().getName().equals(player.getWorld().getName()))
                        .filter(p -> horizontalDistance(p.getLocation(), player.getLocation()) <= getHorizontalStrength()
                                && verticalDistance(p.getLocation(), player.getLocation()) <= getVerticalStrength())
                        .filter(p -> {
                            Member m = getMember(p.getUniqueId());
                            return m != null && m.getVoiceState() != null
                                    && m.getVoiceState().getChannel() != null
                                    && m.getVoiceState().getChannel().getParent() != null
                                    && m.getVoiceState().getChannel().getParent().equals(category);
                        })
                        .map(Player::getUniqueId)
                        .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet));
                if (!playersWithinRange.isEmpty()) {
                    if (category.getChannels().size() == 50) {
//                        .debug(Debug.VOICE, "Can't create new voice network because category " + category.getName() + " is full of channels");
                        continue;
                    }

                    playersWithinRange.add(uuid);
                    networks.add(new Network(playersWithinRange));
                }
            }

            // handle moving players between channels
            Set<Member> members = new HashSet<>(lobbyChannel.getMembers());
            for (Network network : getNetworks()) {
                VoiceChannel voiceChannel = network.getChannel();
                if (voiceChannel == null) continue;
                members.addAll(voiceChannel.getMembers());
            }

            for (Member member : members) {
                UUID uuid = getUniqueId(member);
                VoiceChannel playerChannel = member.getVoiceState().getChannel();

                Network playerNetwork = uuid != null ? networks.stream()
                        .filter(n -> n.contains(uuid))
                        .findAny().orElse(null) : null;

                VoiceChannel shouldBeInChannel;
                if (playerNetwork != null) {
                    if (playerNetwork.getChannel() == null) {
                        // isn't yet created, we can wait until next tick
                        continue;
                    }

                    shouldBeInChannel = playerNetwork.getChannel();
                } else {
                    shouldBeInChannel = lobbyChannel;
                }

                Pair<String, CompletableFuture<Void>> awaitingMove = awaitingMoves.get(member.getId());
                // they're already where they're suppose to be
                if (awaitingMove != null && awaitingMove.getLeft().equals(shouldBeInChannel.getId())) continue;

                // if the cancel succeeded we can move them
                if (awaitingMove != null && !awaitingMove.getLeft().equals(shouldBeInChannel.getId())
                        && !awaitingMove.getRight().cancel(false)) continue;

                // schedule a move to the channel they're suppose to be in, if they aren't there yet
                if (!playerChannel.getId().equals(shouldBeInChannel.getId())) {
                    awaitingMoves.put(member.getId(), Pair.of(
                            shouldBeInChannel.getId(),
                            getGuild().moveVoiceMember(member, shouldBeInChannel)
                                    .submit().whenCompleteAsync((v, t) -> awaitingMoves.remove(member.getId()))
                    ));
                }
            }

            // delete empty networks
            for (Network network : new HashSet<>(networks)) {
                if (!network.isEmpty()) continue;

                VoiceChannel voiceChannel = network.getChannel();
                if (voiceChannel == null) continue;

                if (voiceChannel.getMembers().isEmpty()) {
                    voiceChannel.delete().reason("Lost communication").queue();
                    networks.remove(network);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        markDirty(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        markDirty(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        markDirty(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            networks.stream()
                    .filter(network -> network.contains(event.getPlayer().getUniqueId()))
                    .forEach(network -> network.remove(event.getPlayer().getUniqueId()));
        });
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        checkMutedUser(event.getChannelJoined(), event.getMember());
        if (!event.getChannelJoined().equals(getLobbyChannel())) return;


        UUID uuid = getUniqueId(event.getMember());
        if (uuid == null) return;
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.isOnline()) markDirty(player.getPlayer());
    }

    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event) {
        if (event.getChannelJoined().getParent() != null && !event.getChannelJoined().getParent().equals(getCategory()) &&
                event.getChannelLeft().getParent() != null && event.getChannelLeft().getParent().equals(getCategory())) {
            UUID uuid = getUniqueId(event.getMember());
            if (uuid == null) return;
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (player.isOnline()) {
                networks.stream()
                        .filter(network -> network.contains(player.getPlayer().getUniqueId()))
                        .forEach(network -> network.remove(player.getPlayer()));
            }
        }
        checkMutedUser(event.getChannelJoined(), event.getMember());
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        checkMutedUser(event.getChannelJoined(), event.getMember());
        if (event.getChannelLeft().getParent() == null || !event.getChannelLeft().getParent().equals(getCategory()))
            return;

        UUID uuid = getUniqueId(event.getMember());
        if (uuid == null) return;
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        if (player.isOnline()) {
            networks.stream()
                    .filter(network -> network.contains(player.getPlayer()))
                    .forEach(network -> network.remove(player.getPlayer()));
        }
    }

    @Override
    public void onVoiceChannelDelete(@NotNull VoiceChannelDeleteEvent event) {
        networks.removeIf(network -> network.getChannel() != null && event.getChannel().getId().equals(network.getChannel().getId()));
    }

    private void markDirty(Player player) {
        dirtyPlayers.add(player.getUniqueId());
    }

    private void checkMutedUser(VoiceChannel channel, Member member) {
        if (channel == null || member.getVoiceState() == null || getLobbyChannel() == null || getCategory() == null) {
            return;
        }
        boolean isLobby = channel.getId().equals(getLobbyChannel().getId());
        if (isLobby && !member.getVoiceState().isGuildMuted()) {
            PermissionOverride override = channel.getPermissionOverride(channel.getGuild().getPublicRole());
            if (override != null && override.getDenied().contains(Permission.VOICE_SPEAK)
                    && member.hasPermission(channel, Permission.VOICE_SPEAK, Permission.VOICE_MUTE_OTHERS)
                    && channel.getGuild().getSelfMember().hasPermission(channel, Permission.VOICE_MUTE_OTHERS)
                    && channel.getGuild().getSelfMember().hasPermission(getCategory(), Permission.VOICE_MOVE_OTHERS)) {
                member.mute(true).queue();
                mutedUsers.add(member.getId());
            }
        } else if (!isLobby) {
            if (mutedUsers.remove(member.getId())) {
                member.mute(false).queue();
            }
        }
    }

    public void shutdown() {
        for (Pair<String, CompletableFuture<Void>> value : awaitingMoves.values()) {
            value.getRight().cancel(true);
        }
        for (Network network : networks) {
            for (Member member : network.getChannel().getMembers()) {
                member.mute(false).queue();
                member.getGuild().moveVoiceMember(member, getLobbyChannel()).queue();
            }
            network.getChannel().delete().queue();
            network.clear();
        }
        this.networks.clear();
    }

    public static Main get() {
        return plugin.getVoiceModule();
    }

    public static Guild getGuild() {
        return getLobbyChannel().getGuild();
    }

    public static Category getCategory() {
        if (jda == null) return null;
        String id = plugin.playerData.getString("categoryID");
        if (StringUtils.isBlank(id)) return null;
        return jda.getCategoryById(id);
    }

    public static VoiceChannel getLobbyChannel() {
        if (jda == null) return null;
        String id = plugin.playerData.getString("mainVoiceChannelID");
        if (StringUtils.isBlank(id)) return null;
        return jda.getVoiceChannelById(id);
    }

    public static Member getMember(UUID player) {
        String discordId = plugin.playerData.getString("Data." + player);
        return discordId != null ? getGuild().getMemberById(discordId) : null;
//        String discordId = plugin.getAccountLinkManager().getDiscordId(player);
    }

    public static UUID getUniqueId(Member member) {
//        return plugin.getAccountLinkManager().getUuid(member.getId());
//        return guild.getMemberById(plugin.playerData.getString("Data."+u));
        String id = plugin.playerData.getString("Data." + member.getId());
//        return UUID.fromString(plugin.playerData.getString("Data."+member.getId()));
        return id != null ? UUID.fromString(plugin.playerData.getString("Data." + member.getId())) : null;
    }

    public static double verticalDistance(Location location1, Location location2) {
        return Math.sqrt(NumberConversions.square(location1.getY() - location2.getY()));
    }

    public static double horizontalDistance(Location location1, Location location2) {
        return Math.sqrt(NumberConversions.square(location1.getX() - location2.getX()) + NumberConversions.square(location1.getZ() - location2.getZ()));
    }

    public static double getVerticalStrength() {
        return plugin.playerData.getInt("distance.custom.verticalStrength");
    }

    public static double getHorizontalStrength() {
        return plugin.playerData.getInt("distance.custom.horizontalStrength");
    }

    public static double getFalloff() {
        return plugin.playerData.getInt("distance.custom.falloff");
    }

    public static boolean isVoiceActivationAllowed() {
        return true;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        EmbedBuilder embed = new EmbedBuilder().setTitle("Linking Process");
        String saddsa = plugin.playerData.getString("Data." + event.getUser().getId());
        if (event.getName().equals("link")) {
            if (saddsa != null) {
                event.replyEmbeds(embed.addField("Error", "Your Discord account is already linked to Minecraft.\nType `/unlink` to unlink it.", false)
                                .setColor(Color.RED).build())
                        .setEphemeral(true).queue();
                return;
            }
            Player target = Bukkit.getPlayer(event.getOption("minecraft_username").getAsString());
            if (target == null) {
                event.replyEmbeds(embed.addField("Error", "The specified Minecraft account is not online.", false)
                                .setColor(Color.RED).build())
                        .setEphemeral(true).queue();
                return;
            }
            String das = plugin.playerData.getString("Data." + target.getUniqueId());
            if (das != null) {
                event.replyEmbeds(embed.addField("Error", "The specified Minecraft account is already linked to Discord.", false)
                                .setColor(Color.RED).build())
                        .setEphemeral(true).queue();
                return;
            }
            if (uuidIdMap.containsValue(event.getUser().getId())) {
                uuidCodeMap.remove(target.getUniqueId());
                uuidIdMap.remove(target.getUniqueId());
            }
            String randomCode = new Random().nextInt(800000) + 200000 + "SK";
            uuidCodeMap.put(target.getUniqueId(), randomCode);
            uuidIdMap.put(target.getUniqueId(), event.getUser().getId());
            event.replyEmbeds(embed.addField("Verification Code", "Type `/link " + randomCode + "` in game to complete the process.", false)
                            .setColor(Color.GREEN).build())
                    .setEphemeral(true).queue();
        } else if (event.getName().equals("unlink")) {
            if (saddsa == null) {
                event.replyEmbeds(embed.addField("Error", "Your Discord account is not linked to Minecraft.\nType `/link` to link it.", false)
                                .setColor(Color.RED).build())
                        .setEphemeral(true).queue();
            } else {
                plugin.playerData.set("Data." + event.getUser().getId(), null);
                plugin.playerData.set("Data." + saddsa, null);
                try {
                    plugin.playerData.save(plugin.data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                event.replyEmbeds(embed.addField("Account Unlinked", "Your Discord account has been unlinked from Minecraft.", false)
                                .setColor(Color.GREEN).build())
                        .setEphemeral(true).queue();
            }
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) { //  /verify randomcodeSK
        if (!(sender instanceof Player)) {
            sender.sendMessage("§dSkoice §8• §7This command is §conly executable §7by players.");
            return true;
        }
        Player player = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("unlink")) {
            String userID = plugin.playerData.getString("Data." + player.getUniqueId());
            if (userID == null) {
                player.sendMessage("§dSkoice §8• §7You have §cnot linked your Minecraft account §7to Discord. Type \"§e/link§7\" on our Discord server to link it.");
                return true;
            }
            plugin.playerData.set("Data." + player.getUniqueId(), null);
            plugin.playerData.set("Data." + userID, null);
            try {
                plugin.playerData.save(plugin.data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                getGuild().retrieveMemberById(userID).complete().getUser().openPrivateChannel().complete()
                        .sendMessageEmbeds(new EmbedBuilder().setTitle("Linking Process")
                                .addField("Account Unlinked", "Your Discord account has been unlinked from Minecraft.", false)
                                .setColor(Color.GREEN).build()).queue();
            } catch (ErrorResponseException e) {
                plugin.getLogger().warning("A player attempted to unlink but his Discord account was not found.");
            }
            player.sendMessage("§dSkoice §8• §7You have §aunlinked your Minecraft account §7from Discord.");
            return true;
        } else if (cmd.getName().equalsIgnoreCase("link")) {
            String dasd = plugin.playerData.getString("Data." + player.getUniqueId());
            if (dasd != null) {
                player.sendMessage("§dSkoice §8• §7You have §calready linked your Minecraft account §7to Discord. Type \"§e/unlink§7\" to unlink it.");
                return true;
            }
            if (!uuidCodeMap.containsKey(player.getUniqueId())) {
                player.sendMessage("§dSkoice §8• §7There is §cno pending linking process §7for your Minecraft account. Type \"§e/link§7\" on our Discord server to link it.");
                return true;
            }
            if (args.length < 1) {
                player.sendMessage("§dSkoice §8• §7You have §cnot provided the code §7that was sent to you on Discord.");
                return true;
            }
            String actualCode = uuidCodeMap.get(player.getUniqueId());
            if (!actualCode.equals(args[0])) {
                player.sendMessage("§dSkoice §8• §7The provided code is §cinvalid§7.");
                return true;
            }
            String discordid = uuidIdMap.get(player.getUniqueId());
            Member target = getGuild().getMemberById(discordid);
            if (target == null) {
                uuidCodeMap.remove(player.getUniqueId());
                uuidIdMap.remove(player.getUniqueId());
                player.sendMessage("§cError! It seems that you left our Discord server! If not, try to join a voice channel and try again.");
                return true;
            }
            plugin.playerData.set("Data." + player.getUniqueId(), discordid);
            plugin.playerData.set("Data." + discordid, player.getUniqueId().toString());
            try {
                plugin.playerData.save(plugin.data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            uuidCodeMap.remove(player.getUniqueId());
            uuidIdMap.remove(player.getUniqueId());
//        verifiedmembers.add(player.getUniqueId());

            target.getUser().openPrivateChannel().complete().sendMessageEmbeds(new EmbedBuilder().setTitle("Linking Process")
                            .addField("Account Linked", "Your Discord account has been linked to Minecraft.", false)
                            .setColor(Color.GREEN).build()).queue();
            player.sendMessage("§dSkoice §8• §7You have §alinked your Minecraft account §7to Discord.");
        }
        return true;
    }

    @EventHandler //This is to remove the player from all lists and maps when he leaves the server.
    public void onQuit(PlayerQuitEvent e) {
        // remove VC in player left
        uuidCodeMap.remove(e.getPlayer().getUniqueId());
        uuidIdMap.remove(e.getPlayer().getUniqueId());
    }

    /**
     * Method return type-safe version of Bukkit::getOnlinePlayers
     *
     * @return {@code ArrayList} containing online players
     */
    public static List<Player> getOnlinePlayers() {
        List<Player> onlinePlayers = new ArrayList<>();

        try {
            Method onlinePlayerMethod = Server.class.getMethod("getOnlinePlayers");
            if (onlinePlayerMethod.getReturnType().equals(Collection.class)) {
                for (Object o : ((Collection<?>) onlinePlayerMethod.invoke(Bukkit.getServer()))) {
                    onlinePlayers.add((Player) o);
                }
            } else {
                Collections.addAll(onlinePlayers, ((Player[]) onlinePlayerMethod.invoke(Bukkit.getServer())));
            }
        } catch (Exception e) {
//            error(e);
        }

        return onlinePlayers;
    }
}
