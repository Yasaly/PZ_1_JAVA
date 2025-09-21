package com.methods;

import java.util.List;

public interface Spline {

      //обновить сплайн по точкам и значениям функци

    void updateSpline(List<Point> points, List<Double> fValues);

     //получить значение сплайна и его производных в точке p

    void getValue(Point p, double[] res);
}
