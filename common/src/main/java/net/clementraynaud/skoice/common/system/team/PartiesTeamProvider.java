/*
 * Copyright 2020, 2021, 2022, 2023, 2024, 2025, 2026 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

package net.clementraynaud.skoice.common.system.team;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import net.clementraynaud.skoice.common.model.minecraft.FullPlayer;

import java.util.UUID;

/**
 * Team provider backed by AlessioDP's <a href="https://alessiodp.com/docs/parties">Parties</a>
 * plugin. A player's team is their party id, which is stable and global across
 * a Velocity network, so party members on different backend servers stay grouped.
 * <p>
 * The Parties API is shared by the Bukkit and Velocity builds, so this provider
 * works on both platforms. Every API call is wrapped so that a missing,
 * disabled or misbehaving Parties installation can never affect Skoice.
 */
public class PartiesTeamProvider implements TeamProvider {

    public static final String ID = "parties";

    @Override
    public String getId() {
        return PartiesTeamProvider.ID;
    }

    @Override
    public boolean isAvailable() {
        try {
            Parties.getApi();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public String getTeam(FullPlayer player) {
        try {
            PartyPlayer partyPlayer = Parties.getApi().getPartyPlayer(player.getUniqueId());
            if (partyPlayer == null) {
                return null;
            }
            UUID partyId = partyPlayer.getPartyId();
            return partyId == null ? null : partyId.toString();
        } catch (Throwable ignored) {
            return null;
        }
    }
}
