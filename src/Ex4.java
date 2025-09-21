import java.util.function.DoubleUnaryOperator;

public class Ex4 {

    public static void main(String[] args) {
        // интервал и центральная точка
        double a = 0.03, b = 0.25;
        double x0 = 0.5 * (a + b);

        // функция и её производная
        DoubleUnaryOperator f  = Math::sin;
        DoubleUnaryOperator df = Math::cos;

        // точность
        double eps = 1e-4;

        // стартовый шаг и допустимый максимум (чтобы x0+-2h лежали в от а до б для 5-ти точечной схемы)
        double hMax = Math.min(x0 - a, b - x0) / 2.0;
        double h = 0.01; // шаг
        double hMin = 1e-12;

        System.out.printf("%nинтервал: [%.5f, %.5f], x0=%.5f, eps=%.1e%n%n", a, b, x0, eps);
        System.out.println("h\t\t\tcd3\t\t\t|err|\t\tcd5\t\t\t|err|");

        Best best = new Best();

        while (h >= hMin && h <= hMax) {
            double exact = df.applyAsDouble(x0);

            double d_cd3 = cd3(f, x0, h);
            double e_cd3 = Math.abs(d_cd3 - exact);

            double d_cd5 = cd5(f, x0, h);
            double e_cd5 = Math.abs(d_cd5 - exact);

            System.out.printf("%.6g\t%.8f\t%.6f\t%.8f\t%.6f%n",
                    h, d_cd3, e_cd3, d_cd5, e_cd5);

            best.consider("cd3", 2, h, d_cd3, e_cd3, eps);
            best.consider("cd5", 4, h, d_cd5, e_cd5, eps);

            h *= 0.5; // уменьшаем шаг
        }

        System.out.println();
        if (best.found) {
            System.out.println("оптимальный вариант:");
            System.out.println("  метод: " + best.name);
            System.out.println("  порядок: O(h^" + best.order + ")");
            System.out.println("  h: " + best.h);
            System.out.println("  значение производной: " + best.value);
            System.out.println("  оценка ошибки: " + best.err);
            System.out.println("  число вызовов f: " + best.cost());
        } else {
            System.out.println("не достигли точности eps на перебранных шагах. попробуй увеличить число итераций или изменить стартовый h.");
        }
    }

    // центральная 3-точечная: O(h2)
    private static double cd3(DoubleUnaryOperator f, double x, double h) {
        return (f.applyAsDouble(x + h) - f.applyAsDouble(x - h)) / (2.0 * h);
    }

    // центральная 5-точечная: O(h4)
    private static double cd5(DoubleUnaryOperator f, double x, double h) {
        double fp2 = f.applyAsDouble(x + 2*h);
        double fp1 = f.applyAsDouble(x + h);
        double fm1 = f.applyAsDouble(x - h);
        double fm2 = f.applyAsDouble(x - 2*h);
        return (-fp2 + 8.0*fp1 - 8.0*fm1 + fm2) / (12.0 * h);
    }

    // выбор лучшего варианта
    private static final class Best {
        boolean found = false;
        String name;
        int order;
        double h, value, err;
        int evals;

        void consider(String method, int ord, double step, double val, double error, double eps) {
            if (error > eps) return;
            int cost = costOf(method);
            if (!found
                    || cost < evals
                    || (cost == evals && step > h)
                    || (cost == evals && step == h && error < err)) {
                found = true; name = method; order = ord;
                h = step; value = val; err = error; evals = cost;
            }
        }

        int cost() { return evals; }

        private int costOf(String method) {
            return switch (method) {
                case "cd3" -> 2; // f(x+-h)
                case "cd5" -> 4; // f(x+-h), f(x+-2h)
                default -> Integer.MAX_VALUE;
            };
        }
    }
}
