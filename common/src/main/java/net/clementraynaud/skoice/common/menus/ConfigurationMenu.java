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

package net.clementraynaud.skoice.common.menus;

import net.clementraynaud.skoice.common.bot.Bot;

public class ConfigurationMenu extends EmbeddedMenu {

    public ConfigurationMenu(Bot bot) {
        super(bot);
        ConfigurationMenus.getMenuSet().add(this);
        this.refreshId();
    }

    public ConfigurationMenu(Bot bot, String messageId) {
        super(bot, messageId);
        ConfigurationMenus.getMenuSet().add(this);
    }

    public ConfigurationMenu refreshId() {
        super.setContent(super.bot.getStatus().getMenuId());
        return this;
    }

    @Override
    protected void forget() {
        ConfigurationMenus.getMenuSet().remove(this);
    }
}
