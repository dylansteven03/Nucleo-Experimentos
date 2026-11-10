package co.edu.uceva.numerico.calculo;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Keycloak publica los roles del realm ANIDADOS en el claim realm_access.roles.
 * Spring, por defecto, busca un claim plano ("scope"/"scp"), así que sin este convertidor
 * el usuario queda autenticado pero sin autoridades y todo responde 403.
 * Cada rol "docente" pasa a ser la autoridad ROLE_docente.
 * (Misma clase que en funcion-service: cada servicio valida por su cuenta, decisión de la bitácora.)
 */
public class RolesRealmConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Object acceso = jwt.getClaim("realm_access");
        if (!(acceso instanceof Map<?, ?> mapa)) {
            return List.of();
        }
        Object roles = mapa.get("roles");
        if (!(roles instanceof Collection<?> lista)) {
            return List.of();
        }
        return lista.stream()
                .filter(String.class::isInstance)
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r))
                .toList();
    }
}
