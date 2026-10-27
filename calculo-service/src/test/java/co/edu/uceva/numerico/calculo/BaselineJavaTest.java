package co.edu.uceva.numerico.calculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** Comprueba que la línea base en Java cumple los 11 casos de referencia del contrato. */
class BaselineJavaTest {

    static List<CasosReferencia.Caso> casos() {
        return CasosReferencia.TODOS;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    void cumpleElCasoDeReferencia(CasosReferencia.Caso c) {
        BaselineJava.Resultado r = CasosReferencia.ejecutarJava(c);

        assertEquals(c.codigo(), r.codigo(), "código");
        assertEquals(c.n(), r.n(), "iteraciones");
        if (c.codigo() == 0) {
            assertEquals(c.raiz(), r.ultimoX(), 1e-8, "raíz");
        }
    }

    @Test
    void newtonUsaMenosIteracionesQueBiseccion() {
        // RN-02: mismo polinomio, Newton converge más rápido que bisección.
        int newton = BaselineJava.newton(CasosReferencia.CUBICA, 3, 2, 1e-10, 100).n();
        int biseccion = BaselineJava.biseccion(CasosReferencia.CUBICA, 3, 2, 3, 1e-10, 100).n();
        assertTrue(newton < biseccion);
    }

    @Test
    void biseccionDisminuyeElErrorEnCadaFila() {
        List<FilaIteracion> filas = BaselineJava.biseccion(CasosReferencia.X2_MENOS_4, 2, 0, 5, 1e-10, 100).filas();
        for (int i = 1; i < filas.size(); i++) {
            assertTrue(filas.get(i).error() < filas.get(i - 1).error());
        }
    }

    @Test
    void parametrosInvalidosDevuelvenCodigo4() {
        double[] c = CasosReferencia.X2_MENOS_4;
        assertEquals(4, BaselineJava.biseccion(c, 2, 5, 0, 1e-10, 100).codigo(), "a >= b");
        assertEquals(4, BaselineJava.newton(c, 2, 3, 0, 100).codigo(), "tol <= 0");
        assertEquals(4, BaselineJava.secante(c, 2, 1, 1, 1e-10, 100).codigo(), "x0 == x1");
        assertEquals(4, BaselineJava.newton(c, 11, 3, 1e-10, 100).codigo(), "grado > 10");
    }
}
