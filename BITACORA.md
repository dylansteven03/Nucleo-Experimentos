# Bitácora de decisiones de arquitectura — Núcleo

| Fecha | Decisión | Alternativas descartadas | Justificada contra | Quién |
|---|---|---|---|---|
| 2026-10-10 | Reparto modular: 4 microservicios Spring Boot, módulo nativo en C, frontend Angular | Monolito; microservicios compartiendo una sola base de datos | RQ-02 Modificabilidad: agregar un método debe ser una operación local | Dylan Steven Bedón Ramírez |
| 2026-10-10 | API Gateway reactivo (Spring Cloud Gateway + WebClient) | Servicio Spring MVC con RestClient. Descartado por hipótesis: la orquestación bloqueante podría agotar el pool de hilos bajo ráfagas concurrentes (por validar con medición) | Desempeño (escenario adicional de carga concurrente por redactar). Se asume el costo de un convertidor JWT reactivo propio en el gateway | Dylan Steven Bedón Ramírez |
| 2026-10-26 | Dockerfile multietapa (gcc → Maven/JDK → JRE): la imagen final solo lleva libnumerico.so y el JAR | Compilar el C dentro de la imagen final (descartada: arrastraría el compilador; la etapa gcc:13 pesa 1.97 GB); compilar el .so en Windows y copiarlo (descartada: la biblioteca es solo de Linux y no carga en otro sistema) | RQ-03 Facilidad de despliegue | Dylan Steven Bedón Ramírez |
| 2026-10-26 | Java reserva el arreglo de iteraciones y C solo lo llena; una sola llamada JNA por corrida | C reserva con malloc y expone una función para liberar (descartada: obliga a liberar desde Java y deja riesgo de fugas entre lenguajes); una llamada JNA por iteración (descartada: cruzar la frontera Java↔C cada paso cuesta más de lo que gana C) | RQ-01 Desempeño | Dylan Steven Bedón Ramírez |
| 2026-10-26 | Cargar la biblioteca con -Djna.library.path=/app/lib en el ENTRYPOINT y Native.load("numerico") perezoso | LD_LIBRARY_PATH y Native.load con ruta absoluta (descartadas por ahora: la ruta queda en una sola línea visible del ENTRYPOINT; no se midió diferencia entre ellas). Si el .so no está, el servicio arranca pero /calculo falla con UnsatisfiedLinkError | RQ-03 Facilidad de despliegue | Dylan Steven Bedón Ramírez |

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