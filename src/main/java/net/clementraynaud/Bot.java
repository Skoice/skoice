// Copyright 2020, 2021 Clément "carlodrift" Raynaud, rowisabeast

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
// along with Foobar.  If not, see <https://www.gnu.org/licenses/>.


package net.clementraynaud;

import net.clementraynaud.api.PlaceHolderAPI;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.api.utils.concurrent.Task;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.*;

public class Bot extends ListenerAdapter implements CommandExecutor, Listener {
    public Skoice plugin;
    public HashMap<UUID,String>uuidCodeMap;
    public HashMap<UUID,String>uuidIdMap;
    public List<UUID> verifiedmembers;
    public List<String> allBotMadeChannels;
    public List<Member> allGuildMembers;
    public int radius;
    VoiceChannel mainVoiceChannel;
    public Guild guild;
    private Task<List<Member>> acmssa;
    public Category Gcate;
    public Boolean forcePlayerOnline;
    JDA jda;

    public Bot(Skoice skoiceTwo){
        this.plugin = skoiceTwo;
        startBot();
        uuidCodeMap = new HashMap<>();
        uuidIdMap = new HashMap<>();
        verifiedmembers = new ArrayList<>();
        allBotMadeChannels = new ArrayList<>();
        jda.addEventListener(this);
        radius = plugin.playerData.getInt("radius");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getCommand("verify").setExecutor(this);
        Bukkit.getScheduler().runTaskLater(plugin,()->guild = jda.getGuildById(plugin.playerData.getString("guildID")),1L);
        Bukkit.getScheduler().runTaskLater(plugin,()->acmssa = guild.loadMembers(),1L);
        Bukkit.getScheduler().runTaskLater(plugin,()->Gcate = guild.getCategoryById(plugin.playerData.getString("categoryID")),1L);
        Bukkit.getScheduler().runTaskLater(plugin,()->mainVoiceChannel = guild.getVoiceChannelById(plugin.playerData.getString("mainVoiceChannelID")), 1L);
        Bukkit.getScheduler().runTaskLater(plugin,()->forcePlayerOnline = plugin.playerData.getBoolean("forcePlayersOnline"), 1L);
        Bukkit.getScheduler().runTaskLater(plugin,()-> {
            for(VoiceChannel vc : guild.getVoiceChannels()){
                for(Member mem : vc.getMembers()){
                    this.whatVoiceChannelIsMemberIn.put(mem, vc);
                    plugin.getLogger().info("Member is in '"+mem.getNickname()+"' in Voice Channel '"+vc.getName()+"'");
                }
            }
        }, 10L);
        Bukkit.getScheduler().runTaskTimer(plugin, this::checkVoiceChannels, 2L, 40L);

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            new PlaceHolderAPI(this).register();
        }

    }
    //TODO
    // - Add additional settings
    //    - Show Member status in title/subtitle/message
    //    - Force players to be on discord
    //       - Apply slowness and blindness to that player
    //    - API so other plugins can use this
    //    - Web/Rest API so other can get this plugins information
    //    - Auto updating,
    // - Add unlink command
    //    - Allows players to unlink Accounts
    // - Add softDep for Dynmap
    //    - Custom Marksers
    // - Add setting for `onPlayerMoveEvent` or `EveryXseconds`
    // - Add function to see if there is empty 'group' VC, if so, delete it.


    private void startBot(){
        try {
            jda = JDABuilder.createDefault(plugin.playerData.getString("token"))
                    .setActivity(Activity.listening("*link"))
                    .enableIntents(GatewayIntent.GUILD_VOICE_STATES,
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_PRESENCES,
                            GatewayIntent.GUILD_MESSAGES)
                    .enableCache(CacheFlag.VOICE_STATE,
                            CacheFlag.CLIENT_STATUS,
                            CacheFlag.MEMBER_OVERRIDES,
                            CacheFlag.ACTIVITY,
                            CacheFlag.ONLINE_STATUS)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setAutoReconnect(true)
                    .build()
                    .awaitReady();
            plugin.getLogger().info("JDA is running!");
        }catch (LoginException | InterruptedException e){
            plugin.getLogger().severe("JDA ERROR: "+ Arrays.toString(e.getStackTrace()));
        }
    }
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event){
        if(event.getAuthor().isBot()||event.isWebhookMessage())return;
        String[] args = event.getMessage().getContentRaw().split(" ");
        if(args[0].equalsIgnoreCase("*link")){
            String saddsa = plugin.playerData.getString("Data."+event.getAuthor().getId());
            if(saddsa!=null){
                event.getChannel().sendMessage("This discord account is already linked to a player!").queue();
                return;
            }
            if(uuidIdMap.containsValue(event.getAuthor().getId())){
                event.getChannel().sendMessage(":x: **|** Error! "+event.getAuthor().getAsMention()+", you already have a code generated!").queue();
                return;
            }
            if(args.length!=2){
                event.getChannel().sendMessage(":x: **|** Error! You need to specify a player!").queue();
                return;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if(target==null){
                event.getChannel().sendMessage(":x: **|** Error! The player is not online!").queue();
                return;
            }
            String das = plugin.playerData.getString("Data."+target.getUniqueId());
            if(das!=null){
                event.getChannel().sendMessage("This player is already linked to another discord account").queue();
                return;
            }
            String randomcode = new Random().nextInt(800000)+200000+"PL"; //6581446AA
            uuidCodeMap.put(target.getUniqueId(),randomcode);
            uuidIdMap.put(target.getUniqueId(),event.getAuthor().getId());

            event.getAuthor().openPrivateChannel().complete().sendMessage("Hey! Your verification has been generated!\n" +
                    "Use this command in game: ``/verify "+randomcode+"``").queue();
        }
    }
    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event){
        whatVoiceChannelIsMemberIn.put(event.getMember(), event.getChannelJoined());
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event){
        whatVoiceChannelIsMemberIn.remove(event.getMember());
    }
    @Override
    public void onGuildVoiceMove(GuildVoiceMoveEvent event){
        whatVoiceChannelIsMemberIn.put(event.getMember(), event.getChannelJoined());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        ////plugin.getLogger()info("Player joined");
        String asd = plugin.playerData.getString("Data."+e.getPlayer().getUniqueId());
        //plugin.getLogger()info("Is player in file? | "+asd);
        if(asd!=null){
            //plugin.getLogger()info("Player was found in Verified file");

            verifiedmembers.add(e.getPlayer().getUniqueId());


        }else{
            //plugin.getLogger()info("Couldn't get player from file");

            // Player not verified, if forcePlayerOnline is true send message saying they need to be verified and in VC to play
            if(forcePlayerOnline){
                e.getPlayer().sendMessage(ChatColor.DARK_RED+"\uD83D\uDEAB"+ChatColor.RED+" You Need to Link Your Discord Account to Play! "+ChatColor.DARK_RED+"\uD83D\uDEAB");
            }
        }
        Player pl = e.getPlayer();
        locationsx.put(pl, pl.getLocation().getBlockX());
        locationsy.put(pl, pl.getLocation().getBlockY());
        locationsz.put(pl, pl.getLocation().getBlockZ());

    }
    @EventHandler //This is to remove the player from all lists and maps when he leaves the server.
    public void onQuit(PlayerQuitEvent e){
        // remove VC in player left

        verifiedmembers.remove(e.getPlayer().getUniqueId());
        uuidCodeMap.remove(e.getPlayer().getUniqueId());
        uuidIdMap.remove(e.getPlayer().getUniqueId());

        // Move member back to mainVoiceChannel,
        if(doesMemberAlreadyHaveVoiceChannel(e.getPlayer().getUniqueId())){
            Member nsd = getMemberFromPlayerUUID(e.getPlayer().getUniqueId());
            VoiceChannel playVC = membersVoiceChannel.get(nsd);
            if(nsd.getVoiceState().inVoiceChannel() && nsd.getVoiceState().getChannel().equals(playVC)){
                try {
                    guild.moveVoiceMember(nsd, mainVoiceChannel).complete(true);
                } catch (RateLimitedException ex) {
                    ex.printStackTrace();
                }
                createdVoiceChannels.remove(playVC);
                voiceChannelGroup.remove(playVC);
                memberGroup.remove(nsd);
                membersVoiceChannel.remove(nsd);
                playVC.delete().queue();
            }
        }

    }



    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) { //  /verify randomcodeAA
        if(!(sender instanceof Player)){
            sender.sendMessage("§cOnly players can execute this command!");
            return true;
        }
        Player player = (Player) sender;
        //if(cmd.getName().equalsIgnoreCase("ping")){
        //    player.sendMessage("Pong!");
        //}
        String dasd = plugin.playerData.getString("Data."+player.getUniqueId());
        if(dasd!=null){
            player.sendMessage("§cSorry! You are already verified!");
            return true;
        }
        if(!uuidCodeMap.containsKey(player.getUniqueId())){
            player.sendMessage("§cNot pending verification process!");
            return true;
        }
        if(args.length!=1){
            player.sendMessage("§cUsage: /verify [code]");
            return true;
        }
        String actualcode = uuidCodeMap.get(player.getUniqueId());
        if(!actualcode.equals(args[0])){
            player.sendMessage("§cCode is not valid! Check again!");
            return true;
        }
        String discordid = uuidIdMap.get(player.getUniqueId());
        Member target = guild.getMemberById(discordid);
        if(target==null){
            uuidCodeMap.remove(player.getUniqueId());
            uuidIdMap.remove(player.getUniqueId());
            player.sendMessage("§cError! It seems that you left our Discord server!");
            return true;
        }
        plugin.playerData.set("Data."+ player.getUniqueId(),discordid);
        plugin.playerData.set("Data."+discordid,player.getUniqueId().toString());
        try {
            plugin.playerData.save(plugin.data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        uuidCodeMap.remove(player.getUniqueId());
        uuidIdMap.remove(player.getUniqueId());
        verifiedmembers.add(player.getUniqueId());


        target.getUser().openPrivateChannel().complete().sendMessage(":white_check_mark: **|** Verification successfully, you have linked your account with Mc account: "+player.getName()).queue();
        player.sendMessage("§aYou have been verified correctly! You linked your account with member: "+target.getUser().getName()+"#"+target.getUser().getDiscriminator());
        return true;
    }



    public HashMap<Player, Integer> locationsx = new HashMap<>();
    public HashMap<Player, Integer> locationsy = new HashMap<>();
    public HashMap<Player, Integer> locationsz = new HashMap<>();
    public List<Player> stopedPlayer = new ArrayList<>();
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e){
        if(forcePlayerOnline && (plugin.playerData.getString("Data." + e.getPlayer().getUniqueId()) == null || isPlayerInVC(e.getPlayer().getUniqueId()))){
            e.setTo(e.getFrom()); // Make sure the player doesn't move
            stopedPlayer.add(e.getPlayer());
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1));
            return;
        }
        if(stopedPlayer.contains(e.getPlayer())) {
            stopedPlayer.remove(e.getPlayer());
            e.getPlayer().removePotionEffect(PotionEffectType.BLINDNESS);
        }
        Player player = e.getPlayer();
        int xCords = e.getTo().getBlockX();
        int yCords = e.getTo().getBlockY();
        int zCords = e.getTo().getBlockY();
        if(locationsx.get(player)==null){
            locationsx.put(player, 0);
            locationsy.put(player, 0);
            locationsz.put(player, 0);
        }
        if(!locationsx.get(player).equals(xCords)){
             locationsx.put(player, xCords);
            Skoice(e);
        }else if(!locationsy.get(player).equals(yCords)){
            locationsy.put(player, yCords);
            Skoice(e);
        }else if(!locationsz.get(player).equals(zCords)){
            locationsz.put(player, zCords);
            Skoice(e);
        }

        //if(ifchange){
        //    player.sendMessage("X: " + locationsx.get(player) + " | Y: " + locationsy.get(player) + " | Z: " + locationsz.get(player));
        //    //plugin.getLogger()info("Time stamp: "+new Timestamp(System.currentTimeMillis()));
        //    Skoice(e);
        //}

    }
    //                            Real Skoice
    /////////////////////////////////////////////////////////////////////
    //

    // Variables
    public ArrayList<VoiceChannel> createdVoiceChannels = new ArrayList<>();
    public ArrayList<UUID> coolDownCheck = new ArrayList<>();
    public HashMap<Member, String> memberGroup = new HashMap<>();
    public HashMap<Member, VoiceChannel> whatVoiceChannelIsMemberIn = new HashMap<>();
    public HashMap<VoiceChannel, String> voiceChannelGroup = new HashMap<>();
    public HashMap<Member, VoiceChannel> membersVoiceChannel = new HashMap<>();

    // groups, channels


    // Functions
    public Boolean isPlayerInVC(UUID u){
        Member nm = getMemberFromPlayerUUID(u);
        VoiceChannel playerVC = nm.getVoiceState().getChannel();
        if(playerVC == null){
            return false;
        }else{
            return true;
        }
        //return guild.getMemberById(plugin.playerData.getString("Data."+u)).getVoiceState().inVoiceChannel(); // Make a better check to see if member is in a VC
    }
    public Member getMemberFromPlayerUUID(UUID u){
        return guild.getMemberById(plugin.playerData.getString("Data."+u));
    }
    //public VoiceChannel whatVoiceChannelIsPlayerIn(UUID u){
    //    Member tm = getMemberFromPlayerUUID(u);
    //    return tm.getVoiceState().getChannel();
    //}
    public Boolean isPlayerInRadius(UUID u1, UUID u2){
        Player p1 = Bukkit.getPlayer(u1);
        Player p2 = Bukkit.getPlayer(u2);
        return p1.getLocation().distance(p2.getLocation()) <= radius;
    }
    public Boolean doesMemberAlreadyHaveVoiceChannel(UUID u1){
        Member nm = getMemberFromPlayerUUID(u1);
        return membersVoiceChannel.containsKey(nm);
    }

    public void checkVoiceChannels(){
        if(createdVoiceChannels.size()==0) return;
        for(VoiceChannel createdVoiceChannel : createdVoiceChannels){
            if(createdVoiceChannel==null) {
                createdVoiceChannels.remove(createdVoiceChannel);
                continue;
            }
            // if null, remove that vc from createdvoicechannels
            if(voiceChannelGroup.get(createdVoiceChannel).equals("group")){
                // VC is GROUP

                // if Voice Channel is empty
                if(createdVoiceChannel.getMembers().size()==0){
                    // No one in VC
                    voiceChannelGroup.remove(createdVoiceChannel);
                    createdVoiceChannels.remove(createdVoiceChannel);
                    createdVoiceChannel.delete().queue();
                }else if(createdVoiceChannel.getMembers().size()==1){
                    // 1 person in group VC
                    // Moving them to personal

                    Member MeminVC = createdVoiceChannel.getMembers().get(0);
                    VoiceChannel memVC = membersVoiceChannel.get(MeminVC);
                    memberGroup.put(MeminVC, "single");
                    whatVoiceChannelIsMemberIn.put(MeminVC, memVC);
                    guild.moveVoiceMember(MeminVC, memVC).queue();
                }

            }
        }
    }

    //Main
    // loop every online player, using verifiedmembers

    // WORKS!!!
