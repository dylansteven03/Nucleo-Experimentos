package co.edu.uceva.numerico.funcion.seguridad;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.util.List;

/**
 * Este servicio valida el token por su cuenta (RA-04): firma contra el JWKS del realm,
 * vigencia, emisor y destinatario (audiencia).
 */
@Configuration
public class JwtConfig {

    @Bean
    JwtDecoder jwtDecoder(@Value("${nucleo.security.issuer}") String emisor,
                          @Value("${nucleo.security.jwk-set-uri}") String jwkSetUri,
                          @Value("${nucleo.security.audience}") String audiencia) {
        NimbusJwtDecoder decodificador = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

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
