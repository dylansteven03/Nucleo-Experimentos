package co.edu.uceva.numerico.calculo;

/** Una fila de la tabla de convergencia, lista para convertirse en JSON. */
public record FilaIteracion(int n, double x, double fx, double error) {
}