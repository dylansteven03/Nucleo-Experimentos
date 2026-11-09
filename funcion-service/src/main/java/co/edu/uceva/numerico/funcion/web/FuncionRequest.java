package co.edu.uceva.numerico.funcion.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Cuerpo de POST /funciones (RF-01).
 * Los coeficientes van de menor a mayor grado: x³ − 2x − 5 se envía como [-5, -2, 0, 1].
 */
public record FuncionRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre admite como máximo 120 caracteres")
        String nombre,

        @NotEmpty(message = "Debe enviar al menos un coeficiente")
        @Size(max = 11, message = "El grado máximo es 10: se admiten hasta 11 coeficientes")
        List<@NotNull(message = "Cada coeficiente debe ser un número") Double> coeficientes,

        @NotNull(message = "El inicio del intervalo de trabajo es obligatorio")
        Double intervaloA,

        @NotNull(message = "El fin del intervalo de trabajo es obligatorio")
        Double intervaloB,

        /** Opcional. Si no se envía, se usa 1e-6. */
        Double toleranciaPorDefecto) {
}
