package co.edu.uceva.numerico.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Rutas hacia los tres servicios. Cada servicio ya expone su propio prefijo (/funciones, /calculo,
 * /historial), así que no se reescribe la ruta. El gateway NO quita la cabecera Authorization: llega
 * intacta a cada servicio, que valida el token por su cuenta (RA-04).
 *
 * Se declaran en Java y no en application.yml porque el nombre de las propiedades de Spring Cloud
 * Gateway cambió entre versiones; esta forma funciona en todas.
 */
@Configuration
public class RutasConfig {

    @Bean
    RouteLocator rutas(RouteLocatorBuilder builder,
                       @Value("${nucleo.rutas.funcion}") String funcion,
                       @Value("${nucleo.rutas.calculo}") String calculo,
                       @Value("${nucleo.rutas.historial}") String historial) {
        return builder.routes()
                .route("funcion-service", r -> r.path("/funciones", "/funciones/**").uri(funcion))
                .route("calculo-service", r -> r.path("/calculo/**").uri(calculo))
                .route("historial-service", r -> r.path("/historial", "/historial/**").uri(historial))
                .build();
    }
}
