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

package net.clementraynaud.skoice.common.system;

import net.clementraynaud.skoice.common.model.minecraft.SkoiceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class SpatialIndex {

    private static final int FALLOFF = 3;

    private final int bucketSize;
    private final boolean teamCommunication;
    private final Map<BucketKey, List<LinkedPlayer>> buckets = new HashMap<>();
    private final Map<String, List<LinkedPlayer>> byTeam = new HashMap<>();

    public SpatialIndex(int horizontalRadius, int verticalRadius, boolean teamCommunication) {
        this.bucketSize = Math.max(1, Math.max(horizontalRadius, verticalRadius) + SpatialIndex.FALLOFF);
        this.teamCommunication = teamCommunication;
    }

    public void add(LinkedPlayer player) {
        SkoiceLocation location = player.getFullPlayer().getLocation();
        if (location != null) {
            BucketKey key = new BucketKey(player.getFullPlayer().getWorld(),
                    Math.floorDiv((int) location.getX(), this.bucketSize),
                    Math.floorDiv((int) location.getY(), this.bucketSize),
                    Math.floorDiv((int) location.getZ(), this.bucketSize));
            this.buckets.computeIfAbsent(key, k -> new ArrayList<>()).add(player);
        }

        if (this.teamCommunication) {
            String team = player.getFullPlayer().getTeam();
            if (team != null) {
                this.byTeam.computeIfAbsent(team, k -> new ArrayList<>()).add(player);
            }
        }
    }

    public Set<LinkedPlayer> candidatesFor(LinkedPlayer player) {
        Set<LinkedPlayer> candidates = new LinkedHashSet<>();
        SkoiceLocation location = player.getFullPlayer().getLocation();
        if (location != null) {
            String world = player.getFullPlayer().getWorld();
            int cx = Math.floorDiv((int) location.getX(), this.bucketSize);
            int cy = Math.floorDiv((int) location.getY(), this.bucketSize);
            int cz = Math.floorDiv((int) location.getZ(), this.bucketSize);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        List<LinkedPlayer> bucket = this.buckets.get(new BucketKey(world, cx + dx, cy + dy, cz + dz));
                        if (bucket != null) {
                            candidates.addAll(bucket);
                        }
                    }
                }
            }
        }
        if (this.teamCommunication) {
            String team = player.getFullPlayer().getTeam();
            if (team != null) {
                List<LinkedPlayer> mates = this.byTeam.get(team);
                if (mates != null) {
                    candidates.addAll(mates);
                }
            }
        }
        return candidates;
    }

    private static final class BucketKey {
        private final String world;
        private final int x;
        private final int y;
        private final int z;

        BucketKey(String world, int x, int y, int z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof BucketKey)) {
                return false;
            }
            BucketKey other = (BucketKey) o;
            return this.x == other.x && this.y == other.y && this.z == other.z
                    && Objects.equals(this.world, other.world);
        }

        @Override
        public int hashCode() {
            int h = Objects.hashCode(this.world);
            h = 31 * h + this.x;
            h = 31 * h + this.y;
            h = 31 * h + this.z;
            return h;
        }
    }
}
