// Point.java
package com.methods;

public final class Point {
    // координаты
    private final double X;
    private final double Y;
    private final double Z;

    public Point(double x, double y, double z) {
        this.X = x;
        this.Y = y;
        this.Z = z;
    }

    public Point(double x, double y) {
        this(x, y, 0.0);
    }

    public Point() {
        this(0.0, 0.0, 0.0);
    }

    // доступ к полям
    public double x() { return X; }
    public double y() { return Y; }
    public double z() { return Z; }

    @Override
    public String toString() {
        return "Point(" + X + ", " + Y + ", " + Z + ")";
    }
}
