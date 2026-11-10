# Avance: base de historial-service (RF-07, RF-08, RA-04, RA-05, RA-06)

Antes `historial-service` solo tenía el `pom.xml`. Esta base lo deja arrancable, con seguridad y con el modelo
de datos para guardar corridas y sus tiempos. **Es un punto de partida para Jonatan (HU-07, HU-16) y Dylan (HU-08):
cada uno la completa.**

## Qué incluye
| Pieza | Para qué |
|---|---|
| `HistorialServiceApplication`, `application.yml` | Puerto 8083, base de datos en el 5434 (la del `docker-compose.dev.yml`) |
| `modelo/Corrida`, `CorridaRepository` | Corrida con método, convergencia, raíz, iteraciones, `tiempoNativoMs`, `tiempoTotalMs` y `usuario` (el `sub` del token) |
| `web/CorridaRequest` | Validación: campos obligatorios, tiempos ≥ 0, si convergió debe traer raíz |
| `web/HistorialService`, `HistorialController` | `POST /historial`, `GET /historial/mio`, `GET /historial` |
| `web/ManejadorErrores` | Errores 400 con el mismo formato de funcion-service |
| `seguridad/*` | Igual que calculo-service y funcion-service: JWT validado por este servicio (firma, emisor, audiencia) y roles de Keycloak |
| `scripts/evidencia-auth-historial.ps1` | Genera `docs/evidencia-auth-historial.txt` (9 casos) |
| `pom.xml` | **Cambio:** se agrega `spring-boot-starter-validation` (sin ella `@Valid` no existe) |

## Matriz de autorización
| Endpoint | docente | estudiante | sin token |
|---|---|---|---|
| `POST /historial` | 201 | 201 | 401 |
| `GET /historial/mio` | 200 | 200 | 401 |
| `GET /historial` | 200 | **403** | 401 |

## Decisiones
1. **El dueño de la corrida sale del token, no del cuerpo.** `CorridaRequest` no tiene campo `usuario`; el servicio guarda `jwt.getSubject()`. Así nadie puede registrar ni leer corridas a nombre de otro (RA-06).
2. **Se registran dos tiempos**: el nativo (RQ-01: < 50 ms) y el total de extremo a extremo (RQ-01: < 500 ms). La medición de verdad (30 corridas, descartar 10, mediana y p95) la hace HU-08; aquí solo está donde guardarlos.
3. **`funcionId` es un número suelto**, no una llave foránea, porque funcion-service y historial-service tienen bases separadas.
4. **`ddl-auto: update` es solo para desarrollo.**

## Pendiente (lo completa cada responsable)
- Jonatan: filtros del historial (por función, por método), paginación, orden; y que el frontend registre la corrida al terminar cada ejecución.
- Dylan (HU-08): que los tiempos que llegan sean los medidos de verdad.
- Valentina: que el gateway reenvíe `Authorization` hacia este servicio.

## Cómo aplicarlo
1. Copiar las carpetas del zip a la raíz del repo, **reemplazando** `historial-service/pom.xml`.
2. Recargar Maven en IntelliJ.
3. `docker compose -f docker-compose.dev.yml up -d`, y ejecutar `HistorialServiceApplication`.
4. `.\scripts\evidencia-auth-historial.ps1` desde la raíz del repo.

## Aviso de honestidad
Este código **no se compiló ni se probó** al prepararlo (el entorno donde se escribió no alcanza Maven Central). Las 14 pruebas
unitarias (3 + 7 + 4) y el script están por ejecutar en su máquina: si algo no compila, el error se pega en el chat y se corrige.
