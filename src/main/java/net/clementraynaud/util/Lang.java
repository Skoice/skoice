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

        NO_TOKEN(new HashMap<String, String>() {{
            put("EN", "§cToken not set, join your Minecraft server to set up Skoice.");
            put("FR", "§cToken non défini, rejoignez votre serveur Minecraft pour configurer Skoice.");
        }}),
        NO_LANGUAGE(new HashMap<String, String>() {{
            put("EN", "§cLanguage not set, type \"/configure\" on your Discord server to set up Skoice.");
        }}),
        NO_LOBBY_ID(new HashMap<String, String>() {{
            put("EN", "§cLobby not set, type \"/configure\" on your Discord server to set up Skoice.");
            put("FR", "§cSalon vocal principal non défini, tapez \"/configure\" sur votre serveur Discord pour configurer Skoice.");
        }}),
        NO_DISTANCES(new HashMap<String, String>() {{
            put("EN", "§cRadius not set, join your Minecraft server to set up Skoice.");
            put("FR", "§cRayons non définis, tapez \"/configure\" sur votre serveur Discord pour configurer Skoice.");
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
