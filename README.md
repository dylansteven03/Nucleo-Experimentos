# Núcleo · Banco de Métodos Numéricos

Plataforma académica distribuida para análisis y comparación de métodos de aproximación de raíces polinómicas (bisección, Newton-Raphson y secante). El cálculo corre en una biblioteca en C (`libnumerico.so`) invocada desde Java con JNA.

## Requisitos previos

- Docker y Docker Compose
- JDK 21
- Maven 3.9+
- GCC y Make (solo para desarrollo del módulo nativo en local)

## Estructura

| Carpeta | Contenido |
|---|---|
| `api-gateway/` | Punto de entrada único, orquesta la corrida |
| `funcion-service/` | Catálogo de funciones |
| `calculo-service/` | Invoca la biblioteca nativa por JNA |
| `historial-service/` | Persistencia de corridas |
| `native/` | Código C de `libnumerico.so` |
| `frontend/` | Angular (pendiente) |
| `keycloak/` | Realm importable (pendiente) |
| `docs/` | Contrato, evidencias e informes |

## Verificar el esqueleto (Fase 0)

```bash
mvn clean compile
```

## Arranque único (disponible desde la Fase 9)

```bash
docker compose up --build
```

## Bitácora

Las decisiones de diseño viven en [BITACORA.md](BITACORA.md).
