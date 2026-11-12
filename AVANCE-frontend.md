# Avance: base del frontend Angular (HU-01, 02, 03, 06, 07, 08, 12, 13, 17)

La carpeta `frontend/` solo tenía un `.gitkeep`. Esta base es una aplicación Angular 22 que ya hace el recorrido completo:
inicia sesión con Keycloak, manda el token en cada petición y llama al backend **solo por el api-gateway (8080)**.
**Es un punto de partida para Cristóbal (HU-12, 13) y Brian (HU-01); cada uno la completa y la ajusta.**

## Qué incluye
| Pieza | Para qué |
|---|---|
| `core/auth.service.ts` | Login con Keycloak, flujo Authorization Code + PKCE (RA-02); roles desde `realm_access.roles`; renueva el token si le quedan menos de 30 s y cierra sesión (RA-07) |
| `core/auth.interceptor.ts` | Pone `Authorization: Bearer` en cada petición al gateway, y solo a él (RA-03) |
| `core/api.service.ts` | Llamadas a `/funciones`, `/calculo/*` y `/historial` |
| `paginas/funciones.ts` | Catálogo de funciones; el docente además ve el formulario de registro y el botón Eliminar (HU-01) |
| `paginas/ejecutar.ts` | Elige función y método, ejecuta, muestra la raíz o la causa de no convergencia y la tabla de iteraciones (HU-02, 03, 06); registra la corrida con sus dos tiempos (HU-08) |
| `paginas/historial.ts` | "Mi historial" para todos y "Todo el historial" solo para el docente (HU-07) |
| `config.ts` | Direcciones del gateway y de Keycloak |
| `util.ts` + `util.spec.ts` | Lectura de coeficientes, formato del polinomio, traducción de errores; 11 pruebas |

## Cómo correrlo
Requisitos: Node 24.15 o superior (Angular 22 lo exige), y el backend arriba.
1. En la raíz del repo: `docker compose up -d --build` y esperar ~1 minuto.
2. `cd frontend`
3. `npm install` (la primera vez tarda un par de minutos)
4. `npm start`, y abrir http://localhost:4200
5. Entra con `docente1 / docente123` o `estudiante1 / estudiante123`.
Pruebas: `npm test -- --watch=false`.

## Decisiones
1. **Todo por el gateway.** El frontend nunca habla con un microservicio directamente.
2. **Los coeficientes se escriben de menor a mayor grado** (`x^2 - 4` → `-4, 0, 1`), igual que el contrato y la biblioteca en C. El formulario lo dice en el texto de ayuda. Si el profesor confirma el orden contrario, se cambia en `funciones.ts` y `util.ts`.
3. **Ocultar botones por rol es solo comodidad.** Quien decide es el backend (403); la pantalla lo muestra con un mensaje claro.
4. **La tabla de iteraciones toma sus columnas de lo que devuelve el backend**, así que sigue los cambios de `FilaIteracion` sin tocar el frontend.
5. **El tiempo total lo mide el navegador** (de extremo a extremo) y el nativo viene del backend; los dos se guardan en el historial.
6. **Errores del backend**: se muestra el `mensaje` y la lista `detalles[]` con campo y motivo (formato común de los servicios).

## Qué falta (se reparte)
- **HU-05, gráfica de la caída del error** (Liseth).
- **HU-04, comparar métodos sobre la misma función**.
- Validación más fina del formulario (por ejemplo coeficiente líder distinto de cero antes de enviar; hoy lo rechaza el backend con un mensaje).
- Imagen Docker del frontend (nginx) y su servicio en `docker-compose.yml`; hoy se corre con `npm start`.
- HU-09 y HU-10 (opcionales).

## Aviso de honestidad
Compila (`ng build`) y sus 11 pruebas unitarias pasan. El recorrido de pantallas se probó en un navegador real, pero con **Keycloak y el gateway simulados**: se comprobó que
cada petición lleva el token, que el estudiante no ve el formulario ni "Todo el historial", que el docente sí, y que los errores del backend se muestran.
**No se probó contra el sistema real**; ese es el primer paso al aplicarlo. Si el login da error, casi seguro es una dirección en `config.ts` o el
`redirectUri` del cliente `numerico-web` en Keycloak (debe permitir `http://localhost:4200/*`; ya está en el realm).
