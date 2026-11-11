# Avance: base del api-gateway (RA-02, RA-04, RA-05)

Antes `api-gateway` solo tenía el `pom.xml`. Esta base lo deja arrancable como **puerta única** (puerto 8080) hacia los tres
servicios, validando el token. **Es un punto de partida para Valentina: ella la completa y la ajusta.**

## Qué incluye
| Pieza | Para qué |
|---|---|
| `ApiGatewayApplication`, `application.yml` | Puerto 8080; URL de cada servicio y orígenes CORS por variable de entorno |
| `RutasConfig` | `/funciones/**` → 8081, `/calculo/**` → 8082, `/historial/**` → 8083, sin reescribir la ruta |
| `seguridad/JwtConfig` | Valida firma, vigencia, emisor y audiencia (versión reactiva de la de los otros servicios) |
| `seguridad/SecurityConfig` | Todo exige token, salvo `/actuator/health` y `OPTIONS`; CORS para `http://localhost:4200` |
| `scripts/evidencia-auth-gateway.ps1` | Genera `docs/evidencia-auth-gateway.txt` (10 casos de punta a punta) |

No se cambia el `pom.xml`: ya trae `spring-cloud-starter-gateway` y `oauth2-resource-server`.

## Decisiones
1. **El gateway valida que haya token válido; no decide por rol.** Quién puede qué (docente / estudiante) lo sigue decidiendo cada servicio. Así un 403 siempre viene del dueño de los datos, y el gateway no duplica la matriz.
2. **No hay que "configurar" el reenvío de `Authorization`**: Spring Cloud Gateway lo deja pasar tal cual. Cada servicio vuelve a validar el token por su cuenta (RA-04), sin confiar ciegamente en el gateway. El caso 5 del script lo demuestra: el 403 del estudiante en `POST /funciones` lo responde funcion-service, no el gateway.
3. **Las rutas están en Java (`RutasConfig`) y no en el `application.yml`**, porque el nombre de las propiedades de Spring Cloud Gateway cambió entre versiones; la forma en Java funciona en todas.
4. **CORS en el gateway.** El navegador hace una petición `OPTIONS` previa sin token; por eso se deja pasar sin autenticar.
5. **Fuera de alcance:** limitar peticiones, registro de accesos, balanceo.

## Pendiente
- Docker: en `docker-compose` el gateway debe recibir `FUNCION_URL`, `CALCULO_URL`, `HISTORIAL_URL` y `KEYCLOAK_ISSUER` / `KEYCLOAK_JWKS`. Ojo: dentro de Docker, `localhost` no es el host.
- Ajustar `CORS_ORIGENES` cuando exista el frontend desplegado.
- Con el gateway arriba, el frontend (Cristóbal, HU-13) debe llamar al 8080, no a cada servicio.

## Cómo aplicarlo
1. Copiar `api-gateway/src`, `scripts` y `AVANCE-gateway.md` a la raíz del repo (el `pom.xml` de `api-gateway` no cambia).
2. Recargar Maven.
3. `docker compose -f docker-compose.dev.yml up -d`; arrancar funcion-service, historial-service y ApiGatewayApplication.
4. `.\scripts\evidencia-auth-gateway.ps1` desde la raíz del repo.

## Aviso de honestidad
Este código **no se compiló ni se probó** al prepararlo (el entorno donde se escribió no alcanza Maven Central). No trae pruebas
unitarias: la evidencia es el script de punta a punta. Si algo no compila, el error se pega en el chat y se corrige.
