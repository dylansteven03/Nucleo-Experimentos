# Medición de RQ-01 con el procedimiento del enunciado (un caso, tres variantes, mediana y p95).
#
# Casos (el caso no cambia entre mediciones, o la comparación no vale):
#   newton5          Newton sobre x^5 - 2, coef -2,0,0,0,0,1, x0 = 1.2, tol 1e-10, máximo 100 iteraciones (caso fijo)
#   biseccion1e-12   Bisección sobre x^5 - 2 en [0,2], tol 1e-12, máximo 100 (del orden de 40 iteraciones)
#   agota100         Bisección sobre x^5 - 2 en [0,2], tol 1e-300, máximo 100: no puede converger, agota las 100
# Variantes: A = JNA una llamada por corrida, B = JNA una llamada por iteración, C = Java puro (línea base).
# El script no cambia el motor: se levanta el sistema con el motor de la variante antes de medir.
#
# Procedimiento: 40 corridas por caso, se descartan las 10 primeras (calentamiento de la JVM y carga de la
# biblioteca) y con las 30 restantes se calculan la mediana y el p95. Mide de punta a punta por el gateway
# (curl) y toma el tiempo de cómputo que reporta el servicio (tiempoNativoMs).
#
# Requisitos: docker compose up -d --build, historial con al menos 500 corridas (.\scripts\cargar-historial.ps1)
# y los contenedores con los límites del manifiesto (se comprueba al empezar).
# Uso (PowerShell, desde la raíz del repo):
#   .\scripts\medicion-rq01-enunciado.ps1 -Variante A -Caso newton5
# Guarda la salida en docs\medicion-rq01-<variante>-<caso>.txt

param(
    [ValidateSet("A", "B", "C")][string]$Variante = "A",
    [ValidateSet("newton5", "biseccion1e-12", "agota100")][string]$Caso = "newton5"
)

$keycloak  = "http://localhost:8180/realms/numerico-realm/protocol/openid-connect/token"
$gw        = "http://localhost:8080"
$corridas  = 40
$descartar = 10
$inv       = [System.Globalization.CultureInfo]::InvariantCulture

$nombres = @{
    "A" = "A - JNA, una llamada por corrida"
    "B" = "B - JNA, una llamada por iteracion"
    "C" = "C - Java puro, linea base"
}
$rutas = @{
    "newton5"        = "/calculo/newton?coef=-2,0,0,0,0,1&x0=1.2&tol=1e-10&maxIter=100"
    "biseccion1e-12" = "/calculo/biseccion?coef=-2,0,0,0,0,1&a=0&b=2&tol=1e-12&maxIter=100"
    "agota100"       = "/calculo/biseccion?coef=-2,0,0,0,0,1&a=0&b=2&tol=1e-300&maxIter=100"
}

function Obtener-Token($usuario, $clave) {
    $json = curl.exe -s -X POST $keycloak -d "grant_type=password" -d "client_id=numerico-web" `
        -d "username=$usuario" -d "password=$clave"
    return ($json | ConvertFrom-Json).access_token
}
function Mediana($valores) {
    $o = @($valores | Sort-Object)
    $n = $o.Count
    if ($n % 2 -eq 1) { return $o[($n - 1) / 2] }
    return ($o[$n / 2 - 1] + $o[$n / 2]) / 2
}
# Percentil por rango más cercano: con 30 valores, el p95 es el valor número 29.
function P95($valores) {
    $o = @($valores | Sort-Object)
    return $o[[math]::Ceiling(0.95 * $o.Count) - 1]
}
function F2($x) { return $x.ToString("0.00", $inv) }
function F3($x) { return $x.ToString("0.000", $inv) }

$docente = Obtener-Token "docente1" "docente123"
if (-not $docente) { Write-Host "No se pudo obtener el token. ¿Keycloak ya arrancó?"; exit 1 }

# 1. El entorno del escenario: historial con al menos 500 corridas.
$lista = curl.exe -s -H "Authorization: Bearer $docente" "$gw/historial" | ConvertFrom-Json
$enHistorial = if ($null -eq $lista) { 0 } else { @($lista).Count }
if ($enHistorial -lt 500) {
    Write-Host "El historial tiene $enHistorial corridas y el escenario pide al menos 500. Corra primero .\scripts\cargar-historial.ps1"
    exit 1
}

# 2. Los límites de recursos del contenedor de cálculo (el número que vale es el del contenedor con límites).
$limite = docker inspect nucleo-calculo-service-1 --format "{{.HostConfig.NanoCpus}} {{.HostConfig.Memory}}" 2>$null
$partes = "$limite".Trim().Split(" ")
if ($partes.Count -ne 2 -or [double]$partes[0] -le 0 -or [double]$partes[1] -le 0) {
    Write-Host "El contenedor nucleo-calculo-service-1 no tiene límites de CPU y memoria. Levante el sistema con docker compose up -d desde la rama que trae el manifiesto."
    exit 1
}
$cpuLimite = [double]$partes[0] / 1e9
$memLimite = [double]$partes[1] / 1MB

# 3. Las corridas.
$ruta = $rutas[$Caso]
$temporal = [System.IO.Path]::GetTempFileName()
$total = @(); $computo = @(); $iteraciones = @(); $estado = ""
for ($i = 1; $i -le $corridas; $i++) {
    $salida = & curl.exe -s -o $temporal -w "%{http_code} %{time_total}" -H "Authorization: Bearer $docente" "$gw$ruta"
    $p = "$salida".Trim().Split(" ")
    if ($p[0] -ne "200") {
        Remove-Item $temporal -ErrorAction SilentlyContinue
        Write-Host "La corrida $i respondió $($p[0]). ¿Están arriba todos los servicios?"
        exit 1
    }
    $cuerpo = Get-Content $temporal -Raw | ConvertFrom-Json
    if ($i -gt $descartar) {
        $total       += [double]::Parse($p[1], $inv) * 1000
        $computo     += [double]$cuerpo.tiempoNativoMs
        $iteraciones += [int]$cuerpo.totalIteraciones
    }
    $estado = $cuerpo.estado
}
Remove-Item $temporal -ErrorAction SilentlyContinue

$distintas = @($iteraciones | Select-Object -Unique)
$iterTexto = if ($distintas.Count -eq 1) { "$($distintas[0])" } else { "varía: " + ($distintas -join ", ") }

$fecha = Get-Date -Format "yyyy-MM-dd"
$fila = [pscustomobject]@{
    "Variante"           = $nombres[$Variante]
    "Mediana (ms)"       = (F2 (Mediana $total))
    "P95 (ms)"           = (F2 (P95 $total))
    "Computo mediana"    = (F3 (Mediana $computo))
    "Computo p95"        = (F3 (P95 $computo))
    "Iteraciones"        = $iterTexto
}

$texto = @()
$texto += "Medicion RQ-01 del $fecha. Variante $Variante, caso $Caso."
$texto += "Ruta: GET $ruta  (estado de la ultima corrida: $estado)"
$texto += "Por el gateway ($gw), Docker Desktop, portatil de desarrollo (no una maquina limpia)."
$texto += "Contenedor de calculo con limite de $(F2 $cpuLimite) CPU y $(F2 $memLimite) MiB. Historial con $enHistorial corridas."
$texto += "$corridas corridas, se descartan las $descartar primeras; estadistica con las $($corridas - $descartar) restantes."
$texto += "Mediana y P95: tiempo total medido con curl. Computo: tiempoNativoMs que reporta el servicio."
$texto += ""
$texto += ($fila | Format-Table -AutoSize | Out-String)
$texto | Tee-Object -FilePath "docs\medicion-rq01-$Variante-$Caso.txt"
