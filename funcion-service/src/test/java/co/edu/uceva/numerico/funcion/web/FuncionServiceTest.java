package co.edu.uceva.numerico.funcion.web;

import co.edu.uceva.numerico.funcion.modelo.Funcion;
import co.edu.uceva.numerico.funcion.modelo.FuncionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Reglas de negocio de RF-01 sin levantar Spring ni la base de datos. */
class FuncionServiceTest {

    private final FuncionRepository repositorio = mock(FuncionRepository.class);
    private final FuncionService servicio = new FuncionService(repositorio);

    @BeforeEach
    void preparar() {
        when(repositorio.save(any(Funcion.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void registraUnaFuncionValidaConValoresPorDefecto() {
        FuncionRequest p = new FuncionRequest("x²−4", List.of(-4.0, 0.0, 1.0), 0.0, 5.0, null);

        Funcion f = servicio.registrar(p, "sub-docente");

        assertEquals(2, f.getGrado());
        assertEquals(1e-6, f.getToleranciaPorDefecto());
        assertEquals("sub-docente", f.getCreadoPor());
    }

    @Test
    void rechazaIntervaloInvertido() {
        FuncionRequest p = new FuncionRequest("x²−4", List.of(-4.0, 0.0, 1.0), 5.0, 0.0, null);

        ValidacionException ex = assertThrows(ValidacionException.class, () -> servicio.registrar(p, "s"));

        assertTrue(ex.getErrores().stream().anyMatch(e -> e.campo().equals("intervaloB")));
    }

    @Test
    void rechazaCoeficienteDeMayorGradoIgualACero() {
        FuncionRequest p = new FuncionRequest("mal", List.of(-4.0, 1.0, 0.0), 0.0, 5.0, null);

        ValidacionException ex = assertThrows(ValidacionException.class, () -> servicio.registrar(p, "s"));

        assertTrue(ex.getErrores().stream().anyMatch(e -> e.campo().equals("coeficientes")));
    }

    @Test
    void rechazaToleranciaNoPositiva() {
        FuncionRequest p = new FuncionRequest("x²−4", List.of(-4.0, 0.0, 1.0), 0.0, 5.0, 0.0);

        ValidacionException ex = assertThrows(ValidacionException.class, () -> servicio.registrar(p, "s"));

        assertTrue(ex.getErrores().stream().anyMatch(e -> e.campo().equals("toleranciaPorDefecto")));
    }

    @Test
    void rechazaCoeficientesNoFinitos() {
        FuncionRequest p = new FuncionRequest("mal", List.of(Double.NaN, 0.0, 1.0), 0.0, 5.0, null);

        assertThrows(ValidacionException.class, () -> servicio.registrar(p, "s"));
    }

    @Test
    void obtenerUnIdInexistenteLanzaNoEncontrada() {
        when(repositorio.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThrows(FuncionNoEncontradaException.class, () -> servicio.obtener(99L));
    }
}
