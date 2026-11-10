package co.edu.uceva.numerico.historial.web;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Lo que rechaza la API con 400 antes de tocar la base de datos. */
class CorridaRequestValidacionTest {

    private final Validator validador = Validation.buildDefaultValidatorFactory().getValidator();

    private CorridaRequest valida() {
        return new CorridaRequest(1L, "biseccion", true, 1.4142, 20, 0.8, 12.5);
    }

    @Test
    void aceptaUnaCorridaCompleta() {
        assertTrue(validador.validate(valida()).isEmpty());
    }

    @Test
    void aceptaUnaCorridaQueNoConvergioSinRaiz() {
        assertTrue(validador.validate(new CorridaRequest(1L, "newton", false, null, 100, 1.0, 20.0)).isEmpty());
    }

    @Test
    void rechazaConvergioSinRaiz() {
        assertFalse(validador.validate(new CorridaRequest(1L, "newton", true, null, 5, 1.0, 20.0)).isEmpty());
    }

    @Test
    void rechazaRaizInfinitaCuandoConvergio() {
        assertFalse(validador.validate(
                new CorridaRequest(1L, "newton", true, Double.POSITIVE_INFINITY, 5, 1.0, 20.0)).isEmpty());
    }

    @Test
    void rechazaMetodoEnBlanco() {
        assertFalse(validador.validate(new CorridaRequest(1L, "  ", true, 1.0, 5, 1.0, 20.0)).isEmpty());
    }

    @Test
    void rechazaTiemposNegativos() {
        assertFalse(validador.validate(new CorridaRequest(1L, "biseccion", true, 1.0, 5, -1.0, 20.0)).isEmpty());
        assertFalse(validador.validate(new CorridaRequest(1L, "biseccion", true, 1.0, 5, 1.0, -20.0)).isEmpty());
    }

    @Test
    void rechazaCamposObligatoriosAusentes() {
        assertFalse(validador.validate(new CorridaRequest(null, "biseccion", true, 1.0, 5, 1.0, 20.0)).isEmpty());
        assertFalse(validador.validate(new CorridaRequest(1L, "biseccion", null, 1.0, 5, 1.0, 20.0)).isEmpty());
        assertFalse(validador.validate(new CorridaRequest(1L, "biseccion", true, 1.0, null, 1.0, 20.0)).isEmpty());
    }
}
