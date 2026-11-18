# Despliega el sistema completo como stack de Docker Swarm (docker-stack.yml).
# Uso (PowerShell, desde la raíz del repo):   .\scripts\desplegar-swarm.ps1
# Quitar el stack:                            docker stack rm nucleo
#
# Requisitos: Docker Desktop abierto y el sistema de docker compose APAGADO (docker compose down),
# porque los dos usan los mismos puertos (4200, 8080 a 8083, 8180).

# 1. Swarm debe estar activo (un solo nodo basta). Si no lo está, se activa.
$estado = (docker info --format "{{.Swarm.LocalNodeState}}").Trim()
if ($estado -ne "active") {
    Write-Host "Activando Docker Swarm (docker swarm init)..."
    docker swarm init
    if ($LASTEXITCODE -ne 0) {
        Write-Host "No se pudo activar Swarm. Si el mensaje pide una dirección, use: docker swarm init --advertise-addr 127.0.0.1"
        exit 1
    }
}

# 2. Swarm no construye: se construyen las imágenes con docker compose (quedan como nucleo-*:latest).
Write-Host "Construyendo las imágenes..."
docker compose build
if ($LASTEXITCODE -ne 0) { Write-Host "Falló la construcción de las imágenes."; exit 1 }

# 3. Despliegue del stack. --resolve-image never: las imágenes son locales, no están en un registro.
Write-Host "Desplegando el stack 'nucleo'..."
docker stack deploy --resolve-image never -c docker-stack.yml nucleo
if ($LASTEXITCODE -ne 0) { Write-Host "Falló el despliegue del stack."; exit 1 }

Write-Host ""
Write-Host "Listo. Siga el arranque con:  docker stack services nucleo   (esperar a que todo diga 1/1)"
Write-Host "Frontend: http://localhost:4200   Gateway: http://localhost:8080"
