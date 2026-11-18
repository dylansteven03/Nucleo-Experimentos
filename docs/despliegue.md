# Despliegue del sistema (RQ-03)

Un solo comando levanta todo, sin pasos manuales y sin que nadie tenga que conocer el orden.

## Con Docker Compose (el camino normal)
Requisito: Docker. Nada más (sin JDK, sin gcc, sin Node).
```bash
docker compose up -d --build
```
Frontend en http://localhost:4200, gateway en http://localhost:8080, Keycloak en http://localhost:8180.
Usuarios del realm: `docente1 / docente123`, `estudiante1 / estudiante123`, `estudiante2 / estudiante123`.
Parar: `docker compose down` (agregar `-v` borra los datos).

## Con Docker Swarm (stack)
El mismo sistema como stack de Swarm, con `docker-stack.yml`. Primero apague el de Compose (mismos puertos).
```powershell
.\scripts\desplegar-swarm.ps1
docker stack services nucleo      # esperar a que todo diga 1/1
```
Quitar: `docker stack rm nucleo`.

Diferencias con Compose, por cómo funciona Swarm:
- No construye imágenes: usa las `nucleo-*:latest` que deja `docker compose build` (el script lo hace antes de desplegar).
- Ignora `depends_on`: si la base de datos aún no está lista, el servicio falla al arrancar y `restart_policy: on-failure` lo reintenta cada 5 s hasta que conecta.

## Límites de CPU y memoria declarados
Están en `deploy.resources.limits` de ambos archivos. Son el entorno con el que se miden RQ-01 y RQ-03.

| Servicio | CPU | Memoria |
|---|---|---|
| calculo-service, funcion-service, historial-service, api-gateway | 1 | 512 MiB |
| keycloak | 1 | | keycloak | 1 | 1536 MiB | MiB |
| postgres-funcion, postgres-historial | 0,5 | 256 MiB |
| frontend (nginx) | 0,25 | 64 MiB |

Los 512 MiB y 1 CPU de los servicios Java son los del escenario de RQ-01. Los demás los fijamos nosotros; se comprueban con `docker stats --no-stream` (columna MEM USAGE / LIMIT) después de usar el sistema. Si un servicio se queda sin memoria, aparece reiniciándose en `docker compose ps` y se sube su límite aquí y en la bitácora.

## Cómo comprobar los límites
```powershell
docker compose up -d --build
docker stats --no-stream
```
Cada fila debe mostrar el límite declarado en la columna `MEM USAGE / LIMIT`.

## Cómo medir el arranque desde cero (RQ-03)
Máquina con Docker y nada más. Tres corridas, anotando la mediana:
```powershell
docker compose down -v
docker builder prune -af        # sin caché de construcción
Measure-Command { docker compose up -d --build }
```
Luego, el tiempo hasta la primera petición correcta por el gateway (con un token de docente). El criterio es menos de 90 s desde el comando; la bitácora ya aclara cómo se cuenta (con y sin construcción).
