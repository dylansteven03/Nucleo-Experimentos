# Avance: medición de RQ-01 con el procedimiento del enunciado

## Qué trae
| Archivo | Para qué |
|---|---|
| `scripts/cargar-historial.ps1` | Deja el historial con al menos 500 corridas (el entorno del escenario), a nombre de `estudiante2`. Solo registra las que faltan. |
| `scripts/medicion-rq01-enunciado.ps1` | Mide un caso con una variante: 40 corridas, descarta 10, mediana y p95 de 30. Comprueba antes que el historial tenga 500 y que el contenedor de cálculo tenga los límites del manifiesto; si no, se detiene. |

## Los tres casos (no cambian entre mediciones)
| Caso | Qué es |
|---|---|
| `newton5` | Newton sobre x⁵ − 2, `coef = -2,0,0,0,0,1`, x₀ = 1,2, tol 1e-10, máximo 100 iteraciones. Es el caso fijo del escenario. |
| `biseccion1e-12` | Bisección sobre x⁵ − 2 en [0, 2], tol 1e-12, máximo 100 (del orden de 40 iteraciones). |
| `agota100` | Bisección sobre x⁵ − 2 en [0, 2], tol 1e-300, máximo 100: no puede converger, agota las 100. |

## Variantes
A = JNA con una llamada por corrida (`NUCLEO_MOTOR=nativo`, lo que hay hoy). C = Java puro (`NUCLEO_MOTOR=java`).
B = JNA con una llamada por iteración: **todavía no existe en el código**; requiere funciones de un solo paso en C y su interfaz en Java.

## Cómo medir
1. Sistema arriba con los límites del manifiesto: `docker compose up -d` (ver `docs/despliegue.md`).
2. `.\scripts\cargar-historial.ps1`
3. Variante A, tres casos:
   ```powershell
   .\scripts\medicion-rq01-enunciado.ps1 -Variante A -Caso newton5
   .\scripts\medicion-rq01-enunciado.ps1 -Variante A -Caso biseccion1e-12
   .\scripts\medicion-rq01-enunciado.ps1 -Variante A -Caso agota100
   ```
4. Variante C:
   ```powershell
   $env:NUCLEO_MOTOR = "java"
   docker compose up -d --force-recreate calculo-service
   ```
   Esperar 30 s, comprobar `docker compose exec calculo-service printenv NUCLEO_MOTOR` (debe decir `java`) y repetir los tres comandos con `-Variante C`.
5. Volver al motor nativo: `Remove-Item Env:NUCLEO_MOTOR` y `docker compose up -d --force-recreate calculo-service`.

La salida de cada medición queda en `docs\medicion-rq01-<variante>-<caso>.txt`.

## Qué mide cada reloj (y cuál falta)
- **Total:** `curl -w "%{time_total}"` contra el gateway (reloj de punta a punta).
- **Cómputo:** `tiempoNativoMs` que reporta calculo-service (reloj de Java alrededor del cálculo; en la variante A, alrededor de la llamada JNA).
- **Falta:** el reloj dentro de C (`clock_gettime`), que el enunciado pide para separar el costo del algoritmo del costo del marshalling.

## Aviso de honestidad
Los scripts no se pudieron ejecutar al prepararlos (no había PowerShell a mano): se revisaron a mano. Si fallan, se corrige.
El caso `agota100` depende de que la biblioteca acepte `tol = 1e-300`; la salida muestra el estado y las iteraciones de la última corrida para comprobar que llegó a 100.
