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
