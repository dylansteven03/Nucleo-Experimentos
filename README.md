# Núcleo · Banco de Métodos Numéricos

Plataforma académica distribuida para analizar y comparar métodos de aproximación de raíces polinómicas (bisección, Newton-Raphson y secante). El cálculo corre en una biblioteca en C (`libnumerico.so`) invocada desde Java con JNA. Se evalúa desde dos cursos: Programación en Ambiente Web 2 (aplicación completa) y Arquitectura de Computadores (componente nativo y despliegue).

## Arranque (un solo comando)

Requisito: **Docker** (con Docker Compose). No hacen falta JDK, Maven, gcc ni Node: todo se construye dentro de las imágenes.

```bash
docker compose up -d --build
```

La primera vez tarda varios minutos (construye las imágenes). Después, espere cerca de un minuto a que Keycloak y los servicios terminen de arrancar. `docker compose ps` debe mostrar todo en `Up`.

| Qué | Dirección |
|---|---|
| Aplicación (Angular) | http://localhost:4200 |
| API (api-gateway, puerta única) | http://localhost:8080 |
| Keycloak | http://localhost:8180 |

Usuarios de prueba del realm `numerico-realm`:

| Usuario | Clave | Rol |
|---|---|---|
| `docente1` | `docente123` | docente: administra el catálogo y ve todo el historial |
| `estudiante1` | `estudiante123` | estudiante: ejecuta métodos y ve solo su historial |
| `estudiante2` | `estudiante123` | estudiante |

Parar: `docker compose down` (con `-v` también borra los datos).

Antes de arrancar, cierre cualquier servicio que tenga corriendo en el IDE o `npm start`: usan los mismos puertos (4200, 8080 a 8083, 8180). Las contraseñas de las bases de datos y de Keycloak salen de variables de entorno con valores por defecto (`.env.example`); para otras, copie ese archivo a `.env`.

## Arquitectura

| Carpeta | Contenido |
|---|---|
| `api-gateway/` | Puerta única: valida el token y enruta a los servicios (Spring Cloud Gateway, reactivo) |
| `funcion-service/` | Catálogo de funciones (PostgreSQL) |
| `calculo-service/` | Invoca la biblioteca en C por JNA; también tiene un motor de referencia en Java |
| `historial-service/` | Persistencia de corridas con sus dos tiempos (PostgreSQL) |
| `native/` | Código C de `libnumerico.so`, su cabecera y sus pruebas |
| `frontend/` | Angular, servido con nginx en Docker |
| `keycloak/` | Realm importable (roles `docente` y `estudiante`, cliente `numerico-web`) |
| `docs/` | Contrato, evidencias, mediciones y guía de despliegue |
| `scripts/` | Scripts de evidencia, medición y despliegue (PowerShell) |

Seguridad: Keycloak con Authorization Code + PKCE; cada servicio valida firma, emisor y audiencia del token, y la autorización por rol la decide el servicio dueño de los datos.

## Motor de cálculo

`calculo-service` elige el motor con la variable `NUCLEO_MOTOR`: `nativo` (por defecto, C por JNA) o `java` (línea base). Se usa para comparar los dos en RQ-01:

```bash
NUCLEO_MOTOR=java docker compose up -d --force-recreate calculo-service
```

Los coeficientes de los polinomios van **de menor a mayor grado**: `x^2 - 4` se escribe `[-4, 0, 1]`. (Pendiente de confirmar con el profesor; ver `BITACORA.md`.)

## Despliegue y límites de recursos

`docker-compose.yml` declara los límites de CPU y memoria de todos los servicios, y `docker-stack.yml` es la versión para Docker Swarm. Detalle, comandos y cómo medir el arranque desde cero en [docs/despliegue.md](docs/despliegue.md).

## Pruebas

- Código en C: `make -C native test` (requiere gcc, en Linux o dentro de un contenedor).
- Servicios Java: `mvn test`. Las pruebas del cálculo comparan C contra Java y necesitan `libnumerico.so` compilada, así que se corren en Linux o dentro de Docker; en Windows no cargan la biblioteca.
- Frontend: `npm test -- --watch=false` dentro de `frontend/` (Node 24.15 o superior).

## Evidencias y mediciones

Todo lo medido y las decisiones de diseño con sus alternativas están en [BITACORA.md](BITACORA.md). Los datos crudos están en `docs/`:

- `evidencia-*.txt`: respuestas 401, 403 y 200 de cada servicio, del gateway y del sistema completo (Compose y Swarm).
- `medicion-rq01-*.txt`: tiempos de RQ-01 (C por JNA contra Java).
- `contrato-corrida.md`: formato de entrada y salida de una corrida.

Scripts útiles (PowerShell, desde la raíz del repo): `scripts/cargar-historial.ps1` (deja 500 corridas en el historial), `scripts/medicion-rq01-enunciado.ps1` (mide un caso con una variante), `scripts/vigencia-token.ps1` (baja la vigencia del token a 60 s para probar la renovación de sesión) y `scripts/desplegar-swarm.ps1`.

## Estado y límites conocidos

- La variante B de RQ-01 (una llamada JNA por iteración) **no está implementada ni medida**; tampoco el reloj dentro de C.
- Las mediciones se hicieron en un portátil de desarrollo, no en una máquina limpia; el arranque desde cero en otra máquina no se probó.
- Preguntas abiertas con el profesor: orden de los coeficientes, columna de tamaño de imagen válida en RQ-03 y si el compose con límites basta como manifiesto.

## Equipo

Valentina Rosero Ocoro, Jonatan David Montoya Posada, Liseth Johanna Henao Cardona, Juan David Torres Vásquez, Cristóbal Moreno Montoya, Brian Campo Villada y Dylan Steven Bedón Ramírez. UCEVA, 5.º semestre de Ingeniería de Sistemas.
