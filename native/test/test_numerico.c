/* test_numerico.c — casos de referencia pendientes (Fase 2/3).
 * Por ahora solo comprueba el tamaño del struct (debe ser 32 bytes). */
#include <stdio.h>
#include "numerico.h"

int main(void)
{
    if (sizeof(Iteracion) != 32) {
        printf("FALLO: sizeof(Iteracion) = %zu, se esperaba 32\n", sizeof(Iteracion));
        return 1;
    }
    printf("OK: sizeof(Iteracion) = 32\n");
    return 0;
}
