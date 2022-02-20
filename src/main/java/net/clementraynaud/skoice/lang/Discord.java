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

package net.clementraynaud.skoice.lang;

import java.util.Map;

import static net.clementraynaud.skoice.Skoice.getPlugin;

public enum Discord {
    CONFIGURATION_EMBED_TITLE(Map.of(
            Lang.EN, "Configuration")),

    SERVER_EMBED_TITLE(Map.of(
            Lang.EN, "Server",
            Lang.FR, "Serveur")),

    SERVER_EMBED_DESCRIPTION(Map.of(
            Lang.EN, "In order to work properly, your bot cannot be present on multiple Discord servers. Please select the server where you want the proximity voice chat to be active. Your bot will automatically leave the other ones.",
            Lang.FR, "Afin de fonctionner correctement, votre bot ne peut pas être présent sur plusieurs serveurs Discord. Veuillez sélectionnez le serveur où vous voulez que le chat vocal de proximité soit actif. Votre bot quittera automatiquement les autres.")),

    SERVER_SELECT_MENU_PLACEHOLDER(Map.of(
            Lang.EN, "Select a Server",
            Lang.FR, "Sélectionnez un serveur")),

    LOBBY_EMBED_TITLE(Map.of(
            Lang.EN, "Lobby",
            Lang.FR, "Salon vocal principal")),

    LOBBY_EMBED_DESCRIPTION(Map.of(
            Lang.EN, "Select the channel players have to join to use the proximity voice chat.",
            Lang.FR, "Sélectionnez le salon que les joueurs doivent rejoindre pour utiliser le chat vocal de proximité.")),

    LOBBY_EMBED_ALTERNATIVE_DESCRIPTION(Map.of(
            Lang.EN, "Select the channel players have to join to use the proximity voice chat. It must be in a category.",
            Lang.FR, "Sélectionnez le salon que les joueurs doivent rejoindre pour utiliser le chat vocal de proximité. Il doit se trouver dans une catégorie.")),

    LOBBY_SELECT_MENU_PLACEHOLDER(Map.of(
            Lang.EN, "Select a Voice Channel",
            Lang.FR, "Sélectionnez un salon vocal")),

    NEW_VOICE_CHANNEL_SELECT_OPTION_LABEL(Map.of(
            Lang.EN, "New Voice Channel",
            Lang.FR, "Nouveau salon vocal")),

    NEW_VOICE_CHANNEL_SELECT_OPTION_DESCRIPTION(Map.of(
            Lang.EN, "Skoice will automatically set up a voice channel",
            Lang.FR, "Skoice configurera automatiquement un salon vocal.")),

    DEFAULT_CATEGORY_NAME(Map.of(
            Lang.EN, "Proximity Voice Chat",
            Lang.FR, "Chat vocal de proximité")),

    DEFAULT_LOBBY_NAME(Map.of(
            Lang.EN, "Lobby",
            Lang.FR, "Salon vocal principal")),

    MODE_EMBED_TITLE(Map.of(
            Lang.EN, "Mode")),

    MODE_EMBED_DESCRIPTION(Map.of(
            Lang.EN, "Select a mode or customize the distances.",
            Lang.FR, "Sélectionnez un mode ou personnalisez les distances.")),

    MODE_EMBED_ALTERNATIVE_DESCRIPTION(Map.of(
            Lang.EN, "Let us choose the best settings for your personal use of Skoice. You will still be able to customize the distances later.",
            Lang.FR, "Laissez-nous choisir les meilleures réglages pour votre utilisation personnelle de Skoice. Vous pourrez toujours personnaliser les distances plus tard.")),

    VANILLA_MODE_FIELD_TITLE(Map.of(
            Lang.EN, "Vanilla Mode",
            Lang.FR, "Mode standard")),

    VANILLA_MODE_FIELD_DESCRIPTION(Map.of(
            Lang.EN, "Choose this mode if you plan to play open world game modes (longer distances).",
            Lang.FR, "Choisissez ce mode si vous prévoyez de jouer sur des modes de jeu en monde ouvert (distances plus longues).")),

    VANILLA_MODE_FIELD_ALTERNATIVE_DESCRIPTION(Map.of(
            Lang.EN, "Horizontal Radius: 80 blocks — Vertical Radius: 40 blocks",
            Lang.FR, "Rayon horizontal : 80 blocs — Rayon vertical : 40 blocs")),

    MINIGAME_MODE_FIELD_TITLE(Map.of(
            Lang.EN, "Minigame Mode",
            Lang.FR, "Mode mini-jeu")),

