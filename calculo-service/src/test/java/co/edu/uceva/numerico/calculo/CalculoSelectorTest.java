package co.edu.uceva.numerico.calculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** El selector elige el motor por configuración. Con "java" no se toca la biblioteca nativa. */
class CalculoSelectorTest {

    @Test
    void conMotorJavaCalculaSinBibliotecaNativa() {
        CalculoSelector s = new CalculoSelector(new NativoService(), new JavaService(), "java");

        ResultadoCalculo r = s.newton(CasosReferencia.X2_MENOS_4, 3, 1e-10, 100);

        assertEquals("java", s.motor());
        assertEquals("CONVERGIO", r.estado());
        assertEquals(5, r.totalIteraciones());
        assertEquals(2.0, r.raiz(), 1e-8);
    }

    @Test
    void sinPropiedadElMotorPorDefectoEsNativo() {
        CalculoSelector s = new CalculoSelector(new NativoService(), new JavaService(), "nativo");
        assertEquals("nativo", s.motor());
    }

    @Test
    void unMotorDesconocidoFallaAlArrancar() {
        assertThrows(IllegalArgumentException.class,
                () -> new CalculoSelector(new NativoService(), new JavaService(), "fortran"));
    }

    @Test
    void javaDevuelveNoConvergioConSuCausa() {
        CalculoSelector s = new CalculoSelector(new NativoService(), new JavaService(), "java");

        ResultadoCalculo r = s.newton(CasosReferencia.X2_MENOS_4, 0, 1e-10, 100);

        assertEquals("NO_CONVERGIO", r.estado());
        assertEquals("ZERO_DERIVATIVE", r.codigoCausa());
    }
}
