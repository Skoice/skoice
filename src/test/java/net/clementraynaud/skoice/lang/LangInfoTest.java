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

package net.clementraynaud.skoice.lang;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LangInfoTest {

    @Test
    public void testGetList() {
        Set<String> langList = LangInfo.getList();
        assertNotNull(langList);
        assertTrue(langList.contains("en"));
        assertTrue(langList.contains("fr"));
        assertTrue(langList.contains("de"));
    }

    @Test
    public void testGetJoinedList() {
        String joinedList = LangInfo.getJoinedList();
        assertNotNull(joinedList);
        assertTrue(joinedList.startsWith("<"));
        assertTrue(joinedList.endsWith(">"));
        assertTrue(joinedList.contains("en"));
        assertTrue(joinedList.contains("fr"));
        assertTrue(joinedList.contains("de"));
    }
}