//done//   - The group create spams, fix this // AFTER - i think this should be fixed with the player movement one
//done//   - Check if group has <= 1, if so move member to personal VC, and remove Group // Should work, testing // works-ish
//done//   - Only run Skoice() when the Player has moved to a different block

    private void Skoice(PlayerMoveEvent onMoveEvent){

        // Check if verified in file
        String asd = plugin.playerData.getString("Data."+onMoveEvent.getPlayer().getUniqueId());
        if(asd==null){
            // Player isn't verified
            return;
        }
        // Check if Member is in vc
        if(!isPlayerInVC(onMoveEvent.getPlayer().getUniqueId())) return;

        //check if main
        if(whatVoiceChannelIsMemberIn.get(getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId())).equals(mainVoiceChannel)) {
            // Player is in MainVC
            //onMoveEvent.getPlayer().sendMessage("Member is in mainVoiceChannel");
            // First check if member already has Personal VC
            if (doesMemberAlreadyHaveVoiceChannel(onMoveEvent.getPlayer().getUniqueId())) {
                Member gm = getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId());
                VoiceChannel alvc = membersVoiceChannel.get(gm);
                guild.moveVoiceMember(gm, alvc).queue();
                memberGroup.put(gm, "single");
                //onMoveEvent.getPlayer().sendMessage("Player already has voice channel, moving them back");
            }

            VoiceChannel newCreatedVoiceChannel = null;
            try {
                newCreatedVoiceChannel = guild.createVoiceChannel(onMoveEvent.getPlayer().getName())
                        .setParent(Gcate)
                        .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                        .complete(true);
            } catch (RateLimitedException e) {
                e.printStackTrace();
            }
            createdVoiceChannels.add(newCreatedVoiceChannel);
            // Created Voice channel for user
            membersVoiceChannel.put(getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId()), newCreatedVoiceChannel);
            voiceChannelGroup.put(newCreatedVoiceChannel, "single");
            // Move member to New Voice Channel
            whatVoiceChannelIsMemberIn.put(getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId()), newCreatedVoiceChannel);
            guild.moveVoiceMember(getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId()), newCreatedVoiceChannel).queue();
            memberGroup.put(getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId()), "single");
            //onMoveEvent.getPlayer().sendMessage("Created Single Voice Channel for: " + onMoveEvent.getPlayer().getName());
        }


        //onMoveEvent.getPlayer().sendMessage("Skoice function");
        //  IF the Player is in radius of another player that is already in a group, move player to that group.\
        for(UUID playerUUID : verifiedmembers){
            if(whatVoiceChannelIsMemberIn.get(getMemberFromPlayerUUID(playerUUID)).equals(mainVoiceChannel)){
                //onMoveEvent.getPlayer().sendMessage("Member is in mainVoiceChannel");
                // First check if member already has Personal VC
                if (doesMemberAlreadyHaveVoiceChannel(onMoveEvent.getPlayer().getUniqueId())) {
                    Member gm = getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId());
                    VoiceChannel alvc = membersVoiceChannel.get(gm);
                    guild.moveVoiceMember(gm, alvc).queue();
                    memberGroup.put(gm, "single");
                    //onMoveEvent.getPlayer().sendMessage("Player already has voice channel, moving them back");
                    continue;
                }

                VoiceChannel newCreatedVoiceChannel = null;
                try {
                    newCreatedVoiceChannel = guild.createVoiceChannel(onMoveEvent.getPlayer().getName())
                            .setParent(Gcate)
                            .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                            .complete(true);
                } catch (RateLimitedException e) {
                    e.printStackTrace();
                }
                createdVoiceChannels.add(newCreatedVoiceChannel);
                // Created Voice channel for user
                membersVoiceChannel.put(getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId()), newCreatedVoiceChannel);
                voiceChannelGroup.put(newCreatedVoiceChannel, "single");
                // Move member to New Voice Channel
                whatVoiceChannelIsMemberIn.put(getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId()), newCreatedVoiceChannel);
                guild.moveVoiceMember(getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId()), newCreatedVoiceChannel).queue();
                memberGroup.put(getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId()), "single");
                //onMoveEvent.getPlayer().sendMessage("Created Single Voice Channel for: " + onMoveEvent.getPlayer().getName());
                continue;
            }

            //onMoveEvent.getPlayer().sendMessage("Loop(playerUUID): "+playerUUID);

            if(!isPlayerInVC(playerUUID)) {
                //onMoveEvent.getPlayer().sendMessage("isn't in VC");
                continue; // Player's guild member isn't in VC
            }
            //onMoveEvent.getPlayer().sendMessage("Player is in VC");
            // if player is in main

            // Every online player that is verified
            //
            //onMoveEvent.getPlayer().sendMessage("2 checks");
            if(playerUUID==onMoveEvent.getPlayer().getUniqueId()) {
                //onMoveEvent.getPlayer().sendMessage("Same player, NEXT");
                continue; // Same player as the move event player
            }
            if(!isPlayerInVC(onMoveEvent.getPlayer().getUniqueId())) {
                //onMoveEvent.getPlayer().sendMessage("Moved player isn't in VC");
                continue; // moved player isn't in VC
            }
            //onMoveEvent.getPlayer().sendMessage("Both Passed!");
            // Player is online, and in VC


            // Check distance
            int isplinrad = (int) onMoveEvent.getPlayer().getLocation().distance(Bukkit.getPlayer(playerUUID).getLocation());
            //onMoveEvent.getPlayer().sendMessage("The distance from player '"+Bukkit.getPlayer(playerUUID).getName()+"' is "+isplinrad+" and radius is '"+radius+"'");
            //onMoveEvent.getPlayer().sendMessage("is player in the radius of another player? ");
            if(isplinrad<=radius){
                //onMoveEvent.getPlayer().sendMessage("they sure are");
                // Players are in radius
                // check if they are already in group

                if(getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId()).getVoiceState().getChannel().equals(getMemberFromPlayerUUID(playerUUID).getVoiceState().getChannel())){
                    // Players are in same VC
                    //onMoveEvent.getPlayer().sendMessage("Player are already in the same VC");
                    continue;
                }

                // If vc type is group, just move the moved user to that group and hide the personal VC
                // If vc type is single, make a new Group VC, move both users to it, and hide the personal vc
                if(voiceChannelGroup.get(getMemberFromPlayerUUID(playerUUID).getVoiceState().getChannel()).equals("group")){
                    //
                    //onMoveEvent.getPlayer().sendMessage("Found target player is in Group VC, adding them!");
                    memberGroup.put(getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId()), "group");
                    whatVoiceChannelIsMemberIn.put(getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId()), getMemberFromPlayerUUID(playerUUID).getVoiceState().getChannel());
                    guild.moveVoiceMember(getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId()), getMemberFromPlayerUUID(playerUUID).getVoiceState().getChannel()).queue();



                }else if(voiceChannelGroup.get(getMemberFromPlayerUUID(playerUUID).getVoiceState().getChannel()).equals("single")){
                    //
                    //onMoveEvent.getPlayer().sendMessage("Found target and moved user are both in single VC, bring them together");
                    try {
                        VoiceChannel newGroupVoiceChannel = guild.createVoiceChannel("Group")
                                .setParent(Gcate)
                                .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                                .complete(true);
                        createdVoiceChannels.add(newGroupVoiceChannel);
                        voiceChannelGroup.put(newGroupVoiceChannel, "group");
                        // Move both users into group channel
                        memberGroup.put(getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId()), "group");
                        memberGroup.put(getMemberFromPlayerUUID(playerUUID), "group");
                        whatVoiceChannelIsMemberIn.put(getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId()), newGroupVoiceChannel);
                        whatVoiceChannelIsMemberIn.put(getMemberFromPlayerUUID(playerUUID), newGroupVoiceChannel);
                        guild.moveVoiceMember(getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId()), newGroupVoiceChannel).queue();
                        guild.moveVoiceMember(getMemberFromPlayerUUID(playerUUID), newGroupVoiceChannel).queue();
                    } catch (RateLimitedException e) {
                        e.printStackTrace();
                    }

                    //


                }

                // if the target player is already in a group, move the moved user to that group
                // else
                // Make a new Voice channel
            }else{
                // Player arn't in radius
                //onMoveEvent.getPlayer().sendMessage("they arn't :(");
                VoiceChannel playersPersonalVoiceChannel = membersVoiceChannel.get(getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId()));
                Member playerMember = getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId());
                if(playerMember.getVoiceState().getChannel().equals(playersPersonalVoiceChannel)){
                    // Player is already in Personal Voice Channel, don't do anything
                    //onMoveEvent.getPlayer().sendMessage("Player are already in personal VC's, they wouldn't be touched");
                    continue;
                }
                // if member is in mainVC
                Boolean nm = false;
                if(getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId()).getVoiceState().getChannel().equals(mainVoiceChannel)){
                    try {
                        VoiceChannel newCreatedVoiceChannel = guild.createVoiceChannel(onMoveEvent.getPlayer().getName())
                                .setParent(Gcate)
                                .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL))
                                .complete(true);
                        createdVoiceChannels.add(newCreatedVoiceChannel);
                        // Created Voice channel for user
                        membersVoiceChannel.put(getMemberFromPlayerUUID(playerUUID), newCreatedVoiceChannel);
                        voiceChannelGroup.put(newCreatedVoiceChannel, "single");
                        // Move member to New Voice Channel
                        whatVoiceChannelIsMemberIn.put(getMemberFromPlayerUUID(playerUUID), newCreatedVoiceChannel);
                        guild.moveVoiceMember(getMemberFromPlayerUUID(playerUUID), newCreatedVoiceChannel).queue();
                        memberGroup.put(getMemberFromPlayerUUID(playerUUID), "single");
                        //onMoveEvent.getPlayer().sendMessage("Created Single Voice Channel for: "+onMoveEvent.getPlayer().getName());
                        nm = true;
                    } catch (RateLimitedException e) {
                        e.printStackTrace();
                    }
                }
                if(nm) continue;
                // not in main
                VoiceChannel diffVC = whatVoiceChannelIsMemberIn.get(getMemberFromPlayerUUID(onMoveEvent.getPlayer().getUniqueId()));
                String difID = diffVC.getId();
                Member targetMember = getMemberFromPlayerUUID(playerUUID);
                VoiceChannel targetPersonalVC = membersVoiceChannel.get(targetMember);
                if(voiceChannelGroup.get(diffVC).equals("group")){
                    // In group, move member back to personal
                    memberGroup.put(playerMember, "single");
                    whatVoiceChannelIsMemberIn.put(playerMember, playersPersonalVoiceChannel);
                    guild.moveVoiceMember(playerMember, playersPersonalVoiceChannel).queue();
                    //onMoveEvent.getPlayer().sendMessage("put them back to single");

                    // Check if Voice Channel has less or equal to 1 member
                    // player message here | diffVC size
                    // get new revise of voice channel
                    VoiceChannel saf = guild.getVoiceChannelById(difID);

                    //onMoveEvent.getPlayer().sendMessage("Group left size: "+saf.getMembers().size());
                    //if(saf.getMembers().size()<=1){
                    //    // Group has less or 1 member in it, move that member to single
                    //    for(Member meminVC : saf.getMembers()){
                    //        try {
                    //            VoiceChannel mempersonvc = membersVoiceChannel.get(meminVC);
                    //            memberGroup.put(meminVC, "single");
                    //            whatVoiceChannelIsMemberIn.put(meminVC, mempersonvc);
                    //            guild.moveVoiceMember(meminVC, mempersonvc).complete(true);
                                // All members should be moved now

                                // remove channel from hashmap
                    //            createdVoiceChannels.remove(saf);
                    ///           voiceChannelGroup.remove(saf);
                    //         saf.delete().queue();
                    //      } catch (RateLimitedException e) {
                    //          e.printStackTrace();
                    //      }
                    //  }
                    //}
                }
            }
            // If player is already in VC with other player
            //if(whatVoiceChannelIsPlayerIn(onMoveEvent.getPlayer().getUniqueId()).equals(whatVoiceChannelIsPlayerIn(playerUUID))) return;


        }
        //onMoveEvent.getPlayer().sendMessage("Skoice function done!!");
    }

    //
    /////////////////////////////////////////////////////////////////////
}
