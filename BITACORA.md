# Bitácora de decisiones de arquitectura — Núcleo

| Fecha | Decisión | Alternativas descartadas | Justificada contra | Quién |
|---|---|---|---|---|
| 2026-10-10 | Reparto modular: 4 microservicios Spring Boot, módulo nativo en C, frontend Angular | Monolito; microservicios compartiendo una sola base de datos | RQ-02 Modificabilidad: agregar un método debe ser una operación local | Dylan Steven Bedón Ramírez (propuso y subió); apoyo: equipo de Programacion en Ambiente Web 2 |
| 2026-10-10 | API Gateway reactivo (Spring Cloud Gateway + WebClient) | Servicio Spring MVC con RestClient. Descartado por hipótesis: la orquestación bloqueante podría agotar el pool de hilos bajo ráfagas concurrentes (por validar con medición) | Desempeño (escenario adicional de carga concurrente por redactar). Se asume el costo de un convertidor JWT reactivo propio en el gateway | Dylan Steven Bedón Ramírez (propuso y subió); apoyo: equipo de Programacion en Ambiente Web 2 |
| 2026-10-26 | Dockerfile multietapa (gcc → Maven/JDK → JRE): la imagen final solo lleva libnumerico.so y el JAR | Compilar el C dentro de la imagen final (descartada: arrastraría el compilador; la etapa gcc:13 pesa 1.97 GB); compilar el .so en Windows y copiarlo (descartada: la biblioteca es solo de Linux y no carga en otro sistema) | RQ-03 Facilidad de despliegue | Dylan Steven Bedón Ramírez (implementó y subió); apoyo: Valentina Rosero Ocoro |
| 2026-10-26 | Java reserva el arreglo de iteraciones y C solo lo llena; una sola llamada JNA por corrida | C reserva con malloc y expone una función para liberar (descartada: obliga a liberar desde Java y deja riesgo de fugas entre lenguajes); una llamada JNA por iteración (descartada: cruzar la frontera Java↔C cada paso cuesta más de lo que gana C) | RQ-01 Desempeño | Dylan Steven Bedón Ramírez (implementó y subió); apoyo: Jonatan David Montoya Posada |
| 2026-10-26 | Cargar la biblioteca con -Djna.library.path=/app/lib en el ENTRYPOINT y Native.load("numerico") perezoso | LD_LIBRARY_PATH y Native.load con ruta absoluta (descartadas por ahora: la ruta queda en una sola línea visible del ENTRYPOINT; no se midió diferencia entre ellas). Si el .so no está, el servicio arranca pero /calculo falla con UnsatisfiedLinkError | RQ-03 Facilidad de despliegue | Dylan Steven Bedón Ramírez (implementó y subió); apoyo: Jonatan David Montoya Posada |
| 2026-10-27 | Selector de motor por configuración (nucleo.motor = nativo o java); la línea base en Java pasa de src/test a src/main | Dejar la línea base solo en src/test sin selector (descartada: no permite medir RQ-01 con el mismo endpoint); un endpoint distinto por motor (descartada: duplica la superficie de la API) | RQ-01 Desempeño y RQ-02 Testabilidad | Dylan Steven Bedón Ramírez (implementó y subió); apoyo: Jonatan David Montoya Posada |

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
| libnumerico.so | 28.7 kB | |
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