    MINIGAME_MODE_FIELD_DESCRIPTION(Map.of(
            Lang.EN, "Choose this mode if you plan to play game modes that only require a limited area (shorter distances).",
            Lang.FR, "Choisissez ce mode si vous prévoyez de jouer sur des modes de jeu qui requièrent seulement une zone limitée (distances plus courtes).")),

    MINIGAME_MODE_FIELD_ALTERNATIVE_DESCRIPTION(Map.of(
            Lang.EN, "Horizontal Radius: 40 blocks — Vertical Radius: 20 blocks",
            Lang.FR, "Rayon horizontal : 40 blocs — Rayon vertical : 20 blocs")),

    MODE_SELECT_OPTION_PLACEHOLDER(Map.of(
            Lang.EN, "Select a Mode",
            Lang.FR, "Sélectionnez un mode")),

    CUSTOMIZE_FIELD_TITLE(Map.of(
            Lang.EN, "Customize",
            Lang.FR, "Personnaliser")),

    CUSTOMIZE_FIELD_DESCRIPTION(Map.of(
            Lang.EN, "Set distances according to your personal preferences.",
            Lang.FR, "Définissez les distances en fonction de vos préférences personnelles.")),

    CUSTOMIZE_SELECT_MENU_DESCRIPTION(Map.of(
            Lang.EN, "Set distances from 1 to 1000 blocks.",
            Lang.FR, "Définissez les distances de 1 à 1000 blocs.")),

    CUSTOMIZE_SELECT_MENU_ALTERNATIVE_DESCRIPTION(Map.of(
            Lang.EN, "Horizontal Radius: {horizontalRadius} blocks — Vertical Radius: {verticalRadius} blocks",
            Lang.FR, "Rayon horizontal : {horizontalRadius} blocs — Rayon vertical : {verticalRadius} blocs")),

    HORIZONTAL_RADIUS_EMBED_TITLE(Map.of(
            Lang.EN, "Horizontal Radius",
            Lang.FR, "Rayon horizontal")),

    HORIZONTAL_RADIUS_EMBED_DESCRIPTION(Map.of(
            Lang.EN, "This is the maximum horizontal distance between players.",
            Lang.FR, "Il s'agit de la distance horizontale maximale entre les joueurs.")),

    VERTICAL_RADIUS_EMBED_TITLE(Map.of(
            Lang.EN, "Vertical Radius",
            Lang.FR, "Rayon vertical")),

    ENTER_A_VALUE_FIELD_TITLE(Map.of(
            Lang.EN, "Enter a Value",
            Lang.FR, "Entrez une valeur")),

    ENTER_A_VALUE_FIELD_DESCRIPTION(Map.of(
            Lang.EN, "This setting is currently set to {value} blocks.\nThe value must be between 1 and 1000.",
            Lang.FR, "Cette option est actuellement définie sur {value} blocs.\nLa valeur doit être comprise entre 1 et 1000.")),

    VERTICAL_RADIUS_EMBED_DESCRIPTION(Map.of(
            Lang.EN, "This is the maximum vertical distance between players.",
            Lang.FR, "Il s'agit de la distance verticale maximale entre les joueurs.")),

    ADVANCED_SETTINGS_EMBED_TITLE(Map.of(
            Lang.EN, "Advanced Settings",
            Lang.FR, "Réglages avancées")),

    ADVANCED_SETTINGS_EMBED_DESCRIPTION(Map.of(
            Lang.EN, "Manage other parameters.",
            Lang.FR, "Gérez d'autres paramètres.")),

    ACTION_BAR_ALERT_EMBED_TITLE(Map.of(
            Lang.EN, "Action Bar Alert",
            Lang.FR, "Alerte dans la barre d'action")),

    ACTION_BAR_ALERT_EMBED_DESCRIPTION(Map.of(
            Lang.EN, "Toggle the alert sent to players who are moving away before they get disconnected from their current voice channel.",
            Lang.FR, "Activez ou désactivez l'alerte envoyée aux joueurs qui s'éloignent avant qu'ils soient déconnectés de leur salon vocal actuel.")),

    CHANNEL_VISIBILITY_EMBED_TITLE(Map.of(
            Lang.EN, "Channel Visibility",
            Lang.FR, "Visibilité des salons")),

    CHANNEL_VISIBILITY_EMBED_DESCRIPTION(Map.of(
            Lang.EN, "Toggle the visibility of the temporary channels created by Skoice.",
            Lang.FR, "Rendez visibles ou invisibles les salons temporaires créés par Skoice.")),

    LANGUAGE_EMBED_TITLE(Map.of(
            Lang.EN, "Language",
            Lang.FR, "Langue")),

    LANGUAGE_EMBED_DESCRIPTION(Map.of(
            Lang.EN, "Choose the language used to display messages.",
            Lang.FR, "Choisissez la langue utilisée pour afficher les messages.")),

