package co.edu.uceva.numerico.funcion.seguridad;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** El error que más tiempo consume: los roles de Keycloak vienen anidados en realm_access.roles. */
class RolesRealmConverterTest {

    private final RolesRealmConverter convertidor = new RolesRealmConverter();

    private Jwt tokenCon(Map<String, Object> claims) {
        Jwt.Builder b = Jwt.withTokenValue("t").header("alg", "none").subject("sub-1");
        claims.forEach(b::claim);
        return b.build();
    }

    @Test
    void convierteRolesAnidadosEnAutoridades() {
        Jwt jwt = tokenCon(Map.of("realm_access", Map.of("roles", List.of("docente", "offline_access"))));

        Collection<GrantedAuthority> autoridades = convertidor.convert(jwt);

        assertTrue(autoridades.stream().anyMatch(a -> a.getAuthority().equals("ROLE_docente")));
        assertEquals(2, autoridades.size());
    }

    @Test
    void sinRealmAccessNoHayAutoridades() {
        Jwt jwt = tokenCon(Map.of("scope", "openid"));

        assertTrue(convertidor.convert(jwt).isEmpty());
    }

    @Test
    void realmAccessSinRolesNoHayAutoridades() {
        Jwt jwt = tokenCon(Map.of("realm_access", Map.of("otro", "x")));

        assertTrue(convertidor.convert(jwt).isEmpty());
    }
}
