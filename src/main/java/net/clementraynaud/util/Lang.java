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

package net.clementraynaud.util;

import java.util.HashMap;
import java.util.Map;

import static net.clementraynaud.Skoice.getPlugin;

public class Lang {

    public enum ConsoleMessage {

        PLUGIN_ENABLED_INFO(new HashMap<String, String>() {{
            put("EN", "Plugin enabled!");
            put("FR", "Plugin activé !");
        }}),
        PLUGIN_DISABLED_INFO(new HashMap<String, String>() {{
            put("EN", "Plugin disabled!");
            put("FR", "Plugin désactivé !");
        }}),
        OUTDATED_VERSION_WARNING(new HashMap<String, String>() {{
            put("EN", "§eYou are using an outdated version ({runningVersion}). Download the latest version ({latestVersion}) here: §bhttps://www.spigotmc.org/resources/skoice-proximity-voice-chat.82861/§e.");
            put("FR", "§eVous utilisez une version obsolète ({runningVersion}). Téléchargez la dernière version ({latestVersion}) ici : §bhttps://www.spigotmc.org/resources/skoice-proximity-voice-chat.82861/§e.");
        }}),
        NO_TOKEN_WARNING(new HashMap<String, String>() {{
            put("EN", "§eToken not set, join your Minecraft server to set up Skoice.");
            put("FR", "§eToken non défini, rejoignez votre serveur Minecraft pour configurer Skoice.");
        }}),
        NO_LANGUAGE_WARNING(new HashMap<String, String>() {{
            put("EN", "§eLanguage not set, type \"/configure\" on your Discord server to set up Skoice.");
        }}),
        MULTIPLE_GUILDS_WARNING(new HashMap<String, String>() {{
            put("EN", "§eYour bot is on multiple Discord servers, type \"/configure\" on your Discord server to choose one.");
            put("FR", "§eVotre bot est sur plusieurs serveurs Discord, tapez \"/configure\" sur votre serveur Discord pour en choisir un.");
        }}),
        NO_LOBBY_ID_WARNING(new HashMap<String, String>() {{
            put("EN", "§eLobby not set, type \"/configure\" on your Discord server to set up Skoice.");
            put("FR", "§eSalon vocal principal non défini, tapez \"/configure\" sur votre serveur Discord pour configurer Skoice.");
        }}),
        NO_DISTANCES_WARNING(new HashMap<String, String>() {{
            put("EN", "§eRadius not set, join your Minecraft server to set up Skoice.");
            put("FR", "§eRayons non définis, tapez \"/configure\" sur votre serveur Discord pour configurer Skoice.");
        }}),
        MISSING_ACCESS_ERROR(new HashMap<String, String>() {{
            put("EN", "§cYou did not grant your bot the permission to register commands on your Discord server (\"applications.commands\").");
            put("FR", "§cVous n'avez pas accordé à votre bot la permission de déclarer des commandes sur votre serveur Discord (\"applications.commands\").");
        }});

        private final Map<String, String> languageMessageMap;

        ConsoleMessage(Map<String, String> languageMessageMap) {
            this.languageMessageMap = languageMessageMap;
        }

        public String print() {
            return languageMessageMap.getOrDefault(getPlugin().getConfigFile().getString("language"), languageMessageMap.get("EN"));
        }
    }
}
