package co.edu.uceva.numerico.funcion.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Matriz de autorización de funcion-service (RA-05). La autorización se declara aquí,
 * en el backend, no en el frontend.
 *
 *   POST   /funciones        docente
 *   DELETE /funciones/{id}   docente
 *   GET    /funciones[/id]   docente, estudiante
 *   sin token                401
 *   token válido, rol malo   403
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtDecoder decodificador) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/funciones").hasRole("docente")
                        .requestMatchers(HttpMethod.DELETE, "/funciones/**").hasRole("docente")
                        .requestMatchers(HttpMethod.GET, "/funciones", "/funciones/**")
                        .hasAnyRole("docente", "estudiante")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(o -> o.jwt(j -> j
                        .decoder(decodificador)
                        .jwtAuthenticationConverter(convertidor())));
        return http.build();
    }

    private JwtAuthenticationConverter convertidor() {
        JwtAuthenticationConverter convertidor = new JwtAuthenticationConverter();
        convertidor.setJwtGrantedAuthoritiesConverter(new RolesRealmConverter());
        return convertidor;
    }
}
