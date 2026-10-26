package co.edu.uceva.numerico.calculo;

import com.sun.jna.Library;
import com.sun.jna.ptr.IntByReference;

public interface NumericoLib extends Library {

    /** TEMPORAL Fase 1: se elimina cuando nadie más la use. */
    double nm_suma(double a, double b);

    int nm_biseccion(double[] coef, int grado, double a, double b,
                     double tol, int maxIter, Iteracion[] salida, IntByReference n);

    int nm_newton(double[] coef, int grado, double x0,
                  double tol, int maxIter, Iteracion[] salida, IntByReference n);

    int nm_secante(double[] coef, int grado, double x0, double x1,
                   double tol, int maxIter, Iteracion[] salida, IntByReference n);
}