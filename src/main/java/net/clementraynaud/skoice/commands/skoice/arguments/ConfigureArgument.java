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

package net.clementraynaud.skoice.commands.skoice.arguments;

import net.clementraynaud.skoice.Skoice;
import net.clementraynaud.skoice.config.ConfigField;
import net.clementraynaud.skoice.util.MessageUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfigureArgument extends Argument {

    public ConfigureArgument(Skoice plugin, CommandSender sender) {
        super(plugin, sender, ArgumentInfo.CONFIGURE.isAllowedInConsole(), ArgumentInfo.CONFIGURE.isRestrictedToOperators());
    }

    @Override
    public void run() {
        if (!this.canExecuteCommand()) {
            return;
        }
        Player player = (Player) this.sender;
        if (super.plugin.readConfig().getFile().contains(ConfigField.TOKEN.get()) && super.plugin.getBot().getJda() != null) {
            if (super.plugin.getBot().isReady()) {
                player.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.configuration.already-configured"));
            } else {
                player.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.configuration.incomplete-configuration-operator-discord"));
            }
        } else {
            try {
                TextComponent tutorialPage = new TextComponent(this.plugin.getLang().getMessage("minecraft.interaction.this-page"));
                MessageUtil.setHoverEvent(tutorialPage, this.plugin.getLang().getMessage("minecraft.interaction.link", "https://github.com/carlodrift/skoice/wiki"));
                tutorialPage.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/carlodrift/skoice/wiki"));
                TextComponent tokenCommand = new TextComponent(this.plugin.getLang().getMessage("minecraft.interaction.here"));
                MessageUtil.setHoverEvent(tokenCommand, this.plugin.getLang().getMessage("minecraft.interaction.shortcut", "/skoice token"));
                tokenCommand.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/skoice token "));
                player.spigot().sendMessage(this.plugin.getLang().getMessage("minecraft.chat.configuration.bot-creation-interactive", tutorialPage, tokenCommand));
            } catch (NoSuchMethodError e) {
                player.sendMessage(super.plugin.getLang().getMessage("minecraft.chat.configuration.bot-creation-link"));
            }
        }
    }
}
