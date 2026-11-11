package co.edu.uceva.numerico.historial.web;

import co.edu.uceva.numerico.historial.modelo.Corrida;

import java.time.Instant;

public record CorridaResponse(Long id, Long funcionId, String metodo, boolean convergio, Double raiz,
                              int iteraciones, double tiempoNativoMs, double tiempoTotalMs,
                              String usuario, Instant creadoEn) {

    public static CorridaResponse de(Corrida c) {
        return new CorridaResponse(c.getId(), c.getFuncionId(), c.getMetodo(), c.isConvergio(), c.getRaiz(),
                c.getIteraciones(), c.getTiempoNativoMs(), c.getTiempoTotalMs(), c.getUsuario(), c.getCreadoEn());
    }
}
