package co.edu.uceva.numerico.historial.web;

import co.edu.uceva.numerico.historial.modelo.Corrida;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Historial de corridas. Quién puede qué lo declara SecurityConfig:
 *   POST /historial       docente, estudiante  (registra a nombre del token)
 *   GET  /historial/mio   docente, estudiante  (solo lo propio)
 *   GET  /historial       docente              (todo)
 */
@RestController
@RequestMapping("/historial")
public class HistorialController {

    private final HistorialService servicio;

    public HistorialController(HistorialService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<CorridaResponse> registrar(@Valid @RequestBody CorridaRequest peticion,
                                                     @AuthenticationPrincipal Jwt jwt) {
        Corrida creada = servicio.registrar(peticion, jwt.getSubject());
        return ResponseEntity.created(URI.create("/historial/" + creada.getId()))
                .body(CorridaResponse.de(creada));
    }

    @GetMapping("/mio")
    public List<CorridaResponse> propio(@AuthenticationPrincipal Jwt jwt) {
        return servicio.propias(jwt.getSubject()).stream().map(CorridaResponse::de).toList();
    }

    @GetMapping
    public List<CorridaResponse> todo() {
        return servicio.todas().stream().map(CorridaResponse::de).toList();
    }
}
