package co.edu.uceva.numerico.calculo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Matriz de autorización de calculo-service (RA-05).
 * "Ejecutar un método sobre una función del catálogo": docente y estudiante.
 *
 *   /actuator/health   público (para los healthchecks de Docker)
 *   cualquier otra     docente o estudiante
 *   sin token          401
 *   token válido, sin rol de la aplicación   403
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
                        .anyRequest().hasAnyRole("docente", "estudiante"))
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
