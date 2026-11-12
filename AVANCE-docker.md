# Avance: sistema completo en Docker (Fase 9)

`docker compose up -d --build` levanta TODO con un solo comando: Keycloak, las dos bases de datos, funcion-service,
historial-service, calculo-service (con la librería en C) y el api-gateway como puerta única (8080).

## Qué incluye
| Archivo | Para qué |
|---|---|
| `docker-compose.yml` (raíz) | El sistema completo. `docker-compose.dev.yml` sigue existiendo, para desarrollar desde IntelliJ |
| `funcion-service/Dockerfile`, `historial-service/Dockerfile`, `api-gateway/Dockerfile` | Construyen cada servicio (Maven en una etapa, JRE 21 en la final) |
| `scripts/evidencia-stack-docker.ps1` | 9 casos por el gateway, incluida la llamada a C (`/calculo/ping`) que desde IntelliJ en Windows no funciona |

`calculo-service/Dockerfile` ya existía y no se toca.

## Decisiones
1. **Emisor y claves son dos valores distintos.** El usuario pide el token en `http://localhost:8180`, así que el `iss` es `http://localhost:8180/realms/numerico-realm`. Pero dentro de Docker `localhost` no es Keycloak: los servicios descargan las claves desde `http://keycloak:8080`. Sin esta separación, todos darían 401.
2. **Las bases de datos no publican puertos** en este compose, para no chocar con `docker-compose.dev.yml` (5433 y 5434).
3. **Contexto de construcción = raíz del repo**, como calculo-service, porque Maven necesita el pom padre y los pom de los módulos.
4. **Sin pruebas en la construcción** (`-DskipTests`): las pruebas se corren en IntelliJ y en la integración continua.

## Cómo usarlo
1. Apague lo que use los mismos puertos: `docker compose -f docker-compose.dev.yml down` y detenga los servicios de IntelliJ.
2. `docker compose up -d --build` (la primera vez tarda varios minutos).
3. Espere ~1 minuto. `docker compose ps` debe mostrar todo en `running`.
4. `.\scripts\evidencia-stack-docker.ps1`.
5. Para parar: `docker compose down`.

## Aviso de honestidad
Estos archivos **no se probaron** al prepararlos (el entorno donde se escribieron no tiene Docker ni Maven Central). Es el primer
arranque completo del sistema, así que es normal que aparezca algún ajuste; el error se pega en el chat y se corrige.
