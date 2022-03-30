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
import java.util.stream.Collectors;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.bot.Bot.*;

public class Config {

    public static final String TOKEN_FIELD = "token";
    public static final String LANG_FIELD = "lang";
    public static final String LOBBY_ID_FIELD = "lobby-id";
    public static final String HORIZONTAL_RADIUS_FIELD = "horizontal-radius";
    public static final String VERTICAL_RADIUS_FIELD = "vertical-radius";
    public static final String ACTION_BAR_ALERT_FIELD = "action-bar-alert";
    public static final String CHANNEL_VISIBILITY_FIELD = "channel-visibility";
    public static final String LINK_MAP_FIELD = "link-map";
    public static final String TEMP_FIELD = "temp";
    public static final String TEMP_GUILD_ID_FIELD = TEMP_FIELD + ".guild-id";
    public static final String TEMP_TEXT_CHANNEL_ID_FIELD = TEMP_FIELD + ".text-channel-id";
    public static final String TEMP_MESSAGE_ID_FIELD = TEMP_FIELD + ".message-id";

    private Config() {
    }

    public static Map<String, String> getLinkMap() {
        Map<String, String> castedLinkMap = new HashMap<>();
        if (getPlugin().getConfig().isSet(LINK_MAP_FIELD)) {
            Map<String, Object> linkMap = new HashMap<>(getPlugin().getConfig().getConfigurationSection(LINK_MAP_FIELD).getValues(false));
            for (Map.Entry<String, Object> entry : linkMap.entrySet())
                castedLinkMap.put(entry.getKey(), entry.getValue().toString());
        }
        return castedLinkMap;
    }

    public static String getKeyFromValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet())
            if (Objects.equals(value, entry.getValue()))
                return entry.getKey();

        return null;
    }

    public static Member getMember(UUID minecraftID) {
        String discordID = getLinkMap().get(minecraftID);
        Guild guild = getGuild();
        if (guild == null) return null;
        return discordID != null ? guild.getMemberById(discordID) : null;
    }

    public static void setToken(String token) {
        byte[] tokenBytes = token.getBytes();
        for (int i = 0; i < tokenBytes.length; i++) {
            tokenBytes[i]++;
        }
        getPlugin().getConfig().set(TOKEN_FIELD, Base64.getEncoder().encodeToString(tokenBytes));
        getPlugin().saveConfig();
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

    public static int getHorizontalRadius() {
        return getPlugin().getConfig().getInt(HORIZONTAL_RADIUS_FIELD);
    }

    public static int getVerticalRadius() {
        return getPlugin().getConfig().getInt(VERTICAL_RADIUS_FIELD);
    }

    public static boolean getActionBarAlert() {
        return getPlugin().getConfig().getBoolean(ACTION_BAR_ALERT_FIELD);
    }

    public static boolean getChannelVisibility() {
        return getPlugin().getConfig().getBoolean(CHANNEL_VISIBILITY_FIELD);
    }

    public static void linkUser(String minecraftID, String discordID) {
        Map<String, String> linkMap = getLinkMap();
        linkMap.put(minecraftID, discordID);
        getPlugin().getConfig().set(LINK_MAP_FIELD, linkMap);
        getPlugin().saveConfig();
    }

    public static void unlinkUser(String minecraftID) {
        Map<String, String> linkMap = getLinkMap();
        linkMap.remove(minecraftID);
        getPlugin().getConfig().set(LINK_MAP_FIELD, linkMap);
        getPlugin().saveConfig();
    }
}
