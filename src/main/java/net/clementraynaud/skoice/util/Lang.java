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

package net.clementraynaud.skoice.util;

import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;

import static net.clementraynaud.skoice.Skoice.getPlugin;

public class Lang {

    private static final String CHAT_PREFIX = ChatColor.LIGHT_PURPLE + "Skoice " + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY;

    public enum Console {

        PLUGIN_ENABLED_INFO(translations(
                "Plugin enabled.",
                "Plugin activé.")),
        PLUGIN_DISABLED_INFO(translations(
                "Plugin disabled.",
                "Plugin désactivé.")),
        CONFIGURATION_COMPLETE_INFO(translations(
                "Configuration complete. Type \"/link\" on your Discord server to link your Discord account to Minecraft.",
                "Configuration terminée. Tapez \"/link\" sur votre serveur Discord pour relier votre compte Discord à Minecraft.")),
        BOT_CONNECTED_INFO(translations(
                "Your bot is connected.",
                "Votre bot est connecté.")),
        OUTDATED_VERSION_WARNING(translations(
                ChatColor.YELLOW + "You are using an outdated version ({runningVersion}). Download the latest version ({latestVersion}) here: " + ChatColor.AQUA + "https://www.spigotmc.org/resources/skoice-proximity-voice-chat.82861/" + ChatColor.YELLOW + ".",
                ChatColor.YELLOW + "Vous utilisez une version obsolète ({runningVersion}). Téléchargez la dernière version ({latestVersion}) ici : " + ChatColor.AQUA + "https://www.spigotmc.org/resources/skoice-proximity-voice-chat.82861/" + ChatColor.YELLOW + ".")),
        NO_TOKEN_WARNING(translations(
                ChatColor.YELLOW + "Token not set. Join your Minecraft server to set up Skoice.",
                ChatColor.YELLOW + "Token non défini. Rejoignez votre serveur Minecraft pour configurer Skoice.")),
        NO_LANGUAGE_WARNING(translations(
                ChatColor.YELLOW + "Language not set. Type \"/configure\" on your Discord server to set up Skoice.",
                null)),
        MULTIPLE_GUILDS_WARNING(translations(
                ChatColor.YELLOW + "Your bot is on multiple Discord servers. Type \"/configure\" on your Discord server to choose one.",
                ChatColor.YELLOW + "Votre bot est sur plusieurs serveurs Discord. Tapez \"/configure\" sur votre serveur Discord pour en choisir un.")),
        NO_LOBBY_ID_WARNING(translations(
                ChatColor.YELLOW + "Lobby not set. Type \"/configure\" on your Discord server to set up Skoice.",
                ChatColor.YELLOW + "Salon vocal principal non défini. Tapez \"/configure\" sur votre serveur Discord pour configurer Skoice.")),
        NO_DISTANCES_WARNING(translations(
                ChatColor.YELLOW + "Radius not set. Type \"/configure\" on your Discord server to set up Skoice.",
                ChatColor.YELLOW + "Rayons non définis. Tapez \"/configure\" sur votre serveur Discord pour configurer Skoice.")),
        MISSING_ACCESS_ERROR(translations(
                ChatColor.RED + "You did not grant your bot the permission to register commands on your Discord server (\"applications.commands\").",
                ChatColor.RED + "Vous n'avez pas accordé à votre bot la permission de déclarer des commandes sur votre serveur Discord (\"applications.commands\").")),
        BOT_COULD_NOT_CONNECT_ERROR(translations(
                ChatColor.RED + "Your bot could not connect. To update the token, type \"/token\" followed by the new token.",
                ChatColor.RED + "Votre bot n'a pas pu se connecter. Pour mettre à jour le token, tapez \"/token\" suivi du nouveau token.")),
        UNEXPECTED_VALUE(translations(
                "Unexpected value: {value}",
                "Valeur inattendue : {value}"));

        private final Map<String, String> languageMessageMap;

        Console(Map<String, String> languageMessageMap) {
            this.languageMessageMap = languageMessageMap;
        }

        private static Map<String, String> translations(String en, String fr) {
            Map<String, String> languageMessageMap = new HashMap<>();
            languageMessageMap.put("EN", en);
            languageMessageMap.put("FR", fr);
            return languageMessageMap;
        }

        public String print() {
            return languageMessageMap.getOrDefault(getPlugin().getConfigFile().getString("language"), languageMessageMap.get("EN"));
        }
    }

