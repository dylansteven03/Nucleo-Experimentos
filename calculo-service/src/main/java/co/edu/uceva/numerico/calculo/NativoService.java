package co.edu.uceva.numerico.calculo;

import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NativoService {

    /** Tope de iteraciones que se acepta; define el tamaño del arreglo que Java reserva. */
    static final int MAX_ITER_TOPE = 1000;

    private NumericoLib lib;

    private synchronized NumericoLib lib() {
        if (lib == null) {
            lib = Native.load("numerico", NumericoLib.class);
        }
        return lib;
    }

    /** TEMPORAL Fase 1. */
    public double suma(double a, double b) {
        return lib().nm_suma(a, b);
    }

    public ResultadoCalculo biseccion(double[] coef, double a, double b, double tol, int maxIter) {
        return ejecutar("BISECCION", maxIter,
                (buffer, n) -> lib().nm_biseccion(coef, coef.length - 1, a, b, tol, maxIter, buffer, n));
    }

    public ResultadoCalculo newton(double[] coef, double x0, double tol, int maxIter) {
        return ejecutar("NEWTON", maxIter,
                (buffer, n) -> lib().nm_newton(coef, coef.length - 1, x0, tol, maxIter, buffer, n));
    }

    public ResultadoCalculo secante(double[] coef, double x0, double x1, double tol, int maxIter) {
        return ejecutar("SECANTE", maxIter,
                (buffer, n) -> lib().nm_secante(coef, coef.length - 1, x0, x1, tol, maxIter, buffer, n));
    }

    @FunctionalInterface
    private interface LlamadaNativa {
        int invocar(Iteracion[] buffer, IntByReference n);
    }

    /**
     * Java reserva el arreglo y C solo lo llena: una sola llamada nativa por corrida,
     * sin malloc ni free del lado de C.
     */
    private ResultadoCalculo ejecutar(String metodo, int maxIter, LlamadaNativa llamada) {
        if (maxIter < 1 || maxIter > MAX_ITER_TOPE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "maxIter debe estar entre 1 y " + MAX_ITER_TOPE);
        }
        Iteracion[] buffer = (Iteracion[]) new Iteracion().toArray(maxIter);
        IntByReference n = new IntByReference();

        long inicio = System.nanoTime();
        int codigo = llamada.invocar(buffer, n);
        double tiempoMs = (System.nanoTime() - inicio) / 1_000_000.0;

        int filas = Math.max(0, Math.min(n.getValue(), maxIter));
        List<FilaIteracion> tabla = new ArrayList<>(filas);
        for (int i = 0; i < filas; i++) {
            Iteracion it = buffer[i];
            tabla.add(new FilaIteracion(it.iter, it.x, it.fx, it.error));
        }

        boolean convergio = codigo == 0;
        Double raiz = (convergio && filas > 0) ? tabla.get(filas - 1).x() : null;
        return new ResultadoCalculo(
                metodo,
                convergio ? "CONVERGIO" : "NO_CONVERGIO",
                convergio ? null : causa(codigo, metodo),
                codigo,
                raiz,
                filas,
                tabla,
                tiempoMs);
    }

    /** Traduce los códigos de C a la causa de la API (sección 5 del contrato). */
    private static String causa(int codigo, String metodo) {
        return switch (codigo) {
            case 1 -> "MAX_ITERATIONS_REACHED";
            case 2 -> "ZERO_DERIVATIVE";
            case 3 -> "SECANTE".equals(metodo) ? "DIVISION_BY_ZERO" : "NUMERICAL_INSTABILITY";
            case 4 -> "DOMAIN_BOUNDS_EXCEEDED";
            default -> "UNKNOWN_NATIVE_CODE";
        };
    }
}