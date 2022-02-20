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

package net.clementraynaud.skoice.config;

import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.*;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.bot.Bot.*;

public class Config {

    public static final String TOKEN_FIELD = "token";
    public static final String LANG_FIELD = "lang";
    public static final String LOBBY_ID_FIELD = "lobby-id";
    public static final String HORIZONTAL_RADIUS_FIELD = "radius.horizontal"; // One reference to "radius." remaining
    public static final String VERTICAL_RADIUS_FIELD = "radius.vertical";
    public static final String ACTION_BAR_ALERT_FIELD = "action-bar-alert";
    public static final String CHANNEL_VISIBILITY_FIELD = "channel-visibility";
    public static final String TEMP_GUILD_ID_FIELD = "temp.guild-id";
    public static final String TEMP_TEXT_CHANNEL_ID_FIELD = "temp.text-channel-id";
    public static final String TEMP_MESSAGE_ID_FIELD = "temp.message-id";

    private Config() {
    }

    public static String getKeyFromValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static UUID getMinecraftID(Member member) {
        String minecraftID = getPlugin().getConfig().getString("link." + member.getId());
        return minecraftID != null ? UUID.fromString(minecraftID) : null;
    }

    public static String getToken() {
        return getPlugin().getConfig().getString(TOKEN_FIELD);
    }

    public static void setToken(String token) {
        byte[] tokenBytes = token.getBytes();
        for (int i = 0; i < tokenBytes.length; i++) {
            tokenBytes[i]++;
        }
        getPlugin().getConfig().set(TOKEN_FIELD, Base64.getEncoder().encodeToString(tokenBytes));
        getPlugin().saveConfig();
    }

    public static String getLang() {
        return getPlugin().getConfig().getString(LANG_FIELD);
    }

    public static VoiceChannel getLobby() {
        if (getJda() == null) return null;
        String lobbyID = getPlugin().getConfig().getString(LOBBY_ID_FIELD);
        if (lobbyID == null) return null;
        VoiceChannel lobby = getJda().getVoiceChannelById(lobbyID);
        if (lobby == null) return null;
        if (lobby.getParent() == null) return null;
        return lobby;
    }

    public static void setLobbyID(String lobbyID) {
        getPlugin().getConfig().set(LOBBY_ID_FIELD, lobbyID);
        getPlugin().saveConfig();
    }

    public static Category getCategory() {
        if (getJda() == null) return null;
        VoiceChannel lobby = getLobby();
        if (lobby == null) return null;
        return lobby.getParent();
    }

    public static Guild getGuild() {
        VoiceChannel lobby = getLobby();
        if (lobby == null) return null;
        return lobby.getGuild();
    }

    public static Member getMember(UUID player) {
        String discordID = getPlugin().getConfig().getString("link." + player);
        Guild guild = getGuild();
        if (guild == null) return null;
        return discordID != null ? guild.getMemberById(discordID) : null;
    }

    public static int getHorizontalRadius() {
        return getPlugin().getConfig().getInt(HORIZONTAL_RADIUS_FIELD);
    }

    public static void setHorizontalRadius(int horizontalRadius) {
        getPlugin().getConfig().set(HORIZONTAL_RADIUS_FIELD, horizontalRadius);
        getPlugin().saveConfig();
    }

    public static int getVerticalRadius() {
        return getPlugin().getConfig().getInt(VERTICAL_RADIUS_FIELD);
    }

    public static void setVerticalRadius(int verticalRadius) {
        getPlugin().getConfig().set(VERTICAL_RADIUS_FIELD, verticalRadius);
        getPlugin().saveConfig();
    }

    public static boolean getActionBarAlert() {
        return getPlugin().getConfig().getBoolean(ACTION_BAR_ALERT_FIELD);
    }

    public static void setActionBarAlert(boolean actionBarAlert) {
        getPlugin().getConfig().set(ACTION_BAR_ALERT_FIELD, actionBarAlert);
        getPlugin().saveConfig();
    }

    public static boolean getChannelVisibility() {
        return getPlugin().getConfig().getBoolean(CHANNEL_VISIBILITY_FIELD);
    }

    public static void setChannelVisibility(boolean channelVisibility) {
        getPlugin().getConfig().set(CHANNEL_VISIBILITY_FIELD, channelVisibility);
        getPlugin().saveConfig();
    }

    public static void unlinkUser(String discordID, String minecraftID) {
        getPlugin().getConfig().set("link." + minecraftID, null);
        getPlugin().getConfig().set("link." + discordID, null);
        getPlugin().saveConfig();
    }
}