    public enum Discord {

        CONFIGURATION_EMBED_TITLE(translations(
                "Configuration",
                "Configuration")),
        SERVER_EMBED_TITLE(translations(
                "Server",
                "Serveur")),
        SERVER_EMBED_DESCRIPTION(translations(
                "In order to work properly, your bot cannot be present on multiple Discord servers. Please select the server where you want the proximity voice chat to be active. Your bot will automatically leave the other ones.",
                "Afin de fonctionner correctement, votre bot ne peut pas être présent sur plusieurs serveurs Discord. Veuillez sélectionnez le serveur où vous voulez que le chat vocal de proximité soit actif. Votre bot quittera automatiquement les autres.")),
        SERVER_SELECT_MENU_PLACEHOLDER(translations(
                "Select a Server",
                "Sélectionnez un serveur")),
        LOBBY_EMBED_TITLE(translations(
                "Lobby",
                "Salon vocal principal")),
        LOBBY_EMBED_DESCRIPTION(translations(
                "Select the channel players have to join to use the proximity voice chat.",
                "Sélectionnez le salon que les joueurs doivent rejoindre pour utiliser le chat vocal de proximité.")),
        LOBBY_EMBED_ALTERNATIVE_DESCRIPTION(translations(
                "Select the channel players have to join to use the proximity voice chat. It must be in a category.",
                "Sélectionnez le salon que les joueurs doivent rejoindre pour utiliser le chat vocal de proximité. Il doit se trouver dans une catégorie.")),
        LOBBY_SELECT_MENU_PLACEHOLDER(translations(
                "Select a Voice Channel",
                "Sélectionnez un salon vocal")),
        NEW_VOICE_CHANNEL_SELECT_OPTION_LABEL(translations(
                "New Voice Channel",
                "Nouveau salon vocal")),
        NEW_VOICE_CHANNEL_SELECT_OPTION_DESCRIPTION(translations(
                "Skoice will automatically set up a voice channel",
                "Skoice configurera automatiquement un salon vocal.")),
        DEFAULT_CATEGORY_NAME(translations(
                "Proximity Voice Chat",
                "Chat vocal de proximité")),
        DEFAULT_LOBBY_NAME(translations(
                "Lobby",
                "Salon vocal principal")),
        MODE_EMBED_TITLE(translations(
                "Mode",
                "Mode")),
        MODE_EMBED_DESCRIPTION(translations(
                "Select a mode or customize the distances.",
                "Sélectionnez un mode ou personnalisez les distances.")),
        MODE_EMBED_ALTERNATIVE_DESCRIPTION(translations(
                "Let us choose the best settings for your personal use of Skoice. You will still be able to customize the distances later.",
                "Laissez-nous choisir les meilleures réglages pour votre utilisation personnelle de Skoice. Vous pourrez toujours personnaliser les distances plus tard.")),
        VANILLA_MODE_FIELD_TITLE(translations(
                "Vanilla Mode",
                "Mode standard")),
        VANILLA_MODE_FIELD_DESCRIPTION(translations(
                "Choose this mode if you plan to play open world game modes (longer distances).",
                "Choisissez ce mode si vous prévoyez de jouer sur des modes de jeu en monde ouvert (distances plus longues).")),
        VANILLA_MODE_FIELD_ALTERNATIVE_DESCRIPTION(translations(
                "Horizontal Radius: 80 blocks — Vertical Radius: 40 blocks",
                "Rayon horizontal : 80 blocs — Rayon vertical : 40 blocs")),
        MINIGAME_MODE_FIELD_TITLE(translations(
                "Minigame Mode",
                "Mode mini-jeu")),
        MINIGAME_MODE_FIELD_DESCRIPTION(translations(
                "Choose this mode if you plan to play game modes that only require a limited area (shorter distances).",
                "Choisissez ce mode si vous prévoyez de jouer sur des modes de jeu qui requièrent seulement une zone limitée (distances plus courtes).")),
        MINIGAME_MODE_FIELD_ALTERNATIVE_DESCRIPTION(translations(
                "Horizontal Radius: 40 blocks — Vertical Radius: 20 blocks",
                "Rayon horizontal : 40 blocs — Rayon vertical : 20 blocs")),
        MODE_SELECT_OPTION_PLACEHOLDER(translations(
                "Select a Mode",
                "Sélectionnez un mode")),
        CUSTOMIZE_FIELD_TITLE(translations(
                "Customize",
                "Personnaliser")),
        CUSTOMIZE_FIELD_DESCRIPTION(translations(
                "Set distances according to your personal preferences.",
                "Définissez les distances en fonction de vos préférences personnelles.")),
        CUSTOMIZE_SELECT_MENU_DESCRIPTION(translations(
                "Set distances from 1 to 1000 blocks.",
                "Définissez les distances de 1 à 1000 blocs.")),
        CUSTOMIZE_SELECT_MENU_ALTERNATIVE_DESCRIPTION(translations(
                "Horizontal Radius: {horizontalRadius} blocks — Vertical Radius: {verticalRadius} blocks",
                "Rayon horizontal : {horizontalRadius} blocs — Rayon vertical : {verticalRadius} blocs")),
        HORIZONTAL_RADIUS_EMBED_TITLE(translations(
                "Horizontal Radius",
                "Rayon horizontal")),
        HORIZONTAL_RADIUS_EMBED_DESCRIPTION(translations(
                "This is the maximum horizontal distance between players.",
                "Il s'agit de la distance horizontale maximale entre les joueurs.")),
        VERTICAL_RADIUS_EMBED_TITLE(translations(
                "Vertical Radius",
                "Rayon vertical")),
        ENTER_A_VALUE_FIELD_TITLE(translations(
                "Enter a Value",
                "Entrez une valeur")),
        ENTER_A_VALUE_FIELD_DESCRIPTION(translations(
                "This setting is currently set to {value} blocks.\nThe value must be between 1 and 1000.",
                "Cette option est actuellement définie sur {value} blocs.\nLa valeur doit être comprise entre 1 et 1000.")),
        VERTICAL_RADIUS_EMBED_DESCRIPTION(translations(
                "This is the maximum vertical distance between players.",
                "Il s'agit de la distance verticale maximale entre les joueurs.")),
        ADVANCED_SETTINGS_EMBED_TITLE(translations(
                "Advanced Settings",
                "Réglages avancées")),
        ADVANCED_SETTINGS_EMBED_DESCRIPTION(translations(
                "Manage other parameters.",
                "Gérez d'autres paramètres.")),
        ACTION_BAR_ALERT_EMBED_TITLE(translations(
                "Action Bar Alert",
                "Alerte dans la barre d'action")),
        ACTION_BAR_ALERT_EMBED_DESCRIPTION(translations(
                "Toggle the alert sent to players who are moving away before they get disconnected from their current voice channel.",
                "Activez ou désactivez l'alerte envoyée aux joueurs qui s'éloignent avant qu'ils soient déconnectés de leur salon vocal actuel.")),
        CHANNEL_VISIBILITY_EMBED_TITLE(translations(
                "Channel Visibility",
                "Visibilité des salons")),
        CHANNEL_VISIBILITY_EMBED_DESCRIPTION(translations(
                "Toggle the visibility of the temporary channels created by Skoice.",
                "Rendez visibles ou invisibles les salons temporaires créés par Skoice.")),
        LANGUAGE_EMBED_TITLE(translations(
                "Language",
                "Langue")),
        LANGUAGE_EMBED_DESCRIPTION(translations(
                "Choose the language used to display messages.",
                "Choisissez la langue utilisée pour afficher les messages.")),
        LANGUAGE_SELECT_MENU_PLACEHOLDER(translations(
                "Select a Language",
                "Sélectionnez une langue")),
        TROUBLESHOOTING_FIELD_TITLE(translations(
                "Troubleshooting",
                "Assistance")),
        TROUBLESHOOTING_FIELD_DESCRIPTION(translations(
                "Having issues? [Join our Discord server!](https://discord.gg/h3Tgccc)",
                "Vous rencontrez des problèmes ? [Rejoignez notre serveur Discord !](https://discord.gg/h3Tgccc)")),
        GET_THE_PROXIMITY_VOICE_CHAT_EMBED_TITLE(translations(
                "Get the Proximity Voice Chat",
                "Obtenir le chat vocal de proximité")),
        DOWNLOAD_SKOICE_FIELD_TITLE(translations(
                "Download Skoice",
                "Téléchargez Skoice")),
        DOWNLOAD_SKOICE_FIELD_DESRIPTION(translations(
                "Get the plugin [here](https://www.spigotmc.org/resources/skoice-proximity-voice-chat.82861/) and install it on your Minecraft server.",
                "Obtenez le plugin [ici](https://www.spigotmc.org/resources/skoice-proximity-voice-chat.82861/) et installez-le sur votre serveur Minecraft.")),
        LINKING_PROCESS_EMBED_TITLE(translations(
                "Linking Process",
                "Procédure de liaison")),
        VERIFICATION_CODE_FIELD_TITLE(translations(
                "Verification Code",
                "Code de vérification")),
        VERIFICATION_CODE_FIELD_DESCRIPTION(translations(
                "Type `/link {code}` in game to complete the process.",
                "Tapez `/link {code}` en jeu pour terminer la procédure.")),
        ACCOUNT_LINKED_FIELD_TITLE(translations(
                "Account Linked",
                "Compte lié")),
        ACCOUNT_LINKED_FIELD_DESCRIPTION(translations(
                "Your Discord account has been linked to Minecraft.",
                "Votre compte Discord a été lié à Minecraft.")),
        ACCOUNT_UNLINKED_FIELD_TITLE(translations(
                "Account Unlinked",
                "Compte délié")),
        ACCOUNT_UNLINKED_FIELD_DESCRIPTION(translations(
                "Your Discord account has been unlinked from Minecraft.",
                "Votre compte Discord a été délié de Minecraft.")),
        ACCOUNT_NOT_LINKED_FIELD_TITLE(translations(
                "Account Not Linked",
                "Compte non lié")),
        ACCOUNT_NOT_LINKED_FIELD_DESCRIPTION(translations(
                "Your Discord account is not linked to Minecraft.\nType `/link` to link it.",
                "Votre compte Discord n'est pas lié à Minecraft.\nTapez `/link` pour le lier.")),
        ACCOUNT_NOT_LINKED_FIELD_ALTERNATIVE_DESCRIPTION(translations(
                "Your Discord account is not linked to Minecraft.\nType `/link` on \"{discordServer}\" to link it.",
                "Votre compte Discord n'est pas lié à Minecraft.\nTapez `/link` sur \"{discordServer}\" pour le lier.")),
        ACCOUNT_NOT_LINKED_FIELD_GENERIC_ALTERNATIVE_DESCRIPTION(translations(
                "Your Discord account is not linked to Minecraft.\nType `/link` on our Discord server to link it.",
                "Votre compte Discord n'est pas lié à Minecraft.\nTapez `/link` sur notre serveur Discord pour le lier.")),
        ACCOUNT_ALREADY_LINKED_FIELD_TITLE(translations(
                "Account Already Linked",
                "Compte déjà lié")),
        ACCOUNT_ALREADY_LINKED_FIELD_DESCRIPTION(translations(
                "Your Discord account is already linked to Minecraft.\nType `/unlink` to unlink it.",
                "Votre compte Discord est déjà lié à Minecraft\nTapez `/unlink` pour le délier.")),
        BACK_BUTTON_LABEL(translations(
                "Back",
                "Retour")),
        REFRESH_BUTTON_LABEL(translations(
                "Refresh",
                "Rafraîchir")),
        CLOSE_BUTTON_LABEL(translations(
                "Close",
                "Fermer")),
        ENABLED_SELECT_OPTION_LABEL(translations(
                "Enabled",
                "Activé")),
        DISABLED_SELECT_OPTION_LABEL(translations(
                "Disabled",
                "Désactivé")),
        DEFAULT_SELECT_OPTION_DESCRIPTION(translations(
                "This option is selected by default.",
                "Cette option est sélectionnée par défaut.")),
        TOO_MANY_OPTIONS_SELECT_OPTION_LABEL(translations(
                "Too Many Options",
                "Trop d'options")),
        TOO_MANY_OPTIONS_SELECT_OPTION_DESCRIPTION(translations(
                "Skoice is unabled to load more options.",
                "Skoice est incapable de charger plus d'options.")),
        CONFIGURE_LATER_BUTTON_LABEL(translations(
                "Configure Later",
                "Configurer plus tard")),
        ERROR_EMBED_TITLE(translations(
                "Error",
                "Erreur")),
        ACCESS_DENIED_FIELD_TITLE(translations(
                "Access Denied",
                "Accès refusé")),
        ACCESS_DENIED_FIELD_DESCRIPTION(translations(
                "You do not have the required permission to execute this action.",
                "Vous n'avez la permission requise pour effectuer cette action.")),
        TOO_MANY_INTERACTIONS_FIELD_TITLE(translations(
                "Too Many Interactions",
                "Trop d'interactions")),
        TOO_MANY_INTERACTIONS_FIELD_DESCRIPTION(translations(
                "You are executing commands too fast",
                "Vous exécutez des commandes trop rapidement.")),
        INCOMPLETE_CONFIGURATION_FIELD_TITLE(translations(
                "Incomplete Configuration",
                "Configuration non terminée")),
        INCOMPLETE_CONFIGURATION_FIELD_DESCRIPTION(translations(
                "Skoice is not configured correctly. Please contact an administrator.",
                "Skoice n'est pas configuré correctement. Veuillez contacter un administrateur.")),
        INCOMPLETE_CONFIGURATION_SERVER_MANAGER_FIELD_DESCRIPTION(translations(
                "Type `/configure` on your Discord server to complete the configuration and use Skoice.",
                "Tapez `/configure` sur votre serveur Discord pour terminer la configuration and utiliser Skoice.")),
        INCOMPLETE_CONFIGURATION_SERVER_MANAGER_FIELD_ALTERNATIVE_DESCRIPTION(translations(
                "You have either moved or deleted the lobby.\nType `/configure` on your Discord server to complete the configuration and use Skoice.",
                "Vous avez soit déplacé, soit supprimé le salon vocal principal. Tapez `/configure` sur votre serveur Discord pour terminer la configuration and utiliser Skoice.")),
        ILLEGAL_INTERACTION_FIELD_TITLE(translations(
                "Illegal Interaction",
                "Interaction interdite")),
        ILLEGAL_INTERACTION_FIELD_DESCRIPTION(translations(
                "You can only interact with the bot on a Discord server.",
                "Vous pouvez seulement interagir avec le bot sur un serveur Discord."));

