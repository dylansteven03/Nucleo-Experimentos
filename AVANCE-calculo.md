# Avance · seguridad de calculo-service

Reemplaza la `SecurityConfig` abierta (la temporal de la Fase 1) por la validación real con Keycloak.

| Archivo | Qué hace |
|---|---|
| `calculo-service/.../calculo/SecurityConfig.java` | **Reemplaza** el existente. Cualquier ruta pide rol docente o estudiante; `/actuator/health` queda público. |
| `calculo-service/.../calculo/JwtConfig.java` | Nuevo. Valida firma, emisor y audiencia `numerico-api`. |
| `calculo-service/.../calculo/RolesRealmConverter.java` | Nuevo. Lee los roles de `realm_access.roles`. |
| `calculo-service/src/test/.../RolesRealmConverterTest.java` | Nuevo. 3 pruebas. |
| `scripts/evidencia-auth-calculo.ps1` | Nuevo. Imprime 401, 401, 200, 200 y guarda `docs/evidencia-auth-calculo.txt`. |

No hace falta tocar `application.yml`: los valores de Keycloak tienen valores por defecto de desarrollo.

## Importante
Desde ahora **todas las llamadas a calculo-service necesitan token**. Si una prueba o pantalla llamaba `/calculo/...` sin token, va a recibir 401. El api-gateway (HU-14, parte de Valentina) debe reenviar la cabecera `Authorization` a este servicio.
