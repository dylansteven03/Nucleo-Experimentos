package co.edu.uceva.numerico.funcion.web;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** RF-01: lo que rechaza la API con 400 antes de llegar a la base de datos. */
class FuncionRequestValidacionTest {

    private final Validator validador = Validation.buildDefaultValidatorFactory().getValidator();

    private FuncionRequest con(String nombre, List<Double> coef) {
        return new FuncionRequest(nombre, coef, 0.0, 5.0, null);
    }

    @Test
    void aceptaUnPolinomioDeGrado10() {
        List<Double> once = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            once.add(0.0);
        }
        once.add(1.0);
        assertTrue(validador.validate(con("x^10", once)).isEmpty());
    }

    @Test
    void rechazaGradoMayorA10() {
        List<Double> doce = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            doce.add(0.0);
        }
        doce.add(1.0);
        assertFalse(validador.validate(con("x^11", doce)).isEmpty());
    }

    @Test
    void rechazaListaDeCoeficientesVacia() {
        assertFalse(validador.validate(con("vacía", List.of())).isEmpty());
    }

    @Test
    void rechazaCoeficientesOmitidos() {
        assertFalse(validador.validate(con("sin coeficientes", null)).isEmpty());
    }

    @Test
    void rechazaCoeficienteNulo() {
        assertFalse(validador.validate(con("nulo", Arrays.asList(1.0, null, 2.0))).isEmpty());
    }

    @Test
    void rechazaNombreEnBlanco() {
        assertFalse(validador.validate(con("   ", List.of(-4.0, 0.0, 1.0))).isEmpty());
    }

    @Test
    void rechazaIntervaloOmitido() {
        FuncionRequest sinIntervalo = new FuncionRequest("x²−4", List.of(-4.0, 0.0, 1.0), null, null, null);
        assertFalse(validador.validate(sinIntervalo).isEmpty());
    }
}
