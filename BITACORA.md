# Bitácora de decisiones de arquitectura — Núcleo

| Fecha | Decisión | Alternativas descartadas | Justificada contra | Quién                                                                                                         |
|---|---|---|---|---------------------------------------------------------------------------------------------------------------|
| 2026-10-10 | Reparto modular: 4 microservicios Spring Boot, módulo nativo en C, frontend Angular | Monolito; microservicios compartiendo una sola base de datos | RQ-02 Modificabilidad: agregar un método debe ser una operación local | Dylan Steven Bedón Ramírez (propuso y subió); apoyo: equipo de Programacion en Ambiente Web 2                 |
| 2026-10-10 | API Gateway reactivo (Spring Cloud Gateway + WebClient) | Servicio Spring MVC con RestClient. Descartado por hipótesis: la orquestación bloqueante podría agotar el pool de hilos bajo ráfagas concurrentes (por validar con medición) | Desempeño (escenario adicional de carga concurrente por redactar). Se asume el costo de un convertidor JWT reactivo propio en el gateway | Dylan Steven Bedón Ramírez (propuso y subió); apoyo: equipo de Programacion en Ambiente Web 2                 |
| 2026-10-26 | Dockerfile multietapa (gcc → Maven/JDK → JRE): la imagen final solo lleva libnumerico.so y el JAR | Compilar el C dentro de la imagen final (descartada: arrastraría el compilador; la etapa gcc:13 pesa 1.97 GB); compilar el .so en Windows y copiarlo (descartada: la biblioteca es solo de Linux y no carga en otro sistema) | RQ-03 Facilidad de despliegue | Dylan Steven Bedón Ramírez (implementó y subió); apoyo: Valentina Rosero Ocoro                                |
| 2026-10-26 | Java reserva el arreglo de iteraciones y C solo lo llena; una sola llamada JNA por corrida | C reserva con malloc y expone una función para liberar (descartada: obliga a liberar desde Java y deja riesgo de fugas entre lenguajes); una llamada JNA por iteración (descartada por hipótesis, sin medir: se supuso que cruzar la frontera Java↔C en cada paso cuesta más de lo que gana C; el enunciado pide medirla como variante B y sigue pendiente, ver «Pendientes al momento de la entrega») | RQ-01 Desempeño | Dylan Steven Bedón Ramírez (implementó y subió); apoyo: Jonatan David Montoya Posada                          |
| 2026-10-26 | Cargar la biblioteca con -Djna.library.path=/app/lib en el ENTRYPOINT y Native.load("numerico") perezoso | LD_LIBRARY_PATH y Native.load con ruta absoluta (descartadas por ahora: la ruta queda en una sola línea visible del ENTRYPOINT; no se midió diferencia entre ellas). Si el .so no está, el servicio arranca pero /calculo falla con UnsatisfiedLinkError | RQ-03 Facilidad de despliegue | Dylan Steven Bedón Ramírez (implementó y subió); apoyo: Jonatan David Montoya Posada                          |
| 2026-10-27 | Selector de motor por configuración (nucleo.motor = nativo o java); la línea base en Java pasa de src/test a src/main | Dejar la línea base solo en src/test sin selector (descartada: no permite medir RQ-01 con el mismo endpoint); un endpoint distinto por motor (descartada: duplica la superficie de la API) | RQ-01 Desempeño y RQ-02 Testabilidad | Dylan Steven Bedón Ramírez (implementó y subió); apoyo: Jonatan David Montoya Posada                          |
| 2026-11-06 | La autorización se valida en cada servicio (resource server con JWT), además del gateway | Validar solo en el gateway y dejar los servicios internos abiertos | RA-04: cada servicio debe poder defenderse solo si alguien llega a la red interna | Dylan Steven Bedón Ramírez (implementó y subió); revisión: Valentina Rosero Ocoro                             |
| 2026-11-06 | Los roles se leen del claim anidado `realm_access.roles` con un convertidor propio (`RolesRealmConverter`) | Declarar en Keycloak un mapper que publique los roles en el primer nivel del token | Un mapper deja la regla dentro de la configuración de Keycloak y es más difícil de probar; el convertidor se prueba con un test unitario | Dylan Steven Bedón Ramírez (implementó y subió); revisión: Valentina Rosero Ocoro                             |
| 2026-11-06 | El token debe traer la audiencia `numerico-api` (mapper en el cliente `numerico-web`) y cada servicio la comprueba | Aceptar cualquier token firmado por el realm | RA-04: rechazar un token con destinatario que no le corresponde | Dylan Steven Bedón Ramírez (implementó y subió); revisión: Valentina Rosero Ocoro                             |
| 2026-11-06 | Las funciones guardan `creadoPor` = claim `sub` del token | Guardar el nombre de usuario visible | RA-06: el `sub` es estable; el nombre puede cambiar | Dylan Steven Bedón Ramírez (implementó y subió); revisión: Valentina Rosero Ocoro                             |
| 2026-11-06 | Los coeficientes se guardan de menor a mayor grado, como el contrato de la corrida y la biblioteca en C | Mayor a menor, como el documento del profesor | Evitar invertir en cada llamada a C. **Pendiente confirmar con el profesor** y avisar a Brian (formulario de HU-01) | Dylan Steven Bedón Ramírez (implementó y subió); revisión: Valentina Rosero Ocoro                             |
| 2026-11-09 | calculo-service reemplaza su configuración de seguridad abierta por la misma de funcion-service (JWT, audiencia, roles de Keycloak); docente y estudiante pueden calcular | Dejarlo abierto porque solo lo llama el gateway | RA-04 y RA-05: ningún servicio confía ciegamente en quien lo llama | Dylan Steven Bedón Ramírez (implementó y subió); revisión: Valentina Rosero Ocoro                             |
| 2026-11-10 | En historial-service el dueño de la corrida sale del `sub` del token y no del cuerpo de la petición (`CorridaRequest` no tiene campo usuario); el historial propio se consulta por ese valor | Recibir el usuario en el cuerpo y filtrar por él (permite leer o registrar a nombre de otro) | RA-06: la identidad queda en los datos y no puede falsificarse desde el cliente | Dylan Steven Bedón Ramírez (implementó y subió); revisión: Valentina Rosero Ocoro                             |
| 2026-11-10 | historial-service guarda dos tiempos por corrida: `tiempoNativoMs` y `tiempoTotalMs`; `funcionId` es un número suelto, sin llave foránea | Un solo tiempo; llave foránea entre servicios (descartada: cada servicio tiene su propia base de datos) | RQ-01 (meta menor a 500 ms de extremo a extremo y menor a 50 ms en la parte nativa) y RQ-02 (servicios desacoplados) | Dylan Steven Bedón Ramírez (implementó y subió); revisión: Valentina Rosero Ocoro                             |
| 2026-11-10 | El gateway valida que el token sea válido (firma, vigencia, emisor y audiencia) pero no decide por rol; la autorización por rol la sigue haciendo cada servicio. La cabecera `Authorization` pasa intacta. No fue necesario un convertidor JWT reactivo de roles propio, como se había supuesto en la decisión del 2026-10-10 | Repetir la matriz de roles también en el gateway (duplica reglas y puede desincronizarse); quitar el token y reemplazarlo por otro entre servicios | RA-04 y RA-05: un 403 siempre lo responde el dueño de los datos | Dylan Steven Bedón Ramírez (implementó y subió); revisión: Valentina Rosero Ocoro                             |
| 2026-11-10 | Las rutas del gateway se declaran en Java (`RutasConfig`) y no en `application.yml` | Rutas en `application.yml` | El nombre de las propiedades de Spring Cloud Gateway cambió entre versiones; la forma en Java funciona en todas (RQ-02) | Dylan Steven Bedón Ramírez (implementó y subió); revisión: Valentina Rosero Ocoro                             |
| 2026-11-11 | En Docker el emisor del token (`http://localhost:8180/realms/numerico-realm`) y la dirección de las claves públicas (`http://keycloak:8080/...`) son dos valores distintos | Un solo valor para ambos (todos los servicios responderían 401: el emisor no coincidiría o las claves no se podrían descargar) | RA-04 y RQ-03 (despliegue con un solo comando) | Dylan Steven Bedón Ramírez (implementó y subió); revisión: Valentina Rosero Ocoro                             |
| 2026-11-11 | El `docker-compose.yml` completo usa como contexto de construcción la raíz del repo, igual que calculo-service, y las bases de datos no publican puertos | Contexto por módulo (Maven no resolvería el pom padre ni el reactor); publicar 5433 y 5434 (chocan con `docker-compose.dev.yml`) | RQ-03 Facilidad de despliegue | Dylan Steven Bedón Ramírez (implementó y subió); revisión: Valentina Rosero Ocoro                             |
| 2026-11-16 | La gráfica del error usa escala logarítmica en el eje vertical, con una línea de tolerancia; los errores menores o iguales a 0 no se dibujan | Escala lineal (el error baja varios órdenes de magnitud en pocas iteraciones y, en lineal, el tramo final de las curvas queda pegado al eje) | HU-05: la gráfica debe permitir ver cómo converge cada método y cuándo cruza la tolerancia | Dylan Steven Bedón Ramírez (implementó y subió); revisión: pendiente (Valentina Rosero Ocoro, Liseth Johanna Henao Cardona) |
| 2026-11-16 | La gráfica se dibuja en SVG propio (`grafica.ts` y `grafica-error.ts`), sin librería de gráficos | Chart.js o D3 (descartadas por ahora: una dependencia más en el build y en la imagen; la gráfica solo necesita ejes y líneas) | RQ-03 Facilidad de despliegue; la lógica se prueba con 11 pruebas en `grafica.spec.ts` | Dylan Steven Bedón Ramírez (implementó y subió); revisión: pendiente (Valentina Rosero Ocoro, Liseth Johanna Henao Cardona) |
| 2026-11-16 | La comparación de métodos se hace en el frontend: ejecuta bisección, Newton y secante una tras otra con `/calculo/{metodo}` y registra cada corrida en el historial | Un endpoint nuevo de comparación en el backend (descartado: obligaría a tocar calculo-service y el gateway). Costo asumido: 3 llamadas en lugar de 1 | HU-04 y RQ-02 Modificabilidad: el contrato existente se reutiliza sin cambios | Dylan Steven Bedón Ramírez (implementó y subió); revisión: pendiente (Valentina Rosero Ocoro, Brian Campo Villada) |
| 2026-11-16 | El frontend se empaqueta con Dockerfile multietapa (Node para construir, nginx para servir) y se agrega al `docker-compose.yml` en el puerto 4200 | Servir con `ng serve` dentro del contenedor (descartado: la imagen final llevaría Node y las herramientas de desarrollo) | RQ-03 Facilidad de despliegue: todo el sistema, frontend incluido, sube con un solo comando | Dylan Steven Bedón Ramírez (implementó y subió); revisión: pendiente (Valentina Rosero Ocoro) |
| 2026-11-17 | `docker-compose.yml` pasa `NUCLEO_MOTOR` al calculo-service con valor por defecto `nativo` | Editar el compose a mano cada vez que se quiera medir con Java; levantar un segundo contenedor con `docker run` en otro puerto | RQ-01 Desempeño: permite medir los dos motores por el mismo gateway sin cambiar código | Dylan Steven Bedón Ramírez (implementó y subió); revisión: pendiente (Valentina Rosero Ocoro) |
| 2026-11-17 | Los límites de CPU y memoria se declaran en `deploy.resources.limits` de `docker-compose.yml` y `docker-stack.yml`: 1 CPU y 512 MiB en los cuatro servicios Java, 1 CPU y 1,5 GiB en Keycloak, 0,5 CPU y 256 MiB en cada Postgres, 0,25 CPU y 64 MiB en el frontend | Sin límites (el escenario de RQ-01 exige 1 CPU y 512 MiB declarados en el manifiesto); Keycloak con 1 GiB (descartado: tras usar la aplicación marcó 920,8 MiB, el 89,9 % del límite) | RQ-01 Desempeño (se mide con el límite aplicado) y RQ-03 Facilidad de despliegue; el consumo observado está en la sección «Límites declarados y consumo observado». Los límites de Postgres, Keycloak y frontend los fijó el equipo, no el enunciado | Dylan Steven Bedón Ramírez (implementó y subió); revisión: pendiente (Valentina Rosero Ocoro) |
| 2026-11-17 | Además del compose se entrega `docker-stack.yml` (Docker Swarm) y `scripts/desplegar-swarm.ps1`. Swarm no construye imágenes (usa las `nucleo-*:latest` de `docker compose build`) y no respeta `depends_on`, así que cada servicio lleva `restart_policy: on-failure` | Kubernetes (descartado por tiempo: habría que reescribir los 8 servicios como manifiestos y contar con un clúster; Swarm reutiliza el formato del compose que ya funcionaba); entregar solo el compose (descartado: el enunciado nombra el YAML de Kubernetes o el stack de Swarm). **Pendiente confirmar con el profesor si el compose con límites basta** | RQ-03 Facilidad de despliegue: el sistema completo se levanta con un solo manifiesto | Dylan Steven Bedón Ramírez (implementó y subió); revisión: pendiente (Valentina Rosero Ocoro) |
| 2026-11-18 | El caso fijo de RQ-01 es Newton sobre x⁵ − 2 (`coef = -2,0,0,0,0,1`, de menor a mayor grado), x0 = 1,2, tolerancia 1e-10, máximo 100 iteraciones, con el contenedor de cálculo limitado a 1 CPU y 512 MiB y el historial con 500 corridas cargadas a nombre de `estudiante2`. Se repite con bisección en [0, 2] con tolerancia 1e-12 (41 iteraciones) y con una corrida que agota las 100 (bisección, tolerancia 1e-300) | Los casos pequeños de las mediciones anteriores (x²-4 y x^10-2), que no siguen el escenario; otro polinomio de grado 5 (se eligió x⁵ − 2 por tener una sola raíz real simple, 2^(1/5) ≈ 1,1487, y converger sin sorpresas) | RQ-01 Desempeño: el procedimiento del enunciado exige un caso fijo, un polinomio de grado 5 con x0 cercano a la raíz, y no cambiarlo entre mediciones. El caso se fijó tarde (el 2026-11-18) y no el primer día | Dylan Steven Bedón Ramírez (implementó y subió); revisión: pendiente (Valentina Rosero Ocoro) |

