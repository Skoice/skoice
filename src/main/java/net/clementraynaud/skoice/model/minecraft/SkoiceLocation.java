package net.clementraynaud.skoice.model.minecraft;

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
