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

package net.clementraynaud.skoice.storage.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigFieldTest {

    @Test
    public void testToCamelCase() {
        assertEquals("token", ConfigField.TOKEN.toCamelCase());
        assertEquals("horizontalRadius", ConfigField.HORIZONTAL_RADIUS.toCamelCase());
        assertEquals("channelVisibility", ConfigField.CHANNEL_VISIBILITY.toCamelCase());
    }

    @Test
    public void testToLowerCase() {
        assertEquals("token", ConfigField.TOKEN.toString());
        assertEquals("lang", ConfigField.LANG.toString());
        assertEquals("horizontal-radius", ConfigField.HORIZONTAL_RADIUS.toString());
    }
}