## Fase 1: corrección del esqueleto (-parameters)
El `pom.xml` padre importa Spring Boot como BOM y no hereda de `spring-boot-starter-parent`,
por lo que `-parameters` no estaba activo y `@RequestParam` fallaba con 500. Se agregó
`maven.compiler.parameters=true` en las propiedades del padre. Aplica a los 4 servicios.

## Mediciones RQ-03 (primera medición, imagen nucleo-calculo:fase4)

Medido el 2026-10-26 en el portátil de desarrollo, no en una máquina limpia.

| Dato | Valor | Meta |
|---|---|---|
| Tamaño comprimido (content size, docker images) | 141 MB (141 494 663 bytes) | menos de 250 MB: cumple |
| Tamaño en disco (disk usage, docker images) | 511 MB | menos de 250 MB: no cumple |
| Capas de la base (Ubuntu 117 MB + paquetes 58 MB + JRE 166 MB) | unos 341 MB | |
| app.jar | 29 MB | |
| libnumerico.so | 15,3 kB (corregido en la segunda medición) | |
| Etapa de compilación descartada (gcc:13) | 1.97 GB | |
| Arranque hasta la primera respuesta correcta (3 corridas) | 4,27 s, 4,13 s, 4,14 s | |
| Mediana del arranque | 4,14 s | menos de 90 s: cumple |

