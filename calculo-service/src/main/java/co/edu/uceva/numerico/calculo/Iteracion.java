package co.edu.uceva.numerico.calculo;

import com.sun.jna.Structure;

/** Una fila de la tabla de convergencia. Refleja el struct Iteracion de numerico.h. */
@Structure.FieldOrder({"x", "fx", "error", "iter", "pad"})
public class Iteracion extends Structure {
    public double x;
    public double fx;
    public double error;
    public int iter;
    public int pad;
}