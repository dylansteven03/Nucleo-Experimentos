/* numerico.c — Fase 3: métodos numéricos en C.
 * Bisección, Newton-Raphson y secante. */
#include "numerico.h"
#include <math.h>

#define GRADO_MAX 10
#define DERIVADA_NULA 1e-14

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

/* Evalúa la derivada del polinomio en x (también con Horner).
 * La derivada analítica se calcula a partir de los coeficientes. */
static double poly_deriv(const double *coef, int grado, double x)
{
    if (grado == 0) return 0.0;
    double r = grado * coef[grado];
    for (int i = grado - 1; i >= 1; i--) {
        r = r * x + i * coef[i];
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
    if (n) *n = 0;

    /* Código 4: parámetros inválidos. */
    if (!coef || !salida || !n) return 4;
    if (grado < 0 || grado > GRADO_MAX) return 4;
    if (max_iter <= 0) return 4;
    if (!isfinite(tol) || tol <= 0.0) return 4;
    if (!isfinite(x0)) return 4;

    double x = x0;
    for (int i = 1; i <= max_iter; i++) {
        double fx = poly_eval(coef, grado, x);
        double d  = poly_deriv(coef, grado, x);

        /* Código 2: derivada nula, no se divide por cero. */
        if (!isfinite(d) || fabs(d) < DERIVADA_NULA) return 2;

        double x_nuevo  = x - fx / d;
        double fx_nuevo = poly_eval(coef, grado, x_nuevo);
        double err      = fabs(x_nuevo - x);

        salida[i - 1].x     = x_nuevo;
        salida[i - 1].fx    = fx_nuevo;
        salida[i - 1].error = err;
        salida[i - 1].iter  = i;
        salida[i - 1]._pad  = 0;
        *n = i;

        if (!isfinite(x_nuevo) || !isfinite(fx_nuevo)) return 3;
        if (fx_nuevo == 0.0 || err < tol) return 0;

        x = x_nuevo;
    }
    return 1; /* tope de iteraciones */
}

int nm_secante(const double *coef, int grado, double x0, double x1,
               double tol, int max_iter, Iteracion *salida, int *n)
{
    if (n) *n = 0;

    /* Código 4: parámetros inválidos. */
    if (!coef || !salida || !n) return 4;
    if (grado < 0 || grado > GRADO_MAX) return 4;
    if (max_iter <= 0) return 4;
    if (!isfinite(tol) || tol <= 0.0) return 4;
    if (!isfinite(x0) || !isfinite(x1) || x0 == x1) return 4;

    double xa = x0, xb = x1;
    double fa = poly_eval(coef, grado, xa);
    double fb = poly_eval(coef, grado, xb);
    if (!isfinite(fa) || !isfinite(fb)) return 3;

    for (int i = 1; i <= max_iter; i++) {
        /* Código 3: f(x_{n-1}) == f(x_{n-2}) daría división por cero (NaN). */
        if (fb == fa) return 3;

        double x_nuevo  = xb - fb * (xb - xa) / (fb - fa);
        double fx_nuevo = poly_eval(coef, grado, x_nuevo);
        double err      = fabs(x_nuevo - xb);

        salida[i - 1].x     = x_nuevo;
        salida[i - 1].fx    = fx_nuevo;
        salida[i - 1].error = err;
        salida[i - 1].iter  = i;
        salida[i - 1]._pad  = 0;
        *n = i;

        if (!isfinite(x_nuevo) || !isfinite(fx_nuevo)) return 3;
        if (fx_nuevo == 0.0 || err < tol) return 0;

        xa = xb;  fa = fb;
        xb = x_nuevo;  fb = fx_nuevo;
    }
    return 1; /* tope de iteraciones */
}

double nm_suma(double a, double b)
{
    return a + b;
}