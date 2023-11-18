/*
 * Copyright 2020, 2021, 2022, 2023 Clément "carlodrift" Raynaud, Lucas "Lucas_Cdry" Cadiry and contributors
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

import org.bstats.charts.DrilldownPie;

import java.util.HashMap;
import java.util.Map;

public final class ChartUtil {

    private ChartUtil() {
    }

    public static DrilldownPie createDrilldownPie(String chartId, int value, int min, int interval, int slices) {
        return new DrilldownPie(chartId, () -> {
            Map<String, Map<String, Integer>> map = new HashMap<>();
            Map<String, Integer> entry = new HashMap<>();
            entry.put(Integer.toString(value), 1);
            int minValue = min;
            int maxValue = min + interval - 1;
            for (int i = 0; i < slices - 1; i++) {
                if (value <= maxValue) {
                    map.put(minValue + "-" + maxValue, entry);
                    break;
                }
                minValue += interval;
                maxValue += interval;
                if (i == slices - 2) {
                    map.put(minValue + "+", entry);
                }
            }
            return map;
        });
    }
}
