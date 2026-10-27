package co.edu.uceva.numerico.calculo;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Motor en Java puro: mismos métodos, mismas reglas y mismo formato de respuesta que NativoService.
 * Sirve para comparar contra C (RQ-01) y como alternativa si la biblioteca nativa no está.
 */
@Service
public class JavaService {

    public ResultadoCalculo biseccion(double[] coef, double a, double b, double tol, int maxIter) {
        validar(maxIter);
        return medir("BISECCION",
                () -> BaselineJava.biseccion(coef, coef.length - 1, a, b, tol, maxIter));
    }

    public ResultadoCalculo newton(double[] coef, double x0, double tol, int maxIter) {
        validar(maxIter);
        return medir("NEWTON",
                () -> BaselineJava.newton(coef, coef.length - 1, x0, tol, maxIter));
    }

    public ResultadoCalculo secante(double[] coef, double x0, double x1, double tol, int maxIter) {
        validar(maxIter);
        return medir("SECANTE",
                () -> BaselineJava.secante(coef, coef.length - 1, x0, x1, tol, maxIter));
    }

    @FunctionalInterface
    private interface Calculo {
        BaselineJava.Resultado ejecutar();
    }

    private static void validar(int maxIter) {
        if (maxIter < 1 || maxIter > NativoService.MAX_ITER_TOPE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "maxIter debe estar entre 1 y " + NativoService.MAX_ITER_TOPE);
        }
    }

    private static ResultadoCalculo medir(String metodo, Calculo calculo) {
        long inicio = System.nanoTime();
        BaselineJava.Resultado r = calculo.ejecutar();
        double tiempoMs = (System.nanoTime() - inicio) / 1_000_000.0;

        List<FilaIteracion> tabla = r.filas();
        boolean convergio = r.codigo() == 0;
        Double raiz = (convergio && !tabla.isEmpty()) ? r.ultimoX() : null;
        return new ResultadoCalculo(
                metodo,
                convergio ? "CONVERGIO" : "NO_CONVERGIO",
                convergio ? null : NativoService.causa(r.codigo(), metodo),
                r.codigo(),
                raiz,
                tabla.size(),
                tabla,
                tiempoMs);
    }
}
