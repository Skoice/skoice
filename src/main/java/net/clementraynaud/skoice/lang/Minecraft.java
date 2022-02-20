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

import org.bukkit.ChatColor;

import java.util.Map;

import static net.clementraynaud.skoice.Skoice.getPlugin;
import static net.clementraynaud.skoice.lang.Lang.PREFIX;

public enum Minecraft {
    ILLEGAL_EXECUTOR(Map.of(
            Lang.EN, PREFIX + "This command is " + ChatColor.RED + "only executable " + ChatColor.GRAY + "by players.",
            Lang.FR, PREFIX + "Cette commande est " + ChatColor.RED + "seulement exécutable " + ChatColor.GRAY + "par les joueurs.")),

    MISSING_PERMISSION(Map.of(
            Lang.EN, PREFIX + "This command is " + ChatColor.RED + "only executable " + ChatColor.GRAY + "by operators.",
            Lang.FR, PREFIX + "Cette commande est " + ChatColor.RED + "seulement exécutable " + ChatColor.GRAY + "par les opérateurs.")),

    INCOMPLETE_CONFIGURATION(Map.of(
            Lang.EN, PREFIX + "Skoice is " + ChatColor.RED + "not configured correctly" + ChatColor.GRAY + ". Please contact an administrator.",
            Lang.FR, PREFIX + "Skoice n'est " + ChatColor.RED + "pas configuré correctement" + ChatColor.GRAY + ". Veuillez contacter un administrateur.")),

    INCOMPLETE_CONFIGURATION_OPERATOR_INTERACTIVE(Map.of(
            Lang.EN, "?")),

    INCOMPLETE_CONFIGURATION_OPERATOR_COMMAND(Map.of(
            Lang.EN, PREFIX + "Skoice is " + ChatColor.RED + "not configured correctly" + ChatColor.GRAY + ". Type \"" + ChatColor.YELLOW + "/skoice configure" + ChatColor.GRAY + "\" to set it up.",
            Lang.FR, PREFIX + "Skoice n'est " + ChatColor.RED + "pas configuré correctement" + ChatColor.GRAY + ". Tapez \"" + ChatColor.YELLOW + "/skoice configure" + ChatColor.GRAY + "\" pour le configurer.")),

