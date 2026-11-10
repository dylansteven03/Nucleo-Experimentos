package co.edu.uceva.numerico.historial.web;

import co.edu.uceva.numerico.historial.modelo.Corrida;
import co.edu.uceva.numerico.historial.modelo.CorridaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HistorialServiceTest {

    private final CorridaRepository repositorio = mock(CorridaRepository.class);
    private final HistorialService servicio = new HistorialService(repositorio);

    @Test
    void elDuenoEsElSubDelTokenNoLoQueMandaElCliente() {
        when(repositorio.save(any(Corrida.class))).thenAnswer(i -> i.getArgument(0));

        servicio.registrar(new CorridaRequest(7L, " biseccion ", true, 1.41, 20, 0.8, 12.5), "sub-estudiante-1");

        ArgumentCaptor<Corrida> captura = ArgumentCaptor.forClass(Corrida.class);
        verify(repositorio).save(captura.capture());
        assertEquals("sub-estudiante-1", captura.getValue().getUsuario());
        assertEquals("biseccion", captura.getValue().getMetodo());
        assertEquals(12.5, captura.getValue().getTiempoTotalMs());
    }

    @Test
    void siNoConvergioNoGuardaRaiz() {
        when(repositorio.save(any(Corrida.class))).thenAnswer(i -> i.getArgument(0));

        Corrida c = servicio.registrar(new CorridaRequest(7L, "newton", false, 3.0, 100, 1.0, 20.0), "sub-1");

        assertNull(c.getRaiz());
    }

    @Test
    void elHistorialPropioSeConsultaPorElSub() {
        when(repositorio.findByUsuarioOrderByCreadoEnDesc("sub-1")).thenReturn(List.of());

        servicio.propias("sub-1");

        verify(repositorio).findByUsuarioOrderByCreadoEnDesc("sub-1");
    }

    @Test
    void elHistorialCompletoNoFiltraPorUsuario() {
        servicio.todas();

        verify(repositorio).findAllByOrderByCreadoEnDesc();
    }
}
