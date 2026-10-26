/* test_numerico.c — pruebas de la biblioteca nativa.
 * Casos de referencia de docs/contrato-corrida.md (sección 8).
 * Fase 3, parte 1: bisección. Newton y secante se agregan después. */
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

    if (fallos > 0) {
        printf("%d prueba(s) fallaron\n", fallos);
        return 1;
    }
    printf("Todas las pruebas pasaron\n");
    return 0;
}