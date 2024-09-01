/*
 * Copyright 2020, 2021, 2022, 2023, 2024 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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
import net.clementraynaud.skoice.bot.BotStatus;
import net.clementraynaud.skoice.lang.LangInfo;
import net.clementraynaud.skoice.storage.config.ConfigField;
import org.bukkit.command.CommandSender;

public class LanguageArgument extends Argument {

    private final String arg;

    public LanguageArgument(Skoice plugin, CommandSender sender, String arg) {
        super(plugin, sender, ArgumentInfo.LANGUAGE.isAllowedInConsole(), ArgumentInfo.LANGUAGE.isPermissionRequired(), ArgumentInfo.LANGUAGE.isHidden());
        this.arg = arg;
    }

    @Override
    public void run() {
        if (this.arg.isEmpty()) {
            this.sender.sendMessage(super.plugin.getLang().getMessage("chat.configuration.no-language",
                    LangInfo.getJoinedList()));
            return;
        }

        try {
            LangInfo language = LangInfo.valueOf(this.arg.toUpperCase());

            if (language.toString().equals(super.plugin.getConfigYamlFile().getString(ConfigField.LANG.toString()))) {
                this.sender.sendMessage(super.plugin.getLang().getMessage("chat.configuration.language-already-set",
                        language.getFullName()));
                return;
            }

            super.plugin.getConfigYamlFile().set(ConfigField.LANG.toString(), language.toString());
            super.plugin.getLang().load(language);
            super.plugin.getBot().getLang().load(language);
            super.plugin.getListenerManager().update();

            if (this.plugin.getBot().getStatus() != BotStatus.NOT_CONNECTED) {
                this.plugin.getBot().getCommands().register();
            }
            
            this.sender.sendMessage(super.plugin.getLang().getMessage("chat.configuration.language-updated",
                    language.getFullName()));

        } catch (IllegalArgumentException e) {
            this.sender.sendMessage(super.plugin.getLang().getMessage("chat.configuration.invalid-language",
                    LangInfo.getJoinedList()));
        }
    }

}
