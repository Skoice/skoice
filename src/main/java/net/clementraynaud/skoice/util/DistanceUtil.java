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

package net.clementraynaud.skoice.util;

import org.bukkit.Location;
import org.bukkit.util.NumberConversions;

public class DistanceUtil {

    private DistanceUtil() {
    }

    public static double getHorizontalDistance(Location location1, Location location2) {
        return Math.sqrt(NumberConversions.square(location1.getX() - location2.getX()) + NumberConversions.square(location1.getZ() - location2.getZ()));
    }

    public static double getVerticalDistance(Location location1, Location location2) {
        return Math.abs(location1.getY() - location2.getY());
    }
}
