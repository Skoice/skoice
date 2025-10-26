/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.common.api.events.account;

import net.clementraynaud.skoice.common.api.events.SkoiceEventMinecraft;

/**
 * Called when a Minecraft account is unlinked from its Discord account through in-game or Discord commands.
 * <p>
 * This event is fired after a link between a Minecraft player and Discord user
 * has been removed through the in-game or Discord commands. It is not fired when using
 * {@link net.clementraynaud.skoice.common.SkoiceAPI#unlinkUser}.
 *
 * <p><b>Example usage:</b>
 * <pre>{@code
 * eventBus.subscribe(AccountUnlinkEvent.class, event -> {
 *     UUID minecraftId = event.getMinecraftId();
 *     // Handle the account unlinking
 * });
 * }</pre>
 *
 * @see AccountLinkEvent
 */
public class AccountUnlinkEvent extends SkoiceEventMinecraft {

    public AccountUnlinkEvent(String minecraftId) {
        super(minecraftId);
    }
}