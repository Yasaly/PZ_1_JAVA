import java.util.List;
import com.methods.*;

public class Ex1_Demo {
    public static void main(String[] args) {
        double a = 0.0, b = 1.0;
        int N = 10;
        double r = 5.0;

        // равномерная сетка
        List<Point> uniform = MeshGenerator.uniform(a, b, N);

        // адаптивная сетка
        List<Point> gradedLeft = MeshGenerator.graded(a, b, N, r);


        System.out.println("Равномерная сетка:");
        printPoints(uniform);

        System.out.println("\nАдаптивная сетка:");
        printPoints(gradedLeft);
    }

    private static void printPoints(List<Point> pts) {
        for (int i = 0; i < pts.size(); i++) {
            System.out.printf("x[%d] = %.6f%n", i, pts.get(i).x());
        }
    }
}
