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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworksTest {

    @Mock
    private Network network1;

    @Mock
    private Network network2;

    @BeforeEach
    void setUp() {
        Networks.clear();
    }

    @Test
    void testMergeNetworks() {
        when(this.network1.size()).thenReturn(5);
        when(this.network2.size()).thenReturn(3);

        Networks.merge(this.network1, this.network2);
        verify(this.network1).engulf(this.network2);

        reset(this.network1, this.network2);

        when(this.network1.size()).thenReturn(2);
        when(this.network2.size()).thenReturn(4);

        Networks.merge(this.network1, this.network2);
        verify(this.network2).engulf(this.network1);
    }
}