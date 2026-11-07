# Avance del backend · 6 nov 2026

Código inicial para arrancar el Sprint 1. Se copia sobre la raíz del repositorio (las rutas ya coinciden).

## Qué trae

| Archivo | Para qué | Historia |
|---|---|---|
| `keycloak/numerico-realm.json` | Realm completo: roles `docente` y `estudiante`, cliente público `numerico-web` con PKCE, audiencia `numerico-api`, 3 usuarios de prueba | HU-11 |
| `docker-compose.dev.yml` | Keycloak (puerto 8180) y las dos bases de datos Postgres (5433 y 5434) para desarrollo | HU-11 |
| `funcion-service/` | Catálogo de funciones: `POST/GET/DELETE /funciones`, validación con 400 y mensaje claro, guarda `sub` de quien la registra | HU-01, HU-16 |
| `funcion-service/.../seguridad/` | Validación del JWT (firma, emisor, audiencia), lectura de roles de Keycloak y matriz de autorización | HU-14, HU-15 |
| `scripts/evidencia-auth.ps1` | Ejecuta las llamadas 401/403/200 y guarda la tabla en `docs/evidencia-auth.txt` | Entregable 06 |
| `docs/bitacora-entradas-nuevas.md` | 5 decisiones listas para pegar en `BITACORA.md` | Bitácora |

## Usuarios de prueba (solo desarrollo)

| Usuario | Clave | Rol |
|---|---|---|
| docente1 | docente123 | docente |
| estudiante1 | estudiante123 | estudiante |
| estudiante2 | estudiante123 | estudiante |

## Cómo probarlo

1. Cree una rama: `git checkout develop` y `git checkout -b feature/Dylan-avance-backend`.
2. Copie el contenido de esta carpeta sobre la raíz del repo, aceptando reemplazar `keycloak/numerico-realm.json`.
3. Copie `.env.example` a `.env` si aún no lo hizo.
4. Levante la infraestructura: `docker compose -f docker-compose.dev.yml up -d`.
5. Pruebas sin infraestructura: `mvn -pl funcion-service test` (16 pruebas, no necesitan base de datos ni Keycloak).
6. Corra `FuncionServiceApplication` desde IntelliJ (puerto 8081).
7. En PowerShell, desde la raíz: `.\scripts\evidencia-auth.ps1`. Debe imprimir 401, 401, 403, 200, 201 y 400.
8. Keycloak queda en http://localhost:8180 (usuario `admin`, clave la de `.env`).

## Qué no se pudo comprobar aquí

Este entorno no tiene acceso a Maven Central ni a Docker, así que **no se compiló ni se ejecutó**. Solo se revisó la sintaxis del Java y que el JSON y el YAML sean válidos. Si algo falla al correr, mándenme el error completo.

Dos puntos a vigilar:
- Si el script da 403 para el docente, revise con jwt.io que el token traiga `realm_access.roles`; el realm depende de los scopes por defecto de Keycloak.
- El `iss` del token tiene que ser exactamente `http://localhost:8180/realms/numerico-realm`. Si usan otra dirección, cambien `KEYCLOAK_ISSUER`.

## Para el resto del equipo

- **Valentina (HU-14, 15, 17):** copie el paquete `seguridad/` a `historial-service` y `api-gateway` (en el gateway reactivo hay que usar la versión reactiva del convertidor) y ajuste la matriz de cada uno.
- **Jonatan (HU-07, 16):** en historial-service guarde el `sub` del token en cada corrida, igual que `creadoPor`.
- **Brian (HU-01, 12):** el formulario de registro debe enviar `coeficientes` de menor a mayor grado (ver decisión pendiente abajo) y mostrar `detalles[]` de las respuestas 400.
- **Cristóbal (HU-13):** el interceptor solo adjunta `Authorization: Bearer`; la API de ejemplo ya está lista para probarlo.
- **Juan David (HU-02, 06):** `calculo-service` aún tiene `SecurityConfig` abierta; se reemplaza copiando el paquete `seguridad/`.

## Decisión pendiente con el profesor

El orden de los coeficientes. El documento y los bocetos usan de mayor a menor grado; el contrato de la corrida y la biblioteca en C usan de menor a mayor. Este avance sigue el contrato (menor a mayor). Si el profesor exige mayor a menor, se invierte la lista en `funcion-service` y no se toca la biblioteca en C.
