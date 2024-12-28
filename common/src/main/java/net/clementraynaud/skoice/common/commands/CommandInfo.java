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

package net.clementraynaud.skoice.common.commands;

public enum CommandInfo {

    CONFIGURE(true, false),
    LINK(false, true),
    UNLINK(false, true),
    INVITE(false, false);

    private final boolean serverManagerRequired;
    private final boolean botReadyRequired;

    CommandInfo(boolean serverManagerRequired, boolean botReadyRequired) {
        this.serverManagerRequired = serverManagerRequired;
        this.botReadyRequired = botReadyRequired;
    }

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

    public boolean isServerManagerRequired() {
        return this.serverManagerRequired;
    }

    public boolean isBotReadyRequired() {
        return this.botReadyRequired;
    }
}