    LANGUAGE_SELECT_MENU_PLACEHOLDER(Map.of(
            Lang.EN, "Select a Language",
            Lang.FR, "Sélectionnez une langue")),

    TROUBLESHOOTING_FIELD_TITLE(Map.of(
            Lang.EN, "Troubleshooting",
            Lang.FR, "Assistance")),

    TROUBLESHOOTING_FIELD_DESCRIPTION(Map.of(
            Lang.EN, "Having issues? [Join our Discord server!](https://discord.gg/h3Tgccc)",
            Lang.FR, "Vous rencontrez des problèmes ? [Rejoignez notre serveur Discord !](https://discord.gg/h3Tgccc)")),

    GET_THE_PROXIMITY_VOICE_CHAT_EMBED_TITLE(Map.of(
            Lang.EN, "Get the Proximity Voice Chat",
            Lang.FR, "Obtenir le chat vocal de proximité")),

    DOWNLOAD_SKOICE_FIELD_TITLE(Map.of(
            Lang.EN, "Download Skoice",
            Lang.FR, "Téléchargez Skoice")),

    DOWNLOAD_SKOICE_FIELD_DESRIPTION(Map.of(
            Lang.EN, "Get the plugin [here](https://www.spigotmc.org/resources/skoice-proximity-voice-chat.82861/) and install it on your Minecraft server.",
            Lang.FR, "Obtenez le plugin [ici](https://www.spigotmc.org/resources/skoice-proximity-voice-chat.82861/) et installez-le sur votre serveur Minecraft.")),

    LINKING_PROCESS_EMBED_TITLE(Map.of(
            Lang.EN, "Linking Process",
            Lang.FR, "Procédure de liaison")),

    VERIFICATION_CODE_FIELD_TITLE(Map.of(
            Lang.EN, "Verification Code",
            Lang.FR, "Code de vérification")),

    VERIFICATION_CODE_FIELD_DESCRIPTION(Map.of(
            Lang.EN, "Type `/link {code}` in game to complete the process.",
            Lang.FR, "Tapez `/link {code}` en jeu pour terminer la procédure.")),

    ACCOUNT_LINKED_FIELD_TITLE(Map.of(
            Lang.EN, "Account Linked",
            Lang.FR, "Compte lié")),

    ACCOUNT_LINKED_FIELD_DESCRIPTION(Map.of(
            Lang.EN, "Your Discord account has been linked to Minecraft.",
            Lang.FR, "Votre compte Discord a été lié à Minecraft.")),

    ACCOUNT_UNLINKED_FIELD_TITLE(Map.of(
            Lang.EN, "Account Unlinked",
            Lang.FR, "Compte délié")),

    ACCOUNT_UNLINKED_FIELD_DESCRIPTION(Map.of(
            Lang.EN, "Your Discord account has been unlinked from Minecraft.",
            Lang.FR, "Votre compte Discord a été délié de Minecraft.")),

    ACCOUNT_NOT_LINKED_FIELD_TITLE(Map.of(
            Lang.EN, "Account Not Linked",
            Lang.FR, "Compte non lié")),

    ACCOUNT_NOT_LINKED_FIELD_DESCRIPTION(Map.of(
            Lang.EN, "Your Discord account is not linked to Minecraft.\nType `/link` to link it.",
            Lang.FR, "Votre compte Discord n'est pas lié à Minecraft.\nTapez `/link` pour le lier.")),

    ACCOUNT_NOT_LINKED_FIELD_ALTERNATIVE_DESCRIPTION(Map.of(
            Lang.EN, "Your Discord account is not linked to Minecraft.\nType `/link` on \"{discordServer}\" to link it.",
            Lang.FR, "Votre compte Discord n'est pas lié à Minecraft.\nTapez `/link` sur \"{discordServer}\" pour le lier.")),

    ACCOUNT_NOT_LINKED_FIELD_GENERIC_ALTERNATIVE_DESCRIPTION(Map.of(
            Lang.EN, "Your Discord account is not linked to Minecraft.\nType `/link` on our Discord server to link it.",
            Lang.FR, "Votre compte Discord n'est pas lié à Minecraft.\nTapez `/link` sur notre serveur Discord pour le lier.")),

    ACCOUNT_ALREADY_LINKED_FIELD_TITLE(Map.of(
            Lang.EN, "Account Already Linked",
            Lang.FR, "Compte déjà lié")),

    ACCOUNT_ALREADY_LINKED_FIELD_DESCRIPTION(Map.of(
            Lang.EN, "Your Discord account is already linked to Minecraft.\nType `/unlink` to unlink it.",
            Lang.FR, "Votre compte Discord est déjà lié à Minecraft\nTapez `/unlink` pour le délier.")),

