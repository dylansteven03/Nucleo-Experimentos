# Avance: HU-17 · RA-07 Expiración y cierre de sesión

## Qué cambia
| Archivo | Cambio |
|---|---|
| `frontend/src/app/core/auth.service.ts` | Si Keycloak no consigue renovar, vuelve al login (`onAuthRefreshError`). Si falla la renovación dentro de `tokenVigente`, la petición se detiene mientras la página se redirige (antes salía con un token vacío y mostraba un error). Nuevo `reautenticar()` para un 401 inesperado, con protección contra bucles. Al volver con «atrás» desde la memoria del navegador (`pageshow`), recarga la página para comprobar la sesión. |
| `frontend/src/app/core/auth.interceptor.ts` | Si el backend responde 401, vuelve al login una vez en lugar de mostrar un error sin explicación. |
| `scripts/vigencia-token.ps1` | Cambia la vigencia del token del realm a 60 s (demostración) o la devuelve a 300 s, sin reimportar el realm. |

Qué ya existía y no se toca: renovación con `updateToken(30)`, y `salir()` con `keycloak.logout()`, que llama al cierre de sesión de Keycloak (invalida la sesión allá, no solo en el navegador). La vigencia está declarada en el realm (`accessTokenLifespan: 300`).

## Cómo probarlo (esto es la evidencia del criterio de RA-07)
Requisito: sistema arriba con `docker compose up -d --build` (el frontend cambió, hay que reconstruirlo).

1. `.\scripts\vigencia-token.ps1 -Segundos 60` → debe imprimir `60 segundos`.
2. Abra http://localhost:4200 en una ventana **nueva** (si había sesión, salga primero) e inicie sesión con `docente1`.
3. Con F12 → Network abierto, use la aplicación (por ejemplo, la pantalla Funciones). Espere **más de 1 minuto sin cerrar nada** y vuelva a usarla (Funciones, Ejecutar, Historial).
   - Esperado: la aplicación sigue funcionando. En Network se ve, antes de la petición, una llamada `.../protocol/openid-connect/token` (la renovación). Haga captura.
   - Si en cambio lo lleva al inicio de sesión, también cumple el criterio. Lo que no debe pasar es una pantalla con un error sin explicación.
4. Pulse **Salir**. Debe volver a la pantalla de inicio de sesión de Keycloak.
5. Pulse el botón **Atrás** del navegador una o dos veces. Esperado: no se ve la aplicación con datos; pide iniciar sesión otra vez. Haga captura.
6. Con la sesión ya cerrada, abra http://localhost:4200 en otra pestaña: debe pedir usuario y contraseña de nuevo. Eso demuestra que la sesión se cerró en Keycloak y no solo en el navegador.
7. Al terminar, deje la vigencia como estaba: `.\scripts\vigencia-token.ps1 -Segundos 300`.

## Aviso de honestidad
Compila con las pruebas de tipos de TypeScript (`tsc --noEmit` sin errores). **No se ejecutó contra Keycloak real**; los pasos de arriba son esa prueba. Si algo se comporta distinto, pegue lo que ve (captura y consola F12).
No se agregaron pruebas unitarias de estas dos piezas: dependen de Keycloak y se comprueban con el recorrido de arriba.
