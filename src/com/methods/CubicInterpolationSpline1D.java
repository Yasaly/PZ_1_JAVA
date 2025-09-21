package com.methods;

import java.util.ArrayList;
import java.util.List;

public final class CubicInterpolationSpline1D implements Spline {
    // точки сетки
    private final List<Point> points = new ArrayList<>();
    // коэффициенты сплайна на каждом сегменте
    private double[] a;
    private double[] b;
    private double[] c;
    private double[] d;

    @Override
    public void updateSpline(final List<Point> Points, final List<Double> F_Value) {
        // обновление списка точек сплайна
        points.clear();
        points.addAll(Points);

        if (Points.size() < 2) {
            throw new IllegalArgumentException("Нужно минимум две точки.");
        }
        if (F_Value.size() != Points.size()) {
            throw new IllegalArgumentException("Размер F_Value должен совпадать с числом точек.");
        }

        // число отрезков разбиения
        final int Num_Segment = Points.size() - 1;

        // длина текущего и следующего отрезков
        double h_current, h_next;

        a = new double[Num_Segment];
        b = new double[Num_Segment];
        c = new double[Num_Segment];
        d = new double[Num_Segment];

        double[] f = new double[Num_Segment - 1];

        // вычисление коэффициентов трёхдиагональной системы
        for (int i = 0; i < Num_Segment - 1; i++) {
            // длина текущего и следующего отрезков
            h_current = Points.get(i + 1).x() - Points.get(i).x();
            h_next    = Points.get(i + 2).x() - Points.get(i + 1).x();

            // диагональ
            b[i] = 2.0 * (h_current + h_next);
            // нижняя диагональ (сдвиг на +1)
            this.a[i + 1] = h_current;
            // верхняя диагональ
            d[i] = h_next;
            // правая часть
            f[i] = 3.0 * ((F_Value.get(i + 2) - F_Value.get(i + 1)) / h_next
                    - (F_Value.get(i + 1) - F_Value.get(i)) / h_current);
        }

        // метод прогонки: прямой ход
        for (int j = 1; j < Num_Segment - 1; j++) {
            double coef = this.a[j] / b[j - 1];
            b[j] -= coef * d[j - 1]; // диагональ
            f[j] -= coef * f[j - 1]; // правая часть
        }

        // метод прогонки: обратный ход — получаем внутренние c[i]
        c[Num_Segment - 1] = f[Num_Segment - 2] / b[Num_Segment - 2];
        for (int j = Num_Segment - 2; j > 0; j--) {
            c[j] = (f[j - 1] - c[j + 1] * d[j - 1]) / b[j - 1];
        }

        // краевые условия нулевой кривизны
        c[0] = 0.0;

        for (int i = 0; i < Num_Segment - 1; i++) {
            h_current = Points.get(i + 1).x() - Points.get(i).x();
            a[i] = F_Value.get(i);
            b[i] = (F_Value.get(i + 1) - F_Value.get(i)) / h_current
                    - (c[i + 1] + 2.0 * c[i]) * h_current / 3.0;
            d[i] = (c[i + 1] - c[i]) / h_current / 3.0;
        }

        h_current = Points.get(Num_Segment).x() - Points.get(Num_Segment - 1).x();
        a[Num_Segment - 1] = F_Value.get(Num_Segment - 1);
        b[Num_Segment - 1] = (F_Value.get(Num_Segment) - F_Value.get(Num_Segment - 1)) / h_current
                - 2.0 * c[Num_Segment - 1] * h_current / 3.0;
        d[Num_Segment - 1] = -c[Num_Segment - 1] / h_current / 3.0;
    }

    @Override
    public void getValue(final Point P, final double[] Res) {
        if (Res == null || Res.length < 3) {
            throw new IllegalArgumentException("Массив Res должен иметь длину не менее 3.");
        }
        if (points.size() < 2) {
            throw new IllegalStateException("Сплайн не инициализирован. Сначала вызовите updateSpline().");
        }

        final double eps = 1e-7;
        final int Num_Segment = points.size() - 1;

        // поиск сегмента, которому принадлежит точка
        for (int i = 0; i < Num_Segment; i++) {
            double xi   = points.get(i).x();
            double xi1  = points.get(i + 1).x();
            double px   = P.x();

            if ((px > xi && px < xi1)
                    || Math.abs(px - xi)  < eps
                    || Math.abs(px - xi1) < eps) {

                double diff = (px - xi);
                Res[0] = a[i] + b[i] * diff + c[i] * Math.pow(diff, 2) + d[i] * Math.pow(diff, 3);
                Res[1] = b[i] + 2.0 * c[i] * diff + 3.0 * d[i] * Math.pow(diff, 2);
                Res[2] = 2.0 * c[i] + 6.0 * d[i] * diff;
                return;
            }
        }
        throw new IllegalArgumentException("The point is not found in the segments...");
    }

    public double[] getValue(final Point p) {
        double[] out = new double[3];
        getValue(p, out);
        return out;
    }
}
