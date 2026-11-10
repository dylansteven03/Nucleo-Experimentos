package co.edu.uceva.numerico.historial.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/** Lo que el cliente manda al registrar una corrida. El usuario NO viene aquí: sale del token. */
public record CorridaRequest(
        @NotNull(message = "funcionId es obligatorio") Long funcionId,
        @NotBlank(message = "metodo es obligatorio") @Size(max = 40, message = "metodo admite máximo 40 caracteres") String metodo,
        @NotNull(message = "convergio es obligatorio") Boolean convergio,
        Double raiz,
        @NotNull(message = "iteraciones es obligatorio") @PositiveOrZero(message = "iteraciones no puede ser negativo") Integer iteraciones,
        @NotNull(message = "tiempoNativoMs es obligatorio") @PositiveOrZero(message = "tiempoNativoMs no puede ser negativo") Double tiempoNativoMs,
        @NotNull(message = "tiempoTotalMs es obligatorio") @PositiveOrZero(message = "tiempoTotalMs no puede ser negativo") Double tiempoTotalMs) {

    /** Si convergió debe traer la raíz; si no convergió no tiene raíz (RF-06). */
    @JsonIgnore
    @AssertTrue(message = "raiz es obligatoria cuando convergio es true")
    public boolean isRaizCoherente() {
        return !Boolean.TRUE.equals(convergio) || (raiz != null && Double.isFinite(raiz));
    }
}
