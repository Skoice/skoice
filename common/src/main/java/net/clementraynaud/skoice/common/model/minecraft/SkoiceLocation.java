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

package net.clementraynaud.skoice.common.model.minecraft;

public class SkoiceLocation {

    private final double x;
    private final double y;
    private final double z;

    public SkoiceLocation(double x, double y, double z) {
        this.x = this.round(x);
        this.y = this.round(y);
        this.z = this.round(z);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        SkoiceLocation that = (SkoiceLocation) o;
        return Double.compare(this.x, that.x) == 0 && Double.compare(this.y, that.y) == 0 && Double.compare(this.z, that.z) == 0;
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(this.x);
        result = 31 * result + Double.hashCode(this.y);
        result = 31 * result + Double.hashCode(this.z);
        return result;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    @Override
    public String toString() {
        return "SkoiceLocation{" +
                "x=" + this.x +
                ", y=" + this.y +
                ", z=" + this.z +
                '}';
    }
}
