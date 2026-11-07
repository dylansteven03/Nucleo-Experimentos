package co.edu.uceva.numerico.funcion.web;

import co.edu.uceva.numerico.funcion.modelo.Funcion;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Catálogo de funciones. Quién puede qué lo declara SecurityConfig (matriz de autorización).
 */
@RestController
@RequestMapping("/funciones")
public class FuncionController {

    private final FuncionService servicio;

    public FuncionController(FuncionService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<FuncionResponse> registrar(@Valid @RequestBody FuncionRequest peticion,
                                                     @AuthenticationPrincipal Jwt jwt) {
        Funcion creada = servicio.registrar(peticion, jwt.getSubject());
        return ResponseEntity.created(URI.create("/funciones/" + creada.getId()))
                .body(FuncionResponse.de(creada));
    }

    @GetMapping
    public List<FuncionResponse> listar() {
        return servicio.listar().stream().map(FuncionResponse::de).toList();
    }

    @GetMapping("/{id}")
    public FuncionResponse obtener(@PathVariable Long id) {
        return FuncionResponse.de(servicio.obtener(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
