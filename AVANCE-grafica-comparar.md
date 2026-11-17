# Avance: gráfica del error (HU-05), comparar métodos (HU-04) y frontend en Docker

## Qué incluye
| Archivo | Qué hace |
|---|---|
| `src/app/grafica.ts` | Lógica pura de la gráfica: encuentra las columnas de error e iteración, descarta errores no positivos, calcula la escala logarítmica |
| `src/app/componentes/grafica-error.ts` | Componente `<app-grafica-error>`: error por iteración en SVG puro (sin librerías), una o varias curvas, línea de tolerancia |
| `src/app/paginas/ejecutar.ts` | **Reemplaza** el anterior: ahora muestra la gráfica de la corrida (HU-05) |
| `src/app/paginas/comparar.ts` | Página nueva `/comparar`: bisección, Newton y secante sobre la misma función, uno tras otro, con tabla comparativa y las tres curvas en la misma gráfica (HU-04) |
| `src/app/app.ts`, `app.routes.ts`, `models.ts` | **Reemplazan** a los anteriores: enlace y ruta de Comparar, y el tipo `SerieError` |
| `src/app/grafica.spec.ts` | 11 pruebas nuevas de la gráfica |
| `frontend/Dockerfile`, `nginx.conf`, `.dockerignore` | Imagen del frontend (compila con Node 24, sirve con nginx) |
| `docker-compose.yml` (raíz) | **Reemplaza** al anterior: suma el servicio `frontend` en http://localhost:4200 |

## Decisiones
1. **Escala logarítmica en el eje del error.** Un método que converge bien dibuja una recta que baja; en escala lineal casi todo se vería pegado al eje.
2. **La columna del error se busca por nombre** (la que empieza por "err"), porque las columnas las define el backend. Si no existe, la gráfica dice que no hay datos. Si en `FilaIteracion` la columna se llama de otra forma, se ajusta en `grafica.ts`.
3. **Los errores exactamente 0 no se dibujan** (el logaritmo no existe). Lo dice el texto bajo la gráfica.
4. **La comparación corre los métodos en secuencia, no en paralelo**, para que el tiempo de cada uno no se contamine con los otros. Cada corrida también queda en el historial (HU-08).
5. **Valores iniciales de la comparación:** bisección con [a, b] de la función, secante con x0 = a y x1 = b, y Newton desde el punto medio. Los tres campos se pueden editar.
6. **El frontend en Docker** sirve el mismo código que `npm start`, por el puerto 4200 (el cliente `numerico-web` de Keycloak ya permite `http://localhost:4200/*`, y el gateway ya permite ese origen).

## Cómo aplicarlo
1. Copiar `frontend/` del zip sobre la carpeta `frontend` del proyecto, reemplazando los archivos que pregunte (`app.ts`, `app.routes.ts`, `models.ts`, `ejecutar.ts`).
2. Copiar `docker-compose.yml` a la raíz del repo, reemplazando el anterior, y `AVANCE-grafica-comparar.md` a la raíz.
3. Probar el frontend: `cd frontend`, `npm start` (con el backend arriba).
4. Probar todo en Docker: parar `npm start`, y en la raíz `docker compose up -d --build`; abrir http://localhost:4200.

## Aviso de honestidad
La gráfica, la comparación y la página de Ejecutar compilan (`ng build`), y las 22 pruebas unitarias pasan. Se vieron funcionar en un navegador real, pero con
**Keycloak y el gateway simulados**, con datos inventados. **No se probaron contra el sistema real**, y **la imagen Docker del frontend no se pudo construir
en el entorno donde se escribió** (no tiene Docker). Ese es el primer paso al aplicarlo: si `docker compose up --build` falla en el servicio `frontend`, el error se pega en el chat.
