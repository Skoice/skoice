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

import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

import static net.clementraynaud.Skoice.getPlugin;

public class Lang {

    public enum Console {

        PLUGIN_ENABLED_INFO(new HashMap<String, String>() {{
            put("EN", "Plugin enabled!");
            put("FR", "Plugin activé !");
        }}),
        PLUGIN_DISABLED_INFO(new HashMap<String, String>() {{
            put("EN", "Plugin disabled!");
            put("FR", "Plugin désactivé !");
        }}),
        OUTDATED_VERSION_WARNING(new HashMap<String, String>() {{
            put("EN", ChatColor.YELLOW + "You are using an outdated version ({runningVersion}). Download the latest version ({latestVersion}) here: " + ChatColor.AQUA + "https://www.spigotmc.org/resources/skoice-proximity-voice-chat.82861/" + ChatColor.YELLOW + ".");
            put("FR", ChatColor.YELLOW + "Vous utilisez une version obsolète ({runningVersion}). Téléchargez la dernière version ({latestVersion}) ici : " + ChatColor.AQUA + "https://www.spigotmc.org/resources/skoice-proximity-voice-chat.82861/" + ChatColor.YELLOW + ".");
        }}),
        NO_TOKEN_WARNING(new HashMap<String, String>() {{
            put("EN", ChatColor.YELLOW + "Token not set, join your Minecraft server to set up Skoice.");
            put("FR", ChatColor.YELLOW + "Token non défini, rejoignez votre serveur Minecraft pour configurer Skoice.");
        }}),
        NO_LANGUAGE_WARNING(new HashMap<String, String>() {{
            put("EN", ChatColor.YELLOW + "Language not set, type \"/configure\" on your Discord server to set up Skoice.");
        }}),
        MULTIPLE_GUILDS_WARNING(new HashMap<String, String>() {{
            put("EN", ChatColor.YELLOW + "Your bot is on multiple Discord servers, type \"/configure\" on your Discord server to choose one.");
            put("FR", ChatColor.YELLOW + "Votre bot est sur plusieurs serveurs Discord, tapez \"/configure\" sur votre serveur Discord pour en choisir un.");
        }}),
        NO_LOBBY_ID_WARNING(new HashMap<String, String>() {{
            put("EN", ChatColor.YELLOW + "Lobby not set, type \"/configure\" on your Discord server to set up Skoice.");
            put("FR", ChatColor.YELLOW + "Salon vocal principal non défini, tapez \"/configure\" sur votre serveur Discord pour configurer Skoice.");
        }}),
        NO_DISTANCES_WARNING(new HashMap<String, String>() {{
            put("EN", ChatColor.YELLOW + "Radius not set, join your Minecraft server to set up Skoice.");
            put("FR", ChatColor.YELLOW + "Rayons non définis, tapez \"/configure\" sur votre serveur Discord pour configurer Skoice.");
        }}),
        MISSING_ACCESS_ERROR(new HashMap<String, String>() {{
            put("EN", ChatColor.RED + "You did not grant your bot the permission to register commands on your Discord server (\"applications.commands\").");
            put("FR", ChatColor.RED + "Vous n'avez pas accordé à votre bot la permission de déclarer des commandes sur votre serveur Discord (\"applications.commands\").");
        }});

        private final Map<String, String> languageMessageMap;

        Console(Map<String, String> languageMessageMap) {
            this.languageMessageMap = languageMessageMap;
        }

        public String print() {
            return languageMessageMap.getOrDefault(getPlugin().getConfigFile().getString("language"), languageMessageMap.get("EN"));
        }
    }

    public enum Discord {;

        private final Map<String, String> languageMessageMap;

        Discord(Map<String, String> languageMessageMap) {
            this.languageMessageMap = languageMessageMap;
        }

        public String print() {
            return languageMessageMap.getOrDefault(getPlugin().getConfigFile().getString("language"), languageMessageMap.get("EN"));
        }
    }

    public enum Minecraft {

        ILLEGAL_EXECUTOR(new HashMap<String, String>() {{
            put("EN", CHAT_PREFIX + "This command is " + ChatColor.RED + "only executable " + ChatColor.GRAY + "by players.");
            put("FR", CHAT_PREFIX + "Cette commande est " + ChatColor.RED + "seulement exécutable " + ChatColor.GRAY + "par les joueurs.");
        }}),
        NOT_CONFIGURED_CORRECTLY_INTERACTIVE(new HashMap<String, String>() {{
            put("EN", "?");
            put("FR", "?");
        }}),
        NOT_CONFIGURED_CORRECTLY_COMMAND(new HashMap<String, String>() {{
            put("EN", CHAT_PREFIX + "Skoice is " + ChatColor.RED + "not configured correctly" + ChatColor.GRAY + ". Type \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" to set it up.");
            put("FR", CHAT_PREFIX + "Skoice n'est " + ChatColor.RED + "pas configuré correctement" + ChatColor.GRAY + ". Tapez \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" pour le configurer.");
        }}),
        NOT_CONFIGURED_CORRECTLY_DISCORD(new HashMap<String, String>() {{
            put("EN", CHAT_PREFIX + "Skoice is " + ChatColor.RED + "not configured correctly" + ChatColor.GRAY + ". Type \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" on your Discord server to set it up.");
            put("FR", CHAT_PREFIX + "Skoice n'est " + ChatColor.RED + "pas configuré correctement" + ChatColor.GRAY + ". Tapez \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" sur votre serveur Discord pour le configurer.");
        }}),
        BOT_CREATION_INTERACTIVE(new HashMap<String, String>() {{
            put("EN", "?");
            put("FR", "?");
        }}),
        BOT_CREATION_LINK(new HashMap<String, String>() {{
            put("EN", " \n" + CHAT_PREFIX + "Configuration (" + ChatColor.WHITE + "Bot Creation" + ChatColor.GRAY + ")\n \n" + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "First, you need to create a bot and invite it to your Discord server. Please follow the instructions on this page: " + ChatColor.AQUA + "https://github.com/carlodrift/skoice/wiki" + ChatColor.GRAY + ".\n" + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "When you have retrieved its token, type \"" + ChatColor.YELLOW + "/token" + ChatColor.GRAY + "\" followed by the token.\n" + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "Once done, type \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" on your Discord server to go to the next step.\n ");
            put("FR", " \n" + CHAT_PREFIX + "Configuration (" + ChatColor.WHITE + "Création du bot" + ChatColor.GRAY + ")\n \n" + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "Tout d'abord, vous devez créer un bot et l'inviter sur votre serveur Discord. Merci de suivre les instructions sur cette page: " + ChatColor.AQUA + "https://github.com/carlodrift/skoice/wiki" + ChatColor.GRAY + ".\n" + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "Une fois son token récupéré, tapez \"" + ChatColor.YELLOW + "/token" + ChatColor.GRAY + "\" suivi du token.\n" + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "Une fois fait, tapez \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" sur votre serveur Discord pour passer à l'étape suivante.\n ");
        }}),
        ALREADY_CONFIGURED(new HashMap<String, String>() {{
            put("EN", CHAT_PREFIX + "Skoice is " + ChatColor.RED + "already configured" + ChatColor.GRAY + ". Type \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" on your Discord server to update its settings.");
            put("FR", CHAT_PREFIX + "Skoice est " + ChatColor.RED + "déjà configuré" + ChatColor.GRAY + ". Tapez \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" sur votre serveur Discord pour mettre à jour ses paramètres.");
        }}),
        NO_TOKEN(new HashMap<String, String>() {{
            put("EN", CHAT_PREFIX + "You have " + ChatColor.RED + "not provided a token" + ChatColor.GRAY + ".");
            put("FR", CHAT_PREFIX + "Vous n'avez " + ChatColor.RED + "pas fourni de token" + ChatColor.GRAY + ".");
        }}),
        INVALID_TOKEN(new HashMap<String, String>() {{
            put("EN", CHAT_PREFIX + "You have " + ChatColor.RED + "not provided a valid token" + ChatColor.GRAY + ".");
            put("FR", CHAT_PREFIX + "Vous n'avez " + ChatColor.RED + "pas fourni de token valide" + ChatColor.GRAY + ".");
        }}),
        ALREADY_CONNECTED(new HashMap<String, String>() {{
            put("EN", CHAT_PREFIX + "A bot is " + ChatColor.RED + "already connected" + ChatColor.GRAY + ". Restart your Minecraft server to apply the new token.");
            put("FR", CHAT_PREFIX + "Un bot is " + ChatColor.RED + "déjà connecté" + ChatColor.GRAY + ". Redémarrez votre serveur Minecraft pour appliquer le nouveau token.");
        }});

        private static final String CHAT_PREFIX = ChatColor.LIGHT_PURPLE + "Skoice " + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY;

        private final Map<String, String> languageMessageMap;

        Minecraft(Map<String, String> languageMessageMap) {
            this.languageMessageMap = languageMessageMap;
        }

        public String print() {
            return languageMessageMap.getOrDefault(getPlugin().getConfigFile().getString("language"), languageMessageMap.get("EN"));
        }
    }
}
