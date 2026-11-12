/**
 * Direcciones del sistema. TODAS las llamadas al backend van por el api-gateway (puerto 8080);
 * el frontend nunca habla directo con un microservicio.
 * Keycloak: realm numerico-realm, cliente público numerico-web (flujo Authorization Code + PKCE).
 */
export const CONFIG = {
  apiUrl: 'http://localhost:8080',
  keycloakUrl: 'http://localhost:8180',
  realm: 'numerico-realm',
  clientId: 'numerico-web',
};
