# Medición de RQ-01 (desempeño) de punta a punta, por el api-gateway (HU-08).
# Por cada caso: 40 corridas, se descartan las 10 primeras (calentamiento de la JVM y carga de la biblioteca en C)
# y con las 30 restantes se calculan la mediana y el p95 del tiempo TOTAL (lo que ve el cliente por HTTP)
# y del tiempo NATIVO (tiempoNativoMs, medido dentro del servicio).
# Metas de RQ-01: total < 500 ms y nativo < 50 ms. Se compara el p95, que es el criterio más exigente.
#
# Requisitos: docker compose up -d --build  y esperar ~1 minuto.
# Uso (PowerShell, desde la raíz del repo):   .\scripts\medicion-rq01.ps1
# Guarda la salida en docs\medicion-rq01.txt para pegarla en la bitácora.

$keycloak  = "http://localhost:8180/realms/numerico-realm/protocol/openid-connect/token"
$base      = "http://localhost:8080/calculo"
$corridas  = 40
$descartar = 10
$inv       = [System.Globalization.CultureInfo]::InvariantCulture

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
    $indice = [math]::Ceiling(0.95 * $o.Count) - 1
    return $o[$indice]
}

function Formato($numero) { return $numero.ToString("0.00", $inv) }

function Medir($caso, $ruta) {
    # Un token nuevo por caso: dura pocos minutos y la medición completa tarda más.
    $token = Obtener-Token "docente1" "docente123"
    $temporal = [System.IO.Path]::GetTempFileName()
    $total  = @()
    $nativo = @()
    $estado = ""

    for ($i = 1; $i -le $corridas; $i++) {
        $salida = & curl.exe -s -o $temporal -w "%{http_code} %{time_total}" `
            -H "Authorization: Bearer $token" "$base$ruta"
        $partes = "$salida".Trim().Split(" ")
        if ($partes[0] -ne "200") {
            Remove-Item $temporal -ErrorAction SilentlyContinue
            Write-Host "El caso '$caso' respondió $($partes[0]) en la corrida $i. ¿Están arriba todos los servicios?"
            exit 1
        }
        $cuerpo = Get-Content $temporal -Raw | ConvertFrom-Json
        if ($i -gt $descartar) {
            $total  += [double]::Parse($partes[1], $inv) * 1000
            $nativo += [double]$cuerpo.tiempoNativoMs
        }
        $estado = $cuerpo.estado
    }
    Remove-Item $temporal -ErrorAction SilentlyContinue

    $p95Total  = P95 $total
    $p95Nativo = P95 $nativo
    $cumple = if (($p95Total -lt 500) -and ($p95Nativo -lt 50)) { "Cumple" } else { "NO cumple" }

    return [pscustomobject]@{
        Caso            = $caso
        Estado          = $estado
        "Mediana total" = (Formato (Mediana $total))
        "p95 total"     = (Formato $p95Total)
        "Mediana nativo"= (Formato (Mediana $nativo))
        "p95 nativo"    = (Formato $p95Nativo)
        "RQ-01"         = $cumple
    }
}

# Los coeficientes van de menor a mayor grado: x^2 - 4  ->  coef=-4,0,1
$filas = @(
    (Medir "Bisección x^2-4 en [0,5], tol 1e-10"       "/biseccion?coef=-4,0,1&a=0&b=5&tol=1e-10"),
    (Medir "Newton x^2-4, x0=3, tol 1e-10"             "/newton?coef=-4,0,1&x0=3&tol=1e-10"),
    (Medir "Secante x^2-4, x0=1, x1=3, tol 1e-10"      "/secante?coef=-4,0,1&x0=1&x1=3&tol=1e-10"),
    (Medir "Bisección x^10-2 en [0,2], tol 1e-12"      "/biseccion?coef=-2,0,0,0,0,0,0,0,0,0,1&a=0&b=2&tol=1e-12")
)

$fecha = Get-Date -Format "yyyy-MM-dd"
$texto = @()
$texto += "Medición RQ-01 del $fecha, por el gateway (puerto 8080), Docker Desktop, portátil de desarrollo (no una máquina limpia)."
$texto += "$corridas corridas por caso, se descartan las $descartar primeras; estadística con las $($corridas - $descartar) restantes. Tiempos en ms."
$texto += "Metas: total < 500 ms y nativo < 50 ms (se compara el p95)."
$texto += ""
$texto += ($filas | Format-Table -AutoSize | Out-String)
$texto | Tee-Object -FilePath "docs\medicion-rq01.txt"
