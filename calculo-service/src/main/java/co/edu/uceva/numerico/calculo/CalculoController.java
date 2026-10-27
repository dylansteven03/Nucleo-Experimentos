package co.edu.uceva.numerico.calculo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * TEMPORAL Fase 4: endpoints GET para probar de punta a punta desde curl.
 * El contrato final (POST con coeficientes) llega con el gateway.
 * Los coeficientes van de menor a mayor grado: x^2 - 4  ->  coef=-4,0,1
 */
@RestController
@RequestMapping("/calculo")
public class CalculoController {

    private final CalculoSelector nativo;

    public CalculoController(CalculoSelector nativo) {
        this.nativo = nativo;
    }

    @GetMapping("/biseccion")
    public ResultadoCalculo biseccion(@RequestParam double[] coef,
                                      @RequestParam double a,
                                      @RequestParam double b,
                                      @RequestParam(defaultValue = "1e-6") double tol,
                                      @RequestParam(defaultValue = "100") int maxIter) {
        return nativo.biseccion(coef, a, b, tol, maxIter);
    }

    @GetMapping("/newton")
    public ResultadoCalculo newton(@RequestParam double[] coef,
                                   @RequestParam double x0,
                                   @RequestParam(defaultValue = "1e-6") double tol,
                                   @RequestParam(defaultValue = "100") int maxIter) {
        return nativo.newton(coef, x0, tol, maxIter);
    }

    @GetMapping("/secante")
    public ResultadoCalculo secante(@RequestParam double[] coef,
                                    @RequestParam double x0,
                                    @RequestParam double x1,
                                    @RequestParam(defaultValue = "1e-6") double tol,
                                    @RequestParam(defaultValue = "100") int maxIter) {
        return nativo.secante(coef, x0, x1, tol, maxIter);
    }
}
