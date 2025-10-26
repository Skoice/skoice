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

import net.clementraynaud.skoice.common.api.events.SkoiceEventLink;

/**
 * Called when a Minecraft account is linked to a Discord account through the Skoice in-game linking process.
 * <p>
 * This event is fired after a successful link is established between a Minecraft
 * player and a Discord user through the in-game command. It is not fired when using
 * {@link net.clementraynaud.skoice.common.SkoiceAPI#linkUser}.
 *
 * <p><b>Example usage:</b>
 * <pre>{@code
 * eventBus.subscribe(AccountLinkEvent.class, event -> {
 *     UUID minecraftId = event.getMinecraftId();
 *     String discordId = event.getDiscordId();
 *     // Handle the account linking
 * });
 * }</pre>
 *
 * @see AccountUnlinkEvent
 */
public class AccountLinkEvent extends SkoiceEventLink {

    public AccountLinkEvent(String minecraftId, String discordId) {
        super(minecraftId, discordId);
    }
}