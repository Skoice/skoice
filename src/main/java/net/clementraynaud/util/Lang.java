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

        Console(Map<String, String> languageMessageMap) {
            this.languageMessageMap = languageMessageMap;
        }

        public String print() {
            return languageMessageMap.getOrDefault(getPlugin().getConfigFile().getString("language"), languageMessageMap.get("EN"));
        }
    }

    public enum Discord {

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
            put("EN", "§dSkoice §8• §7This command is §conly executable §7by players.");
            put("FR", "§dSkoice §8• §7Cette commande est §cseulement exécutable §7par les joueurs.");
        }}),
        NOT_CONFIGURED_CORRECTLY_INTERACTIVE(new HashMap<String, String>() {{
            put("EN", "?");
            put("FR", "?");
        }}),
        NOT_CONFIGURED_CORRECTLY_COMMAND(new HashMap<String, String>() {{
            put("EN", "§dSkoice §8• §7Skoice is §cnot configured correctly§7. Type \"§e/configure§7\" to set it up.");
            put("FR", "§dSkoice §8• §7Skoice n'est §cpas configuré correctement§7. Tapez \"§e/configure§7\" pour le configurer.");
        }}),
        NOT_CONFIGURED_CORRECTLY_DISCORD(new HashMap<String, String>() {{
            put("EN", "§dSkoice §8• §7Skoice is §cnot configured correctly§7. Type \"§e/configure§7\" on your Discord server to set it up.");
            put("FR", "§dSkoice §8• §7Skoice n'est §cpas configuré correctement§7. Tapez \"§e/configure§7\" sur votre serveur Discord pour le configurer.");
        }}),
        BOT_CREATION_INTERACTIVE(new HashMap<String, String>() {{
            put("EN", "?");
            put("FR", "?");
        }}),
        BOT_CREATION_LINK(new HashMap<String, String>() {{
            put("EN", " \n§dSkoice §8• §7Configuration (§fBot Creation§7)\n \n§8• §7First, you need to create a bot and invite it to your Discord server. Please follow the instructions on this page: §bhttps://github.com/carlodrift/skoice/wiki§7.\n§8• §7When you have retrieved its token, type \"§e/token§7\" followed by the token.\n§8• §7Once done, type \"§e/configure§7\" on your Discord server to go to the next step.\n ");
            put("FR", " \n§dSkoice §8• §7Configuration (§fCréation du bot§7)\n \n§8• §7Tout d'abord, vous devez créer un bot et l'inviter sur votre serveur Discord. Merci de suivre les instructions sur cette page: §bhttps://github.com/carlodrift/skoice/wiki§7.\n§8• §7Une fois son token récupéré, tapez \"§e/token§7\" suivi du token.\n§8• §7Une fois fait, tapez \"§e/configure§7\" sur votre serveur Discord pour passer à l'étape suivante.\n ");
        }}),
        ALREADY_CONFIGURED(new HashMap<String, String>() {{
            put("EN", "§dSkoice §8• §7Skoice is §calready configured§7. Type \"§e/configure§7\" on your Discord server to update its settings.");
            put("FR", "§dSkoice §8• §7Skoice est §cdéjà configuré§7. Tapez \"§e/configure§7\" sur votre serveur Discord pour mettre à jour ses paramètres.");
        }}),
        NO_TOKEN(new HashMap<String, String>() {{
            put("EN", "§dSkoice §8• §7You have §cnot provided a token§7.");
            put("FR", "§dSkoice §8• §7Vous n'avez §cpas fourni de token§7.");
        }}),
        INVALID_TOKEN(new HashMap<String, String>() {{
            put("EN", "§dSkoice §8• §7You have §cnot provided a valid token§7.");
            put("FR", "§dSkoice §8• §7Vous n'avez §cpas fourni de token valide§7.");
        }}),
        ALREADY_CONNECTED(new HashMap<String, String>() {{
            put("EN", "§dSkoice §8• §7A bot is §calready connected§7. Restart your Minecraft server to apply the new token.");
            put("FR", "§dSkoice §8• §7Un bot is §cdéjà connecté§7. Redémarrez votre serveur Minecraft pour appliquer le nouveau token.");
        }});

        private final Map<String, String> languageMessageMap;

        Minecraft(Map<String, String> languageMessageMap) {
            this.languageMessageMap = languageMessageMap;
        }

        public String print() {
            return languageMessageMap.getOrDefault(getPlugin().getConfigFile().getString("language"), languageMessageMap.get("EN"));
        }
    }
}
