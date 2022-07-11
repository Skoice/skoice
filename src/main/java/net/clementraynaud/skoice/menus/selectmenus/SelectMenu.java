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

package net.clementraynaud.skoice.menus.selectmenus;

import net.clementraynaud.skoice.lang.Lang;

public abstract class SelectMenu {

    protected final Lang lang;

    private final boolean isRefreshable;

    protected SelectMenu(Lang lang, boolean isRefreshable) {
        this.lang = lang;
        this.isRefreshable = isRefreshable;
    }

    public boolean isRefreshable() {
        return this.isRefreshable;
    }

    public abstract net.dv8tion.jda.api.interactions.components.selections.SelectMenu get();
}
