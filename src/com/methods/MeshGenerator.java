package com.methods;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

public final class MeshGenerator {
    private MeshGenerator() {}

    public static List<Point> uniform(double a, double b, int N) {
        if (N < 1) throw new IllegalArgumentException("N >= 1");
        List<Point> pts = new ArrayList<>(N + 1);
        double h = (b - a) / N;
        for (int i = 0; i <= N; i++) pts.add(new Point(a + i * h, 0.0, 0.0));
        return pts;
    }

    // адаптивная сетка (всегда уплотнение слева)
    public static List<Point> graded(double a, double b, int N, double r) {
        if (N < 1) throw new IllegalArgumentException("N >= 1");
        if (r < 1.0) throw new IllegalArgumentException("r >= 1");
        if (Math.abs(r - 1.0) < 1e-14) return uniform(a, b, N);

        double L = b - a;
        double q  = Math.pow(r, 1.0 / (N - 1));
        double h0 = L * (q - 1.0) / (Math.pow(q, N) - 1.0);

        List<Point> pts = new ArrayList<>(N + 1);
        double x = a;
        pts.add(new Point(x, 0.0, 0.0));
        for (int k = 0; k < N; k++) {
            x += h0 * Math.pow(q, k);
            pts.add(new Point(x, 0.0, 0.0));
        }
        return pts;
    }

    public static List<Double> sample(List<Point> pts, java.util.function.DoubleUnaryOperator f) {
        List<Double> vals = new ArrayList<>(pts.size());
        for (Point p : pts) vals.add(f.applyAsDouble(p.x()));
        return vals;
    }
}
