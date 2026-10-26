/* numerico.c — Fase 3: métodos numéricos en C.
 * Bisección implementada; Newton y secante siguen como stubs (código 4). */
#include "numerico.h"
#include <math.h>

#define GRADO_MAX 10

/* Evalúa el polinomio en x con el método de Horner.
 * coef[i] es el coeficiente de x^i; hay grado+1 valores. */
static double poly_eval(const double *coef, int grado, double x)
{
    double r = coef[grado];
    for (int i = grado - 1; i >= 0; i--) {
        r = r * x + coef[i];
    }
    return r;
}

int nm_biseccion(const double *coef, int grado, double a, double b,
                 double tol, int max_iter, Iteracion *salida, int *n)
{
    if (n) *n = 0;

    /* Código 4: parámetros inválidos. */
    if (!coef || !salida || !n) return 4;
    if (grado < 0 || grado > GRADO_MAX) return 4;
    if (max_iter <= 0) return 4;
    if (!isfinite(tol) || tol <= 0.0) return 4;
    if (!isfinite(a) || !isfinite(b) || a >= b) return 4;

    double fa = poly_eval(coef, grado, a);
    double fb = poly_eval(coef, grado, b);
    if (!isfinite(fa) || !isfinite(fb)) return 3;
    /* Se exige cambio de signo: f(a)·f(b) < 0. */
    if (!((fa < 0.0 && fb > 0.0) || (fa > 0.0 && fb < 0.0))) return 4;

    for (int i = 1; i <= max_iter; i++) {
        double m   = a + (b - a) / 2.0;
        double fm  = poly_eval(coef, grado, m);
        double err = (b - a) / 2.0;

        salida[i - 1].x     = m;
        salida[i - 1].fx    = fm;
        salida[i - 1].error = err;
        salida[i - 1].iter  = i;
        salida[i - 1]._pad  = 0;
        *n = i;

        if (!isfinite(fm)) return 3;
        if (fm == 0.0 || err < tol) return 0;

        /* Nos quedamos con la mitad donde cambia el signo. */
        if ((fa < 0.0) == (fm < 0.0)) {
            a = m;
            fa = fm;
        } else {
            b = m;
        }
    }
    return 1; /* tope de iteraciones */
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