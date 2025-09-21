import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

import com.methods.CubicInterpolationSpline1D;
import com.methods.MeshGenerator;
import com.methods.Point;
import com.methods.Spline;

public class Ex3 {

    public static void main(String[] args) {
        // интервал и функция
        double a = 0.03, b = 0.25;                 // от а до б
        DoubleUnaryOperator f   = Math::sin;         // f(x)
        DoubleUnaryOperator df  = Math::cos;         // первая производная
        DoubleUnaryOperator d2f = x -> -Math.sin(x); // вторая производная

        //кол-во сегментов сетки
        int baseN = 16; // тогда h = (b-a)/N, вложенные сетки: 2N и 4N

        // считаем для трёх сеток
        runCase("h   ", a, b, baseN, f, df, d2f);
        runCase("h/2 ", a, b, baseN * 2, f, df, d2f);
        runCase("h/4 ", a, b, baseN * 4, f, df, d2f);
    }

    // строим сетку, сплайн, строим таблицы, вычисляем ошибки
    private static void runCase(String tag, double a, double b, int N,
                                DoubleUnaryOperator f, DoubleUnaryOperator df, DoubleUnaryOperator d2f) {
        System.out.println("шаг " + tag + " (N=" + N + ", узлов " + (N + 1) + ")");

        // равномерная сетка и табличные значения f в узлах
        List<Point> grid = MeshGenerator.uniform(a, b, N);
        List<Double> fvals = MeshGenerator.sample(grid, f);

        // строим сплайн
        Spline spline = new CubicInterpolationSpline1D();
        spline.updateSpline(grid, fvals);

        // формируем неузловые точки
        List<Point> mids = midpoints(grid);

        // печать таблицы значений сплайна и производных
        printSampleTable(spline, f, df, d2f, mids);

        // оценка точности на плотной проверочной сетке (исключаем узлы)
        int M = 2001; // точек проверки
        double[] deltas = maxErrorsOnDenseGrid(spline, f, df, d2f, a, b, M);

        System.out.printf("Del0 = %.8f,  Del1 = %.8f,  Del2 = %.8f%n%n", deltas[0], deltas[1], deltas[2]);
    }

    // середины сегментов (чтобы точно не совпасть с узловыми точками)
    private static List<Point> midpoints(List<Point> grid) {
        List<Point> mids = new ArrayList<>(grid.size() - 1);
        for (int i = 0; i < grid.size() - 1; i++) {
            double x = 0.5 * (grid.get(i).x() + grid.get(i + 1).x());
            mids.add(new Point(x, 0, 0));
        }
        return mids;
    }

    // печать таблицыв
    private static void printSampleTable(Spline s,
                                         DoubleUnaryOperator f, DoubleUnaryOperator df, DoubleUnaryOperator d2f,
                                         List<Point> pts) {
        System.out.println("x\t\t\tf(x)\t\ts(x)\t\tf'(x)\t\ts'(x)\t\tf''(x)\t\ts''(x)");
        double[] res = new double[3];
        for (Point p : pts) {
            s.getValue(p, res);
            System.out.printf("%.6f\t%.6f\t%.6f\t%.6f\t%.6f\t%.6f\t%.6f%n",
                    p.x(), f.applyAsDouble(p.x()), res[0],
                    df.applyAsDouble(p.x()), res[1],
                    d2f.applyAsDouble(p.x()), res[2]);
        }
        System.out.println();
    }


    // максимум ошибок на плотной проверочной сетке
    private static double[] maxErrorsOnDenseGrid(Spline s,
                                                 DoubleUnaryOperator f, DoubleUnaryOperator df, DoubleUnaryOperator d2f,
                                                 double a, double b, int M) {
        double max0 = 0.0, max1 = 0.0, max2 = 0.0;
        double[] res = new double[3];
        double h = (b - a) / (M - 1);
        double eps = 1e-12;

        for (int i = 0; i < M; i++) {
            double x = a + i * h;

            // сдвигаем x на полшага, чтобы не попасть в узлы исходной сетки
            double xShifted = Math.min(b - eps, Math.max(a + eps, x + 0.5 * h));

            s.getValue(new Point(xShifted, 0, 0), res);

            double e0 = Math.abs(f.applyAsDouble(xShifted)  - res[0]);
            double e1 = Math.abs(df.applyAsDouble(xShifted) - res[1]);
            double e2 = Math.abs(d2f.applyAsDouble(xShifted) - res[2]);

            if (e0 > max0) max0 = e0;
            if (e1 > max1) max1 = e1;
            if (e2 > max2) max2 = e2;
        }
        return new double[]{max0, max1, max2};
    }
}
