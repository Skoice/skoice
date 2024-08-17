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

package net.clementraynaud.skoice.system;

import net.clementraynaud.skoice.Skoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkTest {

    @Mock
    private Skoice plugin;

    private Network network;

    @BeforeEach
    void setUp() {
        this.network = new Network(this.plugin, "channelId");
    }

    @Test
    void canPlayerConnect_checksPlayerEligibility() {
        LinkedPlayer player = mock(LinkedPlayer.class);
        LinkedPlayer anotherPlayer = mock(LinkedPlayer.class);

        when(player.isStateEligible()).thenReturn(true);
        when(anotherPlayer.isStateEligible()).thenReturn(true);
        when(anotherPlayer.isCloseEnoughToPlayer(player, false)).thenReturn(true);

        this.network.add(anotherPlayer);

        assertTrue(this.network.canPlayerConnect(player));

        when(anotherPlayer.isCloseEnoughToPlayer(player, false)).thenReturn(false);
        assertFalse(this.network.canPlayerConnect(player));
    }

    @Test
    void canPlayerStayConnected_checksPlayerEligibility() {
        LinkedPlayer player = mock(LinkedPlayer.class);
        LinkedPlayer anotherPlayer = mock(LinkedPlayer.class);

        when(player.isStateEligible()).thenReturn(true);
        when(anotherPlayer.isStateEligible()).thenReturn(true);
        when(anotherPlayer.isCloseEnoughToPlayer(player, true)).thenReturn(true);

        this.network.add(anotherPlayer);

        assertTrue(this.network.canPlayerStayConnected(player));

        when(anotherPlayer.isCloseEnoughToPlayer(player, true)).thenReturn(false);
        assertFalse(this.network.canPlayerStayConnected(player));
    }
}