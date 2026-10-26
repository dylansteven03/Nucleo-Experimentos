/* test_numerico.c — pruebas de la biblioteca nativa.
 * Casos de referencia de docs/contrato-corrida.md (sección 8).
 * Fase 3: bisección, Newton y secante. */
#include <stdio.h>
#include <math.h>
#include "numerico.h"

static int fallos = 0;

static void comprobar(int condicion, const char *mensaje)
{
    if (condicion) {
        printf("OK:    %s\n", mensaje);
    } else {
        printf("FALLO: %s\n", mensaje);
        fallos++;
    }
}

/* Igualdad con tolerancia relativa 1e-9 (como pide el contrato). */
static int casi(double obtenido, double esperado)
{
    return fabs(obtenido - esperado) <= 1e-9 * fmax(1.0, fabs(esperado));
}

/* El error de cada fila debe ser menor que el de la anterior. */
static int error_decrece(const Iteracion *s, int n)
{
    for (int i = 1; i < n; i++) {
        if (s[i].error >= s[i - 1].error) return 0;
    }
    return 1;
}

int main(void)
{
    const double x2_menos_4[]   = {-4.0, 0.0, 1.0};         /* x^2 - 4      */
    const double cubica[]       = {-5.0, -2.0, 0.0, 1.0};   /* x^3 - 2x - 5 */
    const double x2_mas_1[]     = {1.0, 0.0, 1.0};          /* x^2 + 1      */
    Iteracion s[1000];
    int n, codigo;

    comprobar(sizeof(Iteracion) == 32, "sizeof(Iteracion) == 32");
    comprobar(fabs(nm_suma(2.0, 3.0) - 5.0) < 1e-12, "nm_suma(2, 3) == 5");

    /* Caso 1: bisección x^2-4 en [0,5], converge */
    codigo = nm_biseccion(x2_menos_4, 2, 0.0, 5.0, 1e-10, 100, s, &n);
    comprobar(codigo == 0, "caso 1: bisección x^2-4 [0,5] devuelve 0");
    comprobar(n == 36, "caso 1: 36 iteraciones");
    comprobar(casi(s[n - 1].x, 2.00000000004), "caso 1: raíz ~ 2");
    comprobar(casi(s[0].x, 2.5) && casi(s[0].fx, 2.25) && casi(s[0].error, 2.5),
              "caso 1: fila 1 = (2.5, 2.25, 2.5)");
    comprobar(casi(s[2].x, 1.875) && casi(s[2].fx, -0.484375) && casi(s[2].error, 0.625),
              "caso 1: fila 3 = (1.875, -0.484375, 0.625)");
    comprobar(error_decrece(s, n), "caso 1: el error decrece en cada fila");

    /* Caso 5: bisección x^3-2x-5 en [2,3], converge */
    codigo = nm_biseccion(cubica, 3, 2.0, 3.0, 1e-10, 100, s, &n);
    comprobar(codigo == 0, "caso 5: bisección cúbica [2,3] devuelve 0");
    comprobar(n == 34, "caso 5: 34 iteraciones");
    comprobar(casi(s[n - 1].x, 2.0945514816), "caso 5: raíz ~ 2.0945514816");

    /* Caso 7: tope de iteraciones (código 1) */
    codigo = nm_biseccion(x2_menos_4, 2, 0.0, 5.0, 1e-10, 3, s, &n);
    comprobar(codigo == 1, "caso 7: max_iter=3 devuelve 1");
    comprobar(n == 3 && casi(s[2].x, 1.875), "caso 7: 3 filas, última x = 1.875");

    /* Caso 11: sin cambio de signo (código 4) */
    codigo = nm_biseccion(x2_menos_4, 2, 3.0, 5.0, 1e-10, 100, s, &n);
    comprobar(codigo == 4, "caso 11: [3,5] sin cambio de signo devuelve 4");
    comprobar(n == 0, "caso 11: 0 filas escritas");

    /* Otros parámetros inválidos (código 4) */
    codigo = nm_biseccion(x2_menos_4, 2, 0.0, 5.0, 0.0, 100, s, &n);
    comprobar(codigo == 4, "bisección: tolerancia 0 devuelve 4");
    codigo = nm_biseccion(x2_menos_4, 2, 5.0, 0.0, 1e-10, 100, s, &n);
    comprobar(codigo == 4, "bisección: a >= b devuelve 4");
    codigo = nm_biseccion(x2_menos_4, 2, 0.0, 5.0, 1e-10, 0, s, &n);
    comprobar(codigo == 4, "bisección: max_iter 0 devuelve 4");

    /* ---------- Newton-Raphson ---------- */

    /* Caso 2: x^2-4, x0 = 3 */
    codigo = nm_newton(x2_menos_4, 2, 3.0, 1e-10, 100, s, &n);
    comprobar(codigo == 0, "caso 2: Newton x^2-4 x0=3 devuelve 0");
    comprobar(n == 5, "caso 2: 5 iteraciones");
    comprobar(casi(s[n - 1].x, 2.0), "caso 2: raíz ~ 2");
    comprobar(casi(s[0].x, 2.1666666667) && casi(s[0].fx, 0.6944444444) && casi(s[0].error, 0.8333333333),
              "caso 2: fila 1 = (2.1666666667, 0.6944444444, 0.8333333333)");

    /* Caso 4: x^3-2x-5, x0 = 2 */
    codigo = nm_newton(cubica, 3, 2.0, 1e-10, 100, s, &n);
    comprobar(codigo == 0, "caso 4: Newton cúbica x0=2 devuelve 0");
    comprobar(n == 5, "caso 4: 5 iteraciones");
    comprobar(casi(s[n - 1].x, 2.0945514815), "caso 4: raíz ~ 2.0945514815");
    comprobar(casi(s[0].x, 2.1) && casi(s[0].fx, 0.061) && casi(s[0].error, 0.1),
              "caso 4: fila 1 = (2.1, 0.061, 0.1)");

    /* Newton llega en menos iteraciones que la bisección (RN-02) */
    comprobar(n < 34, "RN-02: Newton usa menos iteraciones que bisección");

    /* Caso 8: derivada nula en el punto inicial (código 2) */
    codigo = nm_newton(x2_menos_4, 2, 0.0, 1e-10, 100, s, &n);
    comprobar(codigo == 2, "caso 8: Newton x0=0 devuelve 2");
    comprobar(n == 0, "caso 8: 0 filas escritas");

    /* Caso 9: x^2+1, el primer paso cae en x=0 donde f'=0 (código 2) */
    codigo = nm_newton(x2_mas_1, 2, 1.0, 1e-10, 100, s, &n);
    comprobar(codigo == 2, "caso 9: Newton x^2+1 x0=1 devuelve 2");
    comprobar(n == 1 && casi(s[0].x, 0.0) && casi(s[0].fx, 1.0) && casi(s[0].error, 1.0),
              "caso 9: 1 fila = (0, 1, 1)");

    /* Newton: parámetros inválidos */
    codigo = nm_newton(x2_menos_4, 2, 3.0, -1.0, 100, s, &n);
    comprobar(codigo == 4, "Newton: tolerancia negativa devuelve 4");

    /* ---------- Secante ---------- */

    /* Caso 3: x^2-4, x0=1, x1=3 */
    codigo = nm_secante(x2_menos_4, 2, 1.0, 3.0, 1e-10, 100, s, &n);
    comprobar(codigo == 0, "caso 3: secante x^2-4 devuelve 0");
    comprobar(n == 7, "caso 3: 7 iteraciones");
    comprobar(casi(s[n - 1].x, 2.0), "caso 3: raíz ~ 2");
    comprobar(casi(s[0].x, 1.75) && casi(s[0].fx, -0.9375) && casi(s[0].error, 1.25),
              "caso 3: fila 1 = (1.75, -0.9375, 1.25)");

    /* Caso 6: x^3-2x-5, x0=2, x1=3 */
    codigo = nm_secante(cubica, 3, 2.0, 3.0, 1e-10, 100, s, &n);
    comprobar(codigo == 0, "caso 6: secante cúbica devuelve 0");
    comprobar(n == 7, "caso 6: 7 iteraciones");
    comprobar(casi(s[n - 1].x, 2.0945514815), "caso 6: raíz ~ 2.0945514815");
    comprobar(casi(s[0].x, 2.0588235294) && casi(s[0].error, 0.9411764706),
              "caso 6: fila 1 = (2.0588235294, _, 0.9411764706)");

    /* Caso 10: f(x0) == f(x1) (código 3, sin NaN) */
    codigo = nm_secante(x2_menos_4, 2, -1.0, 1.0, 1e-10, 100, s, &n);
    comprobar(codigo == 3, "caso 10: secante con f(x0)=f(x1) devuelve 3");
    comprobar(n == 0, "caso 10: 0 filas escritas");

    /* Secante: parámetros inválidos */
    codigo = nm_secante(x2_menos_4, 2, 2.0, 2.0, 1e-10, 100, s, &n);
    comprobar(codigo == 4, "secante: x0 == x1 devuelve 4");

    if (fallos > 0) {
        printf("%d prueba(s) fallaron\n", fallos);
        return 1;
    }
    printf("Todas las pruebas pasaron\n");
    return 0;
}