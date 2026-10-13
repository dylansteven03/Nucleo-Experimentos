/* numerico.h — contrato congelado con calculo-service (JNA).
 * No cambiar firmas ni el struct sin acordarlo en la bitácora de ambos frentes. */
#ifndef NUMERICO_H
#define NUMERICO_H

typedef struct {
    double x;      /* aproximación en este paso */
    double fx;     /* f(x) evaluado ahí */
    double error;  /* error estimado del paso */
    int    iter;   /* número de iteración */
    int    _pad;   /* alineación explícita a 8 bytes */
} Iteracion;

/* Códigos de retorno:
 * 0 convergió · 1 tope de iteraciones · 2 derivada nula
 * 3 desbordamiento o NaN · 4 parámetros inválidos
 *
 * Memoria: Java reserva `salida` con capacidad max_iter; C solo la llena.
 * C nunca hace malloc. `n` recibe cuántas filas se escribieron. */

int nm_biseccion(const double *coef, int grado, double a, double b,
                 double tol, int max_iter, Iteracion *salida, int *n);
int nm_newton   (const double *coef, int grado, double x0,
                 double tol, int max_iter, Iteracion *salida, int *n);
int nm_secante  (const double *coef, int grado, double x0, double x1,
                 double tol, int max_iter, Iteracion *salida, int *n);

/* TEMPORAL Fase 1: función de prueba para validar JNA. Se elimina en Fase 3. */
double nm_suma(double a, double b);

#endif
