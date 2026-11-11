package co.edu.uceva.numerico.gateway.seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import java.util.List;

/**
 * El gateway también valida el token (firma, vigencia, emisor y audiencia): corta lo inválido en la
 * puerta. Aun así cada servicio vuelve a validarlo, porque ninguno confía ciegamente en quien le llama.
 * Esta es la versión reactiva (WebFlux) de la misma configuración de los otros servicios.
 */
@Configuration
public class JwtConfig {

    @Bean
    ReactiveJwtDecoder jwtDecoder(@Value("${nucleo.security.issuer}") String emisor,
                                  @Value("${nucleo.security.jwk-set-uri}") String jwkSetUri,
                                  @Value("${nucleo.security.audience}") String audiencia) {
        NimbusReactiveJwtDecoder decodificador = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri).build();

        OAuth2TokenValidator<Jwt> validaAudiencia = jwt -> {
            List<String> destinatarios = jwt.getAudience();
            if (destinatarios != null && destinatarios.contains(audiencia)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "El token no es para esta API", null));
        };

        decodificador.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(emisor), validaAudiencia));
        return decodificador;
    }
}
