package co.edu.uceva.numerico.funcion.web;

import co.edu.uceva.numerico.funcion.modelo.Funcion;

import java.time.Instant;
import java.util.List;

public record FuncionResponse(
        Long id,
        String nombre,
        int grado,
        List<Double> coeficientes,
        double intervaloA,
        double intervaloB,
        double toleranciaPorDefecto,
        String creadoPor,
        Instant creadoEn) {

    public static FuncionResponse de(Funcion f) {
        return new FuncionResponse(f.getId(), f.getNombre(), f.getGrado(), f.getCoeficientes(),
                f.getIntervaloA(), f.getIntervaloB(), f.getToleranciaPorDefecto(),
                f.getCreadoPor(), f.getCreadoEn());
    }
}
