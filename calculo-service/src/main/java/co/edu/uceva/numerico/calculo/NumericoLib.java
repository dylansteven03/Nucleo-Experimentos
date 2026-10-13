package co.edu.uceva.numerico.calculo;

import com.sun.jna.Library;

public interface NumericoLib extends Library {
    double nm_suma(double a, double b);
}