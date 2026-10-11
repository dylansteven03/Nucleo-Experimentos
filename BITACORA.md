# Bitácora de decisiones de arquitectura — Núcleo

| Fecha | Decisión | Alternativas descartadas | Justificada contra | Quién |
|---|---|---|---|---|
| 2026-10-10 | Reparto modular: 4 microservicios Spring Boot, módulo nativo en C, frontend Angular | Monolito; microservicios compartiendo una sola base de datos | RQ-02 Modificabilidad: agregar un método debe ser una operación local | Dylan Steven Bedón Ramírez |
| 2026-10-10 | API Gateway reactivo (Spring Cloud Gateway + WebClient) | Servicio Spring MVC con RestClient. Descartado por hipótesis: la orquestación bloqueante podría agotar el pool de hilos bajo ráfagas concurrentes (por validar con medición) | Desempeño (escenario adicional de carga concurrente por redactar). Se asume el costo de un convertidor JWT reactivo propio en el gateway | Dylan Steven Bedón Ramírez |
