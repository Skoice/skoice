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

package net.clementraynaud.skoice.menus;

import net.clementraynaud.skoice.bot.Bot;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

public class ConfigurationMenu extends EmbeddedMenu {

    public ConfigurationMenu(Bot bot) {
        super(bot);
    }

    public void generate(IReplyCallback interaction) {
        this.deleteFromHook();
        this.refreshId().reply(interaction);
    }

    public ConfigurationMenu refreshId() {
        switch (super.bot.getStatus()) {
            case MULTIPLE_GUILDS:
                super.setContent("server");
                break;
            case MISSING_PERMISSION:
                super.setContent("permissions");
                break;
            case NO_VOICE_CHANNEL:
                super.setContent("voice-channel");
                break;
            case NO_RADIUS:
                super.setContent("range");
                break;
            default:
                super.setContent("settings");
        }
        return this;
    }
}