    INCOMPLETE_CONFIGURATION_OPERATOR_DISCORD(Map.of(
            Lang.EN, PREFIX + "Skoice is " + ChatColor.RED + "not configured correctly" + ChatColor.GRAY + ". Type \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" on your Discord server to set it up.",
            Lang.FR, PREFIX + "Skoice n'est " + ChatColor.RED + "pas configuré correctement" + ChatColor.GRAY + ". Tapez \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" sur votre serveur Discord pour le configurer.")),

    BOT_CREATION_INTERACTIVE(Map.of(
            Lang.EN, "?")),

    BOT_CREATION_LINK(Map.of(
            Lang.EN, " \n" + PREFIX + "Configuration (" + ChatColor.WHITE + "Bot Creation" + ChatColor.GRAY + ")\n \n" + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "First, you need to create a bot and invite it to your Discord server. Please follow the instructions on this page: " + ChatColor.AQUA + "https://github.com/carlodrift/skoice/wiki" + ChatColor.GRAY + ".\n" + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "When you have retrieved its token, type \"" + ChatColor.YELLOW + "/skoice token" + ChatColor.GRAY + "\" followed by the token.\n" + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "Once done, type \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" on your Discord server to go to the next step.\n ",
            Lang.FR, " \n" + PREFIX + "Configuration (" + ChatColor.WHITE + "Création du bot" + ChatColor.GRAY + ")\n \n" + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "Tout d'abord, vous devez créer un bot et l'inviter sur votre serveur Discord. Merci de suivre les instructions sur cette page: " + ChatColor.AQUA + "https://github.com/carlodrift/skoice/wiki" + ChatColor.GRAY + ".\n" + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "Une fois son token récupéré, tapez \"" + ChatColor.YELLOW + "/skoice token" + ChatColor.GRAY + "\" suivi du token.\n" + ChatColor.DARK_GRAY + "• " + ChatColor.GRAY + "Une fois fait, tapez \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" sur votre serveur Discord pour passer à l'étape suivante.\n ")),

    ALREADY_CONFIGURED(Map.of(
            Lang.EN, PREFIX + "Skoice is " + ChatColor.RED + "already configured" + ChatColor.GRAY + ". Type \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" on your Discord server to update its settings.",
            Lang.FR, PREFIX + "Skoice est " + ChatColor.RED + "déjà configuré" + ChatColor.GRAY + ". Tapez \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" sur votre serveur Discord pour mettre à jour ses réglages.")),

    NO_TOKEN(Map.of(
            Lang.EN, PREFIX + "You have " + ChatColor.RED + "not provided a token" + ChatColor.GRAY + ".",
            Lang.FR, PREFIX + "Vous n'avez " + ChatColor.RED + "pas fourni de token" + ChatColor.GRAY + ".")),

    INVALID_TOKEN(Map.of(
            Lang.EN, PREFIX + "You have " + ChatColor.RED + "not provided a valid token" + ChatColor.GRAY + ".",
            Lang.FR, PREFIX + "Vous n'avez " + ChatColor.RED + "pas fourni de token valide" + ChatColor.GRAY + ".")),

    BOT_CONNECTED(Map.of(
            Lang.EN, PREFIX + "Your bot is " + ChatColor.GREEN + "now connected" + ChatColor.GRAY + ".",
            Lang.FR, PREFIX + "Votre bot est " + ChatColor.GREEN + "désormais connecté" + ChatColor.GRAY + ".")),

    BOT_CONNECTED_INCOMPLETE_CONFIGURATION_DISCORD(Map.of(
            Lang.EN, PREFIX + "Your bot is " + ChatColor.GREEN + "now connected" + ChatColor.GRAY + ". Type \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" on your Discord server to set it up.",
            Lang.FR, PREFIX + "Votre bot est " + ChatColor.GREEN + "désormais connecté" + ChatColor.GRAY + ". Tapez \"" + ChatColor.YELLOW + "/configure" + ChatColor.GRAY + "\" sur votre serveur Discord pour le configurer.")),

    BOT_ALREADY_CONNECTED(Map.of(
            Lang.EN, PREFIX + "A bot is " + ChatColor.RED + "already connected" + ChatColor.GRAY + ". Restart your Minecraft server to apply the new token.",
            Lang.FR, PREFIX + "Un bot is " + ChatColor.RED + "déjà connecté" + ChatColor.GRAY + ". Redémarrez votre serveur Minecraft pour appliquer le nouveau token.")),

    BOT_COULD_NOT_CONNECT(Map.of(
            Lang.EN, PREFIX + "Your bot " + ChatColor.RED + "could not connect" + ChatColor.GRAY + ". Try again with a valid token.",
            Lang.FR, PREFIX + "Votre bot " + ChatColor.RED + "n'a pas pu se connecter" + ChatColor.GRAY + ". Essayez de nouveau avec un token valide.")),

    ACCOUNT_LINKED(Map.of(
            Lang.EN, PREFIX + "You have " + ChatColor.GREEN + "linked your Minecraft account " + ChatColor.GRAY + "to Discord.",
            Lang.FR, PREFIX + "Vous avez " + ChatColor.GREEN + "lié votre compte Minecraft " + ChatColor.GRAY + "à Discord.")),

    ACCOUNT_UNLINKED(Map.of(
            Lang.EN, PREFIX + "You have " + ChatColor.RED + "unlinked your Minecraft account " + ChatColor.GRAY + "from Discord.",
            Lang.FR, PREFIX + "Vous avez " + ChatColor.RED + "delié votre compte Minecraft " + ChatColor.GRAY + "de Discord.")),

    ACCOUNT_NOT_LINKED(Map.of(
            Lang.EN, PREFIX + "You have " + ChatColor.RED + "not linked your Minecraft account " + ChatColor.GRAY + "to Discord. Type \"" + ChatColor.YELLOW + "/link" + ChatColor.GRAY + "\" on our Discord server to link it.",
            Lang.FR, PREFIX + "Vous avez " + ChatColor.RED + "pas lié votre compte Minecraft " + ChatColor.GRAY + "à Discord. Tapez \"" + ChatColor.YELLOW + "/link" + ChatColor.GRAY + "\" sur notre serveur Discord pour le lier.")),

    ACCOUNT_ALREADY_LINKED(Map.of(
            Lang.EN, PREFIX + "You have " + ChatColor.RED + "already linked your Minecraft account " + ChatColor.GRAY + "to Discord. Type \"" + ChatColor.YELLOW + "/skoice unlink" + ChatColor.GRAY + "\" to unlink it.",
            Lang.FR, PREFIX + "Vous avez " + ChatColor.RED + "déjà lié votre compte Minecraft " + ChatColor.GRAY + "à Discord. Tapez \"" + ChatColor.YELLOW + "/skoice unlink" + ChatColor.GRAY + "\" pour le délier.")),

    NO_CODE(Map.of(
            Lang.EN, PREFIX + "You have " + ChatColor.RED + "not provided a code" + ChatColor.GRAY + ". Type \"" + ChatColor.YELLOW + "/link" + ChatColor.GRAY + "\" on our Discord server to receive one.",
            Lang.FR, PREFIX + "Vous n'avez " + ChatColor.RED + "pas fourni de code" + ChatColor.GRAY + ". Tapez \"" + ChatColor.YELLOW + "/link" + ChatColor.GRAY + "\" sur notre serveur Discord pour en recevoir un.")),

    INVALID_CODE(Map.of(
            Lang.EN, PREFIX + "You have " + ChatColor.RED + "not provided a valid code" + ChatColor.GRAY + ". Type \"" + ChatColor.YELLOW + "/link" + ChatColor.GRAY + "\" on our Discord server to receive one.",
            Lang.FR, PREFIX + "Vous n'avez " + ChatColor.RED + "pas fourni de code valide" + ChatColor.GRAY + ". Tapez \"" + ChatColor.YELLOW + "/link" + ChatColor.GRAY + "\" sur notre serveur Discord pour en recevoir un.")),

    CONNECTED_TO_PROXIMITY_VOICE_CHAT(Map.of(
            Lang.EN, PREFIX + "You are " + ChatColor.GREEN + "now connected " + ChatColor.GRAY + "to the proximity voice chat.",
            Lang.FR, PREFIX + "Vous êtes " + ChatColor.GREEN + "désormais connecté " + ChatColor.GRAY + "au chat vocal de proximité.")),

    DISCONNECTED_FROM_PROXIMITY_VOICE_CHAT(Map.of(
            Lang.EN, PREFIX + "You are " + ChatColor.RED + "now disconnected " + ChatColor.GRAY + "from the proximity voice chat.",
            Lang.FR, PREFIX + "Vous êtes " + ChatColor.RED + "désormais déconnecté " + ChatColor.GRAY + "du chat vocal de proximité.")),

    ACTION_BAR_ALERT(Map.of(
            Lang.EN, ChatColor.RED + "⚠ " + ChatColor.GRAY + "You are " + ChatColor.RED + "moving away " + ChatColor.GRAY + "from near players.",
            Lang.FR, ChatColor.RED + "⚠ " + ChatColor.GRAY + "Vous vous " + ChatColor.RED + "éloignez " + ChatColor.GRAY + "des joueurs à proximité."));

    private final Map<Lang, String> messages;

    Minecraft(Map<Lang, String> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        String lang = getPlugin().getConfig().getString("lang");
        return messages.getOrDefault(lang == null ? Lang.EN : Lang.valueOf(lang), messages.get(Lang.EN));
    }
}