        private final Map<String, String> languageMessageMap;

        Discord(Map<String, String> languageMessageMap) {
            this.languageMessageMap = languageMessageMap;
        }

        private static Map<String, String> translations(String en, String fr) {
            Map<String, String> languageMessageMap = new HashMap<>();
            languageMessageMap.put("EN", en);
            languageMessageMap.put("FR", fr);
            return languageMessageMap;
        }

        public String print() {
            return languageMessageMap.getOrDefault(getPlugin().getConfigFile().getString("language"), languageMessageMap.get("EN"));
        }
    }

    public enum Minecraft {

        ILLEGAL_EXECUTOR(translations(
                CHAT_PREFIX + "This command is " + ChatColor.RED + "only executable " + ChatColor.GRAY + "by players.",
                CHAT_PREFIX + "Cette commande est " + ChatColor.RED + "seulement exécutable " + ChatColor.GRAY + "par les joueurs.")),
        INCOMPLETE_CONFIGURATION(translations(
                CHAT_PREFIX + "Skoice is " + ChatColor.RED + "not configured correctly" + ChatColor.GRAY + ". Please contact an administrator.",
                CHAT_PREFIX + "Skoice n'est " + ChatColor.RED + "pas configuré correctement" + ChatColor.GRAY + ". Veuillez contacter un administrateur.")),
        INCOMPLETE_CONFIGURATION_OPERATOR_INTERACTIVE(translations(
                "?",
                "?")),
        INCOMPLETE_CONFIGURATION_OPERATOR_COMMAND(translations(
                CHAT_PREFIX + "Skoice is " + ChatColor.RED + "not configured correctly" + ChatColor.GRAY + ". Type \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" to set it up.",
                CHAT_PREFIX + "Skoice n'est " + ChatColor.RED + "pas configuré correctement" + ChatColor.GRAY + ". Tapez \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" pour le configurer.")),
        INCOMPLETE_CONFIGURATION_OPERATOR_DISCORD(translations(
                CHAT_PREFIX + "Skoice is " + ChatColor.RED + "not configured correctly" + ChatColor.GRAY + ". Type \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" on your Discord server to set it up.",
                CHAT_PREFIX + "Skoice n'est " + ChatColor.RED + "pas configuré correctement" + ChatColor.GRAY + ". Tapez \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" sur votre serveur Discord pour le configurer.")),
        BOT_CREATION_INTERACTIVE(translations(
                "?",
                "?")),
        BOT_CREATION_LINK(translations(
                " \n" + CHAT_PREFIX + "Configuration (" + ChatColor.WHITE + "Bot Creation" + ChatColor.GRAY + ")\n \n" + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "First, you need to create a bot and invite it to your Discord server. Please follow the instructions on this page: " + ChatColor.AQUA + "https://github.com/carlodrift/skoice/wiki" + ChatColor.GRAY + ".\n" + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "When you have retrieved its token, type \"" + ChatColor.YELLOW + "/token" + ChatColor.GRAY + "\" followed by the token.\n" + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "Once done, type \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" on your Discord server to go to the next step.\n ",
                " \n" + CHAT_PREFIX + "Configuration (" + ChatColor.WHITE + "Création du bot" + ChatColor.GRAY + ")\n \n" + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "Tout d'abord, vous devez créer un bot et l'inviter sur votre serveur Discord. Merci de suivre les instructions sur cette page: " + ChatColor.AQUA + "https://github.com/carlodrift/skoice/wiki" + ChatColor.GRAY + ".\n" + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "Une fois son token récupéré, tapez \"" + ChatColor.YELLOW + "/token" + ChatColor.GRAY + "\" suivi du token.\n" + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "Une fois fait, tapez \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" sur votre serveur Discord pour passer à l'étape suivante.\n ")),
        ALREADY_CONFIGURED(translations(
                CHAT_PREFIX + "Skoice is " + ChatColor.RED + "already configured" + ChatColor.GRAY + ". Type \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" on your Discord server to update its settings.",
                CHAT_PREFIX + "Skoice est " + ChatColor.RED + "déjà configuré" + ChatColor.GRAY + ". Tapez \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" sur votre serveur Discord pour mettre à jour ses réglages.")),
        NO_TOKEN(translations(
                CHAT_PREFIX + "You have " + ChatColor.RED + "not provided a token" + ChatColor.GRAY + ".",
                CHAT_PREFIX + "Vous n'avez " + ChatColor.RED + "pas fourni de token" + ChatColor.GRAY + ".")),
        INVALID_TOKEN(translations(
                CHAT_PREFIX + "You have " + ChatColor.RED + "not provided a valid token" + ChatColor.GRAY + ".",
                CHAT_PREFIX + "Vous n'avez " + ChatColor.RED + "pas fourni de token valide" + ChatColor.GRAY + ".")),
        BOT_CONNECTED(translations(
                CHAT_PREFIX + "Your bot is " + ChatColor.GREEN + "now connected" + ChatColor.GRAY + ".",
                CHAT_PREFIX + "Votre bot est " + ChatColor.GREEN + "désormais connecté" + ChatColor.GRAY + ".")),
        BOT_CONNECTED_INCOMPLETE_CONFIGURATION_DISCORD(translations(
                CHAT_PREFIX + "Your bot is " + ChatColor.GREEN + "now connected" + ChatColor.GRAY + ". Type \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" on your Discord server to set it up.",
                CHAT_PREFIX + "Votre bot est " + ChatColor.GREEN + "désormais connecté" + ChatColor.GRAY + ". Tapez \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" sur votre serveur Discord pour le configurer.")),
        BOT_ALREADY_CONNECTED(translations(
                CHAT_PREFIX + "A bot is " + ChatColor.RED + "already connected" + ChatColor.GRAY + ". Restart your Minecraft server to apply the new token.",
                CHAT_PREFIX + "Un bot is " + ChatColor.RED + "déjà connecté" + ChatColor.GRAY + ". Redémarrez votre serveur Minecraft pour appliquer le nouveau token.")),
        BOT_COULD_NOT_CONNECT(translations(
                CHAT_PREFIX + "Your bot " + ChatColor.RED + "could not connect" + ChatColor.GRAY + ". Try again with a valid token.",
                CHAT_PREFIX + "Votre bot " + ChatColor.RED + "n'a pas pu se connecter" + ChatColor.GRAY + ". Essayez de nouveau avec un token valide.")),
        ACCOUNT_LINKED(translations(
                CHAT_PREFIX + "You have " + ChatColor.GREEN + "linked your Minecraft account " + ChatColor.GRAY + "to Discord.",
                CHAT_PREFIX + "Vous avez " + ChatColor.GREEN + "lié votre compte Minecraft " + ChatColor.GRAY + "à Discord.")),
        ACCOUNT_UNLINKED(translations(
                CHAT_PREFIX + "You have " + ChatColor.RED + "unlinked your Minecraft account " + ChatColor.GRAY + "from Discord.",
                CHAT_PREFIX + "Vous avez " + ChatColor.RED + "delié votre compte Minecraft " + ChatColor.GRAY + "de Discord.")),
        ACCOUNT_NOT_LINKED(translations(
                CHAT_PREFIX + "You have " + ChatColor.RED + "not linked your Minecraft account " + ChatColor.GRAY + "to Discord. Type \"" + ChatColor.YELLOW + "/link" + ChatColor.GRAY + "\" on our Discord server to link it.",
                CHAT_PREFIX + "Vous avez " + ChatColor.RED + "pas lié votre compte Minecraft " + ChatColor.GRAY + "à Discord. Tapez \"" + ChatColor.YELLOW + "/link" + ChatColor.GRAY + "\" sur notre serveur Discord pour le lier.")),
        ACCOUNT_ALREADY_LINKED(translations(
                CHAT_PREFIX + "You have " + ChatColor.RED + "already linked your Minecraft account " + ChatColor.GRAY + "to Discord. Type \"" + ChatColor.YELLOW + "/unlink" + ChatColor.GRAY + "\" to unlink it.",
                CHAT_PREFIX + "Vous avez " + ChatColor.RED + "déjà lié votre compte Minecraft " + ChatColor.GRAY + "à Discord. Tapez \"" + ChatColor.YELLOW + "/unlink" + ChatColor.GRAY + "\" pour le délier.")),
        NO_CODE(translations(
                CHAT_PREFIX + "You have " + ChatColor.RED + "not provided a code" + ChatColor.GRAY + ". Type \"" + ChatColor.YELLOW + "/link" + ChatColor.GRAY + "\" on our Discord server to receive one.",
                CHAT_PREFIX + "Vous n'avez " + ChatColor.RED + "pas fourni de code" + ChatColor.GRAY + ". Tapez \"" + ChatColor.YELLOW + "/link" + ChatColor.GRAY + "\" sur notre serveur Discord pour en recevoir un.")),
        INVALID_CODE(translations(
                CHAT_PREFIX + "You have " + ChatColor.RED + "not provided a valid code" + ChatColor.GRAY + ". Type \"" + ChatColor.YELLOW + "/link" + ChatColor.GRAY + "\" on our Discord server to receive one.",
                CHAT_PREFIX + "Vous n'avez " + ChatColor.RED + "pas fourni de code valide" + ChatColor.GRAY + ". Tapez \"" + ChatColor.YELLOW + "/link" + ChatColor.GRAY + "\" sur notre serveur Discord pour en recevoir un.")),
        CONNECTED_TO_PROXIMITY_VOICE_CHAT(translations(
                CHAT_PREFIX + "You are " + ChatColor.GREEN + "now connected " + ChatColor.GRAY + "to the proximity voice chat.",
                CHAT_PREFIX + "Vous êtes " + ChatColor.GREEN + "désormais connecté " + ChatColor.GRAY + "au chat vocal de proximité.")),
        DISCONNECTED_FROM_PROXIMITY_VOICE_CHAT(translations(
                CHAT_PREFIX + "You are " + ChatColor.RED + "now disconnected " + ChatColor.GRAY + "from the proximity voice chat.",
                CHAT_PREFIX + "Vous êtes " + ChatColor.RED + "désormais déconnecté " + ChatColor.GRAY + "du chat vocal de proximité.")),
        ACTION_BAR_ALERT(translations(
                ChatColor.RED + "⚠ " + ChatColor.GRAY + "You are " + ChatColor.RED + "moving away " + ChatColor.GRAY + "from near players.",
                ChatColor.RED + "⚠ " + ChatColor.GRAY + "Vous vous " + ChatColor.RED + "éloignez " + ChatColor.GRAY + "des joueurs à proximité."));

        private final Map<String, String> languageMessageMap;

        Minecraft(Map<String, String> languageMessageMap) {
            this.languageMessageMap = languageMessageMap;
        }

        private static Map<String, String> translations(String en, String fr) {
            Map<String, String> languageMessageMap = new HashMap<>();
            languageMessageMap.put("EN", en);
            languageMessageMap.put("FR", fr);
            return languageMessageMap;
        }

        public String print() {
            return languageMessageMap.getOrDefault(getPlugin().getConfigFile().getString("language"), languageMessageMap.get("EN"));
        }
    }
}