    BACK_BUTTON_LABEL(Map.of(
            Lang.EN, "Back",
            Lang.FR, "Retour")),

    REFRESH_BUTTON_LABEL(Map.of(
            Lang.EN, "Refresh",
            Lang.FR, "Rafraîchir")),

    CLOSE_BUTTON_LABEL(Map.of(
            Lang.EN, "Close",
            Lang.FR, "Fermer")),

    ENABLED_SELECT_OPTION_LABEL(Map.of(
            Lang.EN, "Enabled",
            Lang.FR, "Activé")),

    DISABLED_SELECT_OPTION_LABEL(Map.of(
            Lang.EN, "Disabled",
            Lang.FR, "Désactivé")),

    DEFAULT_SELECT_OPTION_DESCRIPTION(Map.of(
            Lang.EN, "This option is selected by default.",
            Lang.FR, "Cette option est sélectionnée par défaut.")),

    TOO_MANY_OPTIONS_SELECT_OPTION_LABEL(Map.of(
            Lang.EN, "Too Many Options",
            Lang.FR, "Trop d'options")),

    TOO_MANY_OPTIONS_SELECT_OPTION_DESCRIPTION(Map.of(
            Lang.EN, "Skoice is unabled to load more options.",
            Lang.FR, "Skoice est incapable de charger plus d'options.")),

    CONFIGURE_LATER_BUTTON_LABEL(Map.of(
            Lang.EN, "Configure Later",
            Lang.FR, "Configurer plus tard")),

    ERROR_EMBED_TITLE(Map.of(
            Lang.EN, "Error",
            Lang.FR, "Erreur")),

    ACCESS_DENIED_FIELD_TITLE(Map.of(
            Lang.EN, "Access Denied",
            Lang.FR, "Accès refusé")),

    ACCESS_DENIED_FIELD_DESCRIPTION(Map.of(
            Lang.EN, "You do not have the required permission to execute this action.",
            Lang.FR, "Vous n'avez la permission requise pour effectuer cette action.")),

    TOO_MANY_INTERACTIONS_FIELD_TITLE(Map.of(
            Lang.EN, "Too Many Interactions",
            Lang.FR, "Trop d'interactions")),

    TOO_MANY_INTERACTIONS_FIELD_DESCRIPTION(Map.of(
            Lang.EN, "You are executing commands too fast",
            Lang.FR, "Vous exécutez des commandes trop rapidement.")),

    INCOMPLETE_CONFIGURATION_FIELD_TITLE(Map.of(
            Lang.EN, "Incomplete Configuration",
            Lang.FR, "Configuration non terminée")),

    INCOMPLETE_CONFIGURATION_FIELD_DESCRIPTION(Map.of(
            Lang.EN, "Skoice is not configured correctly. Please contact an administrator.",
            Lang.FR, "Skoice n'est pas configuré correctement. Veuillez contacter un administrateur.")),

    INCOMPLETE_CONFIGURATION_SERVER_MANAGER_FIELD_DESCRIPTION(Map.of(
            Lang.EN, "Type `/configure` on your Discord server to complete the configuration and use Skoice.",
            Lang.FR, "Tapez `/configure` sur votre serveur Discord pour terminer la configuration and utiliser Skoice.")),

    INCOMPLETE_CONFIGURATION_SERVER_MANAGER_FIELD_ALTERNATIVE_DESCRIPTION(Map.of(
            Lang.EN, "You have either moved or deleted the lobby.\nType `/configure` on your Discord server to complete the configuration and use Skoice.",
            Lang.FR, "Vous avez soit déplacé, soit supprimé le salon vocal principal. Tapez `/configure` sur votre serveur Discord pour terminer la configuration and utiliser Skoice.")),

    ILLEGAL_INTERACTION_FIELD_TITLE(Map.of(
            Lang.EN, "Illegal Interaction",
            Lang.FR, "Interaction interdite")),

    ILLEGAL_INTERACTION_FIELD_DESCRIPTION(Map.of(
            Lang.EN, "You can only interact with the bot on a Discord server.",
            Lang.FR, "Vous pouvez seulement interagir avec le bot sur un serveur Discord.")),

    COMMUNICATION_LOST(Map.of(
            Lang.EN, "Communication lost.",
            Lang.FR, "Communication perdue."));

    private final Map<Lang, String> messages;

    Discord(Map<Lang, String> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        String lang = getPlugin().getConfig().getString("lang");
        return messages.getOrDefault(lang == null ? Lang.EN : Lang.valueOf(lang), messages.get(Lang.EN));
    }
}
