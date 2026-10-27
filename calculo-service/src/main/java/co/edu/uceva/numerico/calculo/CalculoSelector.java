package co.edu.uceva.numerico.calculo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Elige el motor de cálculo según la propiedad nucleo.motor:
 *   nativo (por defecto) -> biblioteca en C vía JNA
 *   java                 -> implementación en Java puro
 * Se cambia sin recompilar: variable de entorno NUCLEO_MOTOR=java o -Dnucleo.motor=java.
 */
@Service
public class CalculoSelector {

    private final NativoService nativo;
    private final JavaService java;
    private final boolean usaNativo;

    public CalculoSelector(NativoService nativo, JavaService java,
                           @Value("${nucleo.motor:nativo}") String motor) {
        this.nativo = nativo;
        this.java = java;
        if ("nativo".equalsIgnoreCase(motor)) {
            this.usaNativo = true;
        } else if ("java".equalsIgnoreCase(motor)) {
            this.usaNativo = false;
        } else {
            throw new IllegalArgumentException(
                    "nucleo.motor debe ser 'nativo' o 'java', pero es '" + motor + "'");
        }
    }

    public String motor() {
        return usaNativo ? "nativo" : "java";
    }

    public ResultadoCalculo biseccion(double[] coef, double a, double b, double tol, int maxIter) {
        return usaNativo ? nativo.biseccion(coef, a, b, tol, maxIter)
                : java.biseccion(coef, a, b, tol, maxIter);
    }

    public ResultadoCalculo newton(double[] coef, double x0, double tol, int maxIter) {
        return usaNativo ? nativo.newton(coef, x0, tol, maxIter)
                : java.newton(coef, x0, tol, maxIter);
    }

    public ResultadoCalculo secante(double[] coef, double x0, double x1, double tol, int maxIter) {
        return usaNativo ? nativo.secante(coef, x0, x1, tol, maxIter)
                : java.secante(coef, x0, x1, tol, maxIter);
    }
}
