package co.edu.uceva.numerico.calculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Compara la biblioteca en C (vía JNA) contra la línea base en Java (RN-05).
 * Si libnumerico.so no se puede cargar (por ejemplo en Windows) las pruebas se omiten,
 * no fallan. Para ejecutarlas: -Djna.library.path=<carpeta con libnumerico.so>.
 */
class NativoVsJavaTest {

    private static final NativoService NATIVO = new NativoService();
    private static boolean libDisponible;

    @BeforeAll
    static void comprobarBiblioteca() {
        try {
            NATIVO.suma(1, 2);
            libDisponible = true;
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            libDisponible = false;
        }
        assumeTrue(libDisponible, "libnumerico.so no disponible: se omite la comparación C vs Java");
    }

    static List<CasosReferencia.Caso> casos() {
        return CasosReferencia.TODOS;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    void cYJavaDanLoMismo(CasosReferencia.Caso c) {
        BaselineJava.Resultado java = CasosReferencia.ejecutarJava(c);
        ResultadoCalculo nativo = ejecutarNativo(c);

        assertEquals(java.codigo(), nativo.codigoNativo(), "código");
        assertEquals(java.n(), nativo.totalIteraciones(), "iteraciones");
        if (java.codigo() == 0) {
            assertNotNull(nativo.raiz());
            assertEquals(java.ultimoX(), nativo.raiz(), 1e-9, "raíz C vs Java");
            assertEquals("CONVERGIO", nativo.estado());
        } else {
            assertEquals("NO_CONVERGIO", nativo.estado());
        }
        // Toda la tabla, fila por fila.
        for (int i = 0; i < java.n(); i++) {
            FilaIteracion fj = java.filas().get(i);
            FilaIteracion fc = nativo.iteraciones().get(i);
            assertEquals(fj.n(), fc.n(), "n fila " + (i + 1));
            assertEquals(fj.x(), fc.x(), 1e-9, "x fila " + (i + 1));
            assertEquals(fj.error(), fc.error(), 1e-9, "error fila " + (i + 1));
        }
    }

    private static ResultadoCalculo ejecutarNativo(CasosReferencia.Caso c) {
        return switch (c.metodo()) {
            case "BISECCION" -> NATIVO.biseccion(c.coef(), c.p1(), c.p2(), CasosReferencia.TOL, c.maxIter());
            case "NEWTON" -> NATIVO.newton(c.coef(), c.p1(), CasosReferencia.TOL, c.maxIter());
            case "SECANTE" -> NATIVO.secante(c.coef(), c.p1(), c.p2(), CasosReferencia.TOL, c.maxIter());
            default -> throw new IllegalArgumentException(c.metodo());
        };
    }
}
