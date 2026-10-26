package co.edu.uceva.numerico.calculo;

import java.util.List;

/** Resultado de un método numérico. La no convergencia NO es un error: estado = NO_CONVERGIO. */
public record ResultadoCalculo(
        String metodo,
        String estado,
        String codigoCausa,
        int codigoNativo,
        Double raiz,
        int totalIteraciones,
        List<FilaIteracion> iteraciones,
        double tiempoNativoMs) {
}