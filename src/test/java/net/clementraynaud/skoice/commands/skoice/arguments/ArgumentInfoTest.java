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

package net.clementraynaud.skoice.commands.skoice.arguments;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArgumentInfoTest {

    @Test
    void testGetJoinedConsoleAllowedList() {
        String joinedConsoleAllowedList = ArgumentInfo.getJoinedConsoleAllowedList();
        assertNotNull(joinedConsoleAllowedList);
        assertTrue(joinedConsoleAllowedList.startsWith("<"));
        assertTrue(joinedConsoleAllowedList.endsWith(">"));
        assertTrue(joinedConsoleAllowedList.contains("token"));
        assertTrue(joinedConsoleAllowedList.contains("language"));
    }

    @Test
    void testGet() {
        assertEquals(ArgumentInfo.CONFIGURE, ArgumentInfo.get("configure"));
        assertEquals(ArgumentInfo.TOKEN, ArgumentInfo.get("token"));
        assertNull(ArgumentInfo.get("nonexistent"));
    }

    @Test
    void testGetList() {
        Set<String> listWithPermission = ArgumentInfo.getList(true);
        assertNotNull(listWithPermission);
        assertTrue(listWithPermission.contains("configure"));
        assertTrue(listWithPermission.contains("tooltips"));
        assertTrue(listWithPermission.contains("token"));

        Set<String> listWithoutPermission = ArgumentInfo.getList(false);
        assertNotNull(listWithoutPermission);
        assertFalse(listWithoutPermission.contains("token"));
        assertTrue(listWithoutPermission.contains("link"));
    }
}