Análisis: más del 90 % del peso de la imagen es la base de Java; la biblioteca nativa casi no pesa. El criterio de tamaño depende de cuál columna de docker images se tome como válida (comprimido: cumple; en disco: no cumple), y se consulta con el profesor. Para bajar el tamaño en disco se evaluarán un runtime recortado con jlink o una base más liviana.

Pendiente: medir con --no-cache, en una máquina limpia, y la medición «antes» (imagen sin la biblioteca).

## Mediciones RQ-01 (primera medición: C vía JNA contra Java)

Medido el 2026-10-27 en el portátil de desarrollo (Docker Desktop), con la imagen nucleo-calculo:fase4b: dos contenedores, uno con motor nativo (puerto 8082) y otro con motor java (puerto 8084). Caso: bisección de x²-4 en [0,5], tol 1e-10, 36 iteraciones, 5 corridas por motor, endpoint GET /calculo/biseccion.

| Motor | Corridas internas (ms) | Mediana interna | Mediana total HTTP | Meta |
|---|---|---|---|---|
| C vía JNA | 26,80 / 1,81 / 0,62 / 0,70 / 0,52 | 0,70 ms | 27 ms | menos de 500 ms: cumple |
| Java | 1,63 / 0,014 / 0,016 / 0,019 / 0,015 | 0,016 ms | 22 ms | menos de 500 ms: cumple |

Análisis: ambos motores cumplen con holgura. Con este caso pequeño Java es más rápido por dentro porque cada llamada a C paga el cruce JNA (reserva y copia de un arreglo de 100 estructuras); el costo total lo domina HTTP y Spring. La primera corrida es más lenta en ambos (carga de la biblioteca en C, calentamiento de la JVM en Java).

Pendiente: repetir con carga mayor (polinomio de grado 10, muchas iteraciones, tol más estricta) para ver si la ventaja de C aparece, y medir en una máquina limpia.

## Mediciones RQ-02 (primera medición: suite de pruebas)

Medido el 2026-10-27 en el portátil de desarrollo (Docker Desktop). Suite: 43 comprobaciones en C (casos 1 a 11 del contrato, parámetros inválidos y RN-02) y 29 pruebas en Java (línea base, selector de motor y comparación C contra Java en los 11 casos).

| Medición | Resultado | Meta |
|---|---|---|
| Maven con caché caliente, 3 corridas | 4,7 s / 4,8 s / 5,0 s (mediana 4,8 s) | menos de 30 s: cumple |
| Pruebas en C (compilar y ejecutar) | 0,72 s | |
| Suite completa (Maven + C) | unos 5,5 s | menos de 30 s: cumple |
| Solo las pruebas de Java (tiempos por clase) | unos 0,5 s | |
| Primera corrida con el caché de Maven vacío | 53,6 s | solo descarga de dependencias |

Análisis: la suite cumple con holgura; el tiempo lo domina el arranque de Maven y de Docker, no las pruebas. La primera ejecución en una máquina sin dependencias descargadas supera los 30 s por la descarga de librerías; en el Dockerfile y en un pipeline conviene usar un caché de dependencias.

Pendiente: repetir en una máquina limpia.

## Mediciones RQ-03 (segunda medición: --no-cache, imagen nucleo-calculo:fase4c)

Medido el 2026-10-27 en el portátil de desarrollo, no en una máquina limpia. La imagen se construyó con docker build --no-cache.

| Dato | Valor | Meta |
|---|---|---|
| Compilador (gcc) en la imagen final | no existe | no debe haber: cumple |
| Archivos .c o .java en la imagen | ninguno | no debe haber fuentes: cumple |
| Contenido de /app | app.jar 28,9 MB (28 955 473 bytes) y lib/libnumerico.so 15 336 bytes | solo lo necesario: cumple |
| Tamaño comprimido (content size) | 141 MB | menos de 250 MB: cumple |
| Tamaño en disco (disk usage) | 511 MB | menos de 250 MB: no cumple |
| Arranque hasta la primera respuesta (3 corridas) | 3,21 s, 3,13 s, 3,09 s | |
| Mediana del arranque | 3,13 s | menos de 90 s: cumple |
| Construcción de la imagen con --no-cache | 99,8 s (Maven 95,6 s, gcc 0,3 s) | informativo |

Corrección: en la primera medición se anotó libnumerico.so en 28,7 kB; el archivo de esta imagen pesa 15 336 bytes (15,3 kB).

Análisis: la imagen cumple lo que se puede verificar sin discusión (sin compilador ni fuentes, contenido mínimo, arranque muy por debajo de 90 s). El tamaño depende de la columna que se tome: comprimido cumple y en disco no cumple, y se consulta con el profesor. Se aclara también si los 90 s se cuentan desde el arranque del contenedor (3,13 s) o incluyen la construcción sin caché (unos 103 s).

Pendiente: medición «antes» (imagen sin la biblioteca) y repetir en una máquina limpia.

## Verificación de seguridad (RA-04 a RA-07)

Evidencia generada con los scripts de `scripts/` y guardada en `docs/`. Todos los casos dieron lo esperado.

| Evidencia | Qué demuestra |
|---|---|
| `evidencia-auth.txt` (funcion-service) | 401 sin token y con firma alterada; 403 de estudiante en operaciones de docente; 200, 201 y 400 |
| `evidencia-auth-calculo.txt` (calculo-service) | 401 sin token y con firma alterada; 200 para docente y estudiante |
| `evidencia-auth-historial.txt` (historial-service) | 401, 201, 403 para estudiante en el historial completo, y el historial propio solo trae filas con su `sub` |
| `evidencia-auth-gateway.txt` (api-gateway) | El gateway corta lo inválido con 401 y deja pasar el token; el 403 lo responde cada servicio |
| `evidencia-stack-docker.txt` (sistema completo) | Los nueve casos por el puerto 8080, incluida la llamada al cálculo en C que desde IntelliJ en Windows no carga |

Pendiente: confirmar con el profesor el orden de los coeficientes (menor a mayor, como el contrato, o mayor a menor, como el documento).

## Mediciones RQ-01 (segunda medición: por el gateway, 30 corridas)

Medido el 2026-11-11 en el portátil de desarrollo (Docker Desktop), no en una máquina limpia. Sistema completo con docker compose, todas las llamadas por el api-gateway (puerto 8080) con token de docente. Por caso: 40 corridas, se descartan las 10 primeras y la estadística usa las 30 restantes. Script: scripts/medicion-rq01.ps1; salida completa en docs/medicion-rq01.txt. Todos los casos terminaron en CONVERGIO.

| Caso | Mediana total | p95 total | Mediana nativo | p95 nativo | Meta |
|---|---|---|---|---|---|
| Bisección x²-4 en [0,5], tol 1e-10 | 18,25 ms | 35,47 ms | 0,18 ms | 0,32 ms | total < 500 ms y nativo < 50 ms: cumple |
| Newton x²-4, x0=3, tol 1e-10 | 13,53 ms | 21,22 ms | 0,08 ms | 0,24 ms | cumple |
| Secante x²-4, x0=1, x1=3, tol 1e-10 | 12,30 ms | 35,02 ms | 0,08 ms | 0,10 ms | cumple |
| Bisección x^10-2 en [0,2], tol 1e-12 | 11,55 ms | 32,87 ms | 0,07 ms | 0,12 ms | cumple |

Análisis: el peor p95 total (35,47 ms) es unas 14 veces menor que la meta de 500 ms, y el peor p95 nativo (0,32 ms) es unas 150 veces menor que la de 50 ms. El tiempo total lo dominan el recorrido HTTP, la validación del token (en el gateway y otra vez en el servicio) y Spring; el cálculo en C pesa menos de 1 ms. El polinomio de grado 10 con tolerancia 1e-12 no es más lento que los casos pequeños, así que con estos tamaños la carga numérica no pesa; la medición con carga mayor que estaba pendiente no muestra diferencia. Respecto a la primera medición (directa al servicio, 27 ms de mediana total), la mediana aquí es menor porque ya se descartó el calentamiento, aunque el recorrido incluye una parada más (el gateway).

Límites: la primera corrida de cada caso (carga de la biblioteca en C, calentamiento de la JVM) se descartó por diseño, así que el arranque en frío no está cubierto por esta tabla. Con 30 muestras, el p95 es el valor número 29, es decir casi el máximo, por lo que es sensible a un pico aislado. El tiempo total incluye el proceso curl.

Pendiente: repetir el script tres veces y reportar la variación; medir en una máquina limpia. La comparación con NUCLEO_MOTOR=java se hizo el 2026-11-17 (sección siguiente).

## Mediciones RQ-01 (tercera medición: motor C contra motor Java, mismo día)

Medido el 2026-11-17 en el portátil de desarrollo (Docker Desktop), no en una máquina limpia. Mismo script y mismos casos que la segunda medición (por el gateway, 40 corridas, se descartan 10). El motor se cambió con `NUCLEO_MOTOR` (el `docker-compose.yml` lo pasa al servicio, con `nativo` por defecto); se comprobó con `printenv` dentro del contenedor. Primero se midió Java y después C, ambos el mismo día. Salidas en `docs/medicion-rq01-java-2026-11-17.txt` y `docs/medicion-rq01-nativo-2026-11-17.txt`.

| Caso | Motor | Mediana total | p95 total | Mediana nativo | p95 nativo |
|---|---|---|---|---|---|
| Bisección x²-4 en [0,5], tol 1e-10 | C (JNA) | 13,80 ms | 34,13 ms | 0,16 ms | 0,35 ms |
| Bisección x²-4 en [0,5], tol 1e-10 | Java | 12,46 ms | 31,07 ms | 0,01 ms | 0,01 ms |
| Newton x²-4, x0=3, tol 1e-10 | C (JNA) | 9,48 ms | 30,58 ms | 0,07 ms | 0,14 ms |
| Newton x²-4, x0=3, tol 1e-10 | Java | 11,51 ms | 27,92 ms | 0,01 ms | 0,01 ms |
| Secante x²-4, x0=1, x1=3, tol 1e-10 | C (JNA) | 10,20 ms | 28,30 ms | 0,07 ms | 0,10 ms |
| Secante x²-4, x0=1, x1=3, tol 1e-10 | Java | 10,41 ms | 34,01 ms | 0,01 ms | 0,01 ms |
| Bisección x^10-2 en [0,2], tol 1e-12 | C (JNA) | 8,55 ms | 27,64 ms | 0,06 ms | 0,09 ms |
| Bisección x^10-2 en [0,2], tol 1e-12 | Java | 9,55 ms | 34,87 ms | 0,01 ms | 0,03 ms |

Análisis: ambos motores cumplen RQ-01 con mucha holgura en los cuatro casos. En la parte nativa, Java es más rápido en todos (menos de 0,03 ms contra 0,06 a 0,35 ms); coincide con la primera medición: con cálculos tan pequeños, el cruce JNA (reservar y copiar el arreglo de iteraciones) pesa más que el propio cálculo. En el tiempo total no hay un ganador claro: las diferencias entre medianas son de 1 a 2 ms y cambian de signo entre casos y entre mediana y p95. Esa diferencia es menor que la variación entre días del propio motor C (en la bisección de x²-4 la mediana total fue 18,25 ms el 2026-11-11 y 13,80 ms hoy), así que no se puede atribuir a un motor. Con estos casos no se observó ventaja de rendimiento de C sobre Java; esa ventaja, si existe, no se midió.

Límites: el script redondea a 2 decimales, así que 0,01 ms es el mínimo que puede mostrar, no un valor exacto. Una sola corrida del script por motor, 30 muestras por caso (el p95 es casi el máximo). Es un portátil de desarrollo y no una máquina limpia. No se midió un caso con carga mucho mayor que el polinomio de grado 10 con tolerancia 1e-12, donde C podría ganar.

Pendiente: repetir el script tres veces por motor y reportar la variación; probar un caso con mucha más carga; medir en una máquina limpia.

## Límites declarados y consumo observado (RQ-01, RQ-03)

Medido el 2026-11-17 en el portátil de desarrollo (Docker Desktop), con `docker stats --no-stream` después de ejecutar cuatro métodos en la aplicación. Manifiesto: `docker-compose.yml` y `docker-stack.yml`; guía en `docs/despliegue.md`.

| Servicio | Límite | Uso observado | % del límite |
|---|---|---|---|
| frontend (nginx) | 0,25 CPU, 64 MiB | 15,85 MiB | 24,8 % |
| api-gateway | 1 CPU, 512 MiB | 206,5 MiB | 40,3 % |
| historial-service | 1 CPU, 512 MiB | 283 MiB | 55,3 % |
| funcion-service | 1 CPU, 512 MiB | 275,2 MiB | 53,8 % |
| calculo-service | 1 CPU, 512 MiB | 171,5 MiB | 33,5 % |
| postgres-historial | 0,5 CPU, 256 MiB | 41,95 MiB | 16,4 % |
| postgres-funcion | 0,5 CPU, 256 MiB | 38,55 MiB | 15,1 % |
| keycloak | 1 CPU, 1 GiB (primer valor) | 920,8 MiB | 89,9 % |
| keycloak | 1 CPU, 1,5 GiB (valor final) | 776,2 MiB (recién reiniciado) y 805,4 MiB (con Swarm) | 50,5 % y 52,4 % |

Análisis: con 1 GiB, Keycloak quedó casi al tope tras iniciar sesión, por eso se subió a 1,5 GiB. Los demás servicios quedan por debajo del 56 %. Es una sola lectura por servicio y la memoria de las JVM crece con el uso, así que no es un máximo.

Verificación con Docker Swarm: `.\scripts\desplegar-swarm.ps1` dejó los 8 servicios en 1/1, los límites aparecieron en `docker stats`, el inicio de sesión en `http://localhost:4200` funcionó y los 9 casos del script de evidencia dieron el código esperado (`docs/evidencia-stack-swarm.txt`). Una vez, justo después de desplegar el stack, el navegador mostró `ERR_EMPTY_RESPONSE` en `localhost:4200`; `curl` respondió 200 y al reintentar funcionó. No se determinó la causa.

## Mediciones RQ-01 (cuarta medición: procedimiento del enunciado, variantes A y C)

Medido el 2026-11-18 en el portátil de desarrollo (Docker Desktop), no en una máquina limpia. Por el gateway (puerto 8080) con token de docente, contenedor de cálculo con 1 CPU y 512 MiB (el script lo comprueba antes de medir) e historial con 500 corridas. Por caso: 40 corridas, se descartan las 10 primeras y la estadística usa las 30 restantes. Scripts: `scripts/cargar-historial.ps1` y `scripts/medicion-rq01-enunciado.ps1`; salidas completas en `docs/medicion-rq01-<variante>-<caso>.txt`. «Total» es `curl` de punta a punta; «cómputo» es `tiempoNativoMs` que reporta el servicio (reloj de Java alrededor del cálculo; en A, alrededor de la llamada JNA). Variante A: C por JNA con una llamada por corrida. Variante C: Java puro (`NUCLEO_MOTOR=java`). **La variante B (una llamada JNA por iteración) no está implementada ni medida.**

Segunda pasada, con el mismo protocolo para A y C (arranque del servicio, `newton5` una vez de calentamiento y otra vez para registrar, luego los otros dos casos):

| Caso | Iteraciones | Variante | Mediana total | p95 total | Mediana cómputo | p95 cómputo |
|---|---|---|---|---|---|---|
| `newton5`, corrida 1 | 5 | A | 17,20 ms | 34,49 ms | 0,211 ms | 1,046 ms |
| `newton5`, corrida 1 | 5 | C | 11,60 ms | 32,84 ms | 0,009 ms | 0,055 ms |
| `newton5`, corrida 2 | 5 | A | 14,65 ms | 32,40 ms | 0,130 ms | 0,227 ms |
| `newton5`, corrida 2 | 5 | C | 11,06 ms | 29,07 ms | 0,008 ms | 0,015 ms |
| `biseccion1e-12` | 41 | A | 14,53 ms | 32,91 ms | 0,120 ms | 0,213 ms |
| `biseccion1e-12` | 41 | C | 10,35 ms | 33,40 ms | 0,013 ms | 0,018 ms |
| `agota100` (NO_CONVERGIO) | 100 | A | 12,12 ms | 32,93 ms | 0,093 ms | 0,158 ms |
| `agota100` (NO_CONVERGIO) | 100 | C | 10,45 ms | 33,11 ms | 0,021 ms | 0,065 ms |

Primera pasada de la variante A (el servicio recién reiniciado; `newton5` fue la primera medición): `newton5` 19,28 ms / 34,40 ms de total y 0,183 ms / 0,541 ms de cómputo; `biseccion1e-12` 11,55 ms / 32,73 ms y 0,081 ms / 0,115 ms; `agota100` 11,73 ms / 28,94 ms y 0,083 ms / 0,115 ms (mediana / p95). En `docs/` quedan además otras dos salidas de `newton5` de esa pasada (`...-pasada1.txt`).

Análisis: ambas variantes cumplen RQ-01 con mucha holgura (el peor p95 total es 34,49 ms contra 800 ms, y el peor p95 de cómputo, 1,046 ms, contra 50 ms). En el cómputo, Java es más rápido que C por JNA en los cuatro casos, entre unas 4 y 16 veces (con las medianas de la corrida 2 de `newton5`, de `biseccion1e-12` y de `agota100`). En Java el cómputo crece con las iteraciones (0,008, 0,013 y 0,021 ms con 5, 41 y 100); en A no: baja de 0,211 a 0,130, 0,120 y 0,093 ms en el orden en que se midió, no por las iteraciones, lo que es compatible con un calentamiento que continúa después de las 10 corridas descartadas (hipótesis, no comprobada) sobre un costo fijo por llamada de unos 0,1 ms (preparar el buffer y cruzar JNA). En el tiempo total no hay un ganador: A y C difieren entre 1,7 y 4 ms de mediana, y A mismo varió 3 ms entre pasadas (11,55 y 14,53 ms en `biseccion1e-12`), así que esa diferencia no se puede atribuir a un motor. Con el tope de 100 iteraciones no se llegó a un punto donde C supere a Java; no se midió y no se afirma que exista.

Límites: tres relojes pide el enunciado y hay dos (curl y el reloj de Java); falta `clock_gettime` dentro de C. Solo dos pasadas de A y una de C, no tres. 30 muestras por caso (el p95 es casi el máximo), portátil de desarrollo, no una máquina limpia. El script redondea a 3 decimales el cómputo.

## Pendientes al momento de la entrega

- **Variante B** (JNA, una llamada por iteración) de RQ-01: no se implementó ni se midió. La decisión del 2026-10-26 que la descarta es una hipótesis, no un resultado.
- **Reloj dentro de C** (`clock_gettime`) y repetir cada medición tres veces para reportar la variación.
- **RQ-03:** medición «antes» (imagen sin la biblioteca) y arranque desde cero en una máquina limpia, tanto para RQ-01, RQ-02 como RQ-03. Nada de esto se probó fuera del portátil de desarrollo.
- **Preguntas al profesor sin respuesta:** orden de los coeficientes (menor a mayor, como el contrato, o mayor a menor, como el documento); cuál columna de tamaño vale en RQ-03 (comprimido 141 MB o en disco 511 MB) y desde cuándo se cuentan los 90 s; si el compose con límites basta como manifiesto o se exige Swarm o Kubernetes.
- **Jira:** HU-04, HU-05, HU-17 y SCRUM-40 estaban en revisión; SCRUM-40 no se cierra hasta tener la variante B.
