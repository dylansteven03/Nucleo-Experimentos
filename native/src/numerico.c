/* numerico.c — implementación pendiente (Fase 3).
 * Stubs que devuelven 4 (parámetros inválidos) para que el módulo compile. */
#include "numerico.h"

int nm_biseccion(const double *coef, int grado, double a, double b,
                 double tol, int max_iter, Iteracion *salida, int *n)
{
    (void)coef; (void)grado; (void)a; (void)b; (void)tol; (void)max_iter; (void)salida;
    if (n) *n = 0;
    return 4;
}

int nm_newton(const double *coef, int grado, double x0,
              double tol, int max_iter, Iteracion *salida, int *n)
{
    (void)coef; (void)grado; (void)x0; (void)tol; (void)max_iter; (void)salida;
    if (n) *n = 0;
    return 4;
}

int nm_secante(const double *coef, int grado, double x0, double x1,
               double tol, int max_iter, Iteracion *salida, int *n)
{
    (void)coef; (void)grado; (void)x0; (void)x1; (void)tol; (void)max_iter; (void)salida;
    if (n) *n = 0;
    return 4;
}

double nm_suma(double a, double b)
{
    return a + b;
}