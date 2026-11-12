# Evidencia del sistema completo en Docker, todo por el api-gateway (puerto 8080).
# Requisitos: docker compose up -d --build   (esperar ~1 minuto a que Keycloak y los servicios arranquen).
# Uso (PowerShell, desde la raíz del repo):   .\scripts\evidencia-stack-docker.ps1
# Guarda la salida en docs\evidencia-stack-docker.txt para adjuntarla.

$keycloak = "http://localhost:8180/realms/numerico-realm/protocol/openid-connect/token"
$gw       = "http://localhost:8080"

function Obtener-Token($usuario, $clave) {
    $json = curl.exe -s -X POST $keycloak -d "grant_type=password" -d "client_id=numerico-web" `
        -d "username=$usuario" -d "password=$clave"
    return ($json | ConvertFrom-Json).access_token
}

function Codigo($metodo, $url, $token, $cuerpo) {
    $parametros = @("-s", "-o", "NUL", "-w", "%{http_code}", "-X", $metodo, $url)
    if ($token) { $parametros += @("-H", "Authorization: Bearer $token") }
    $archivo = $null
    if ($cuerpo) {
        $archivo = [System.IO.Path]::GetTempFileName()
        [System.IO.File]::WriteAllText($archivo, $cuerpo)
        $parametros += @("-H", "Content-Type: application/json", "--data-binary", "@$archivo")
    }
    $salida = & curl.exe @parametros
    if ($archivo) { Remove-Item $archivo -ErrorAction SilentlyContinue }
    return $salida
}

$docente    = Obtener-Token "docente1" "docente123"
$estudiante = Obtener-Token "estudiante1" "estudiante123"
if (-not $docente -or -not $estudiante) { Write-Host "No se pudo obtener token. ¿Keycloak ya arrancó?"; exit 1 }

$partes = $docente.Split(".")
$firma  = $partes[2].ToCharArray()
$firma[10] = if ($firma[10] -eq 'A') { 'B' } else { 'A' }
$alterado = $partes[0] + "." + $partes[1] + "." + (-join $firma)

$funcion = '{"nombre":"x^2 - 2","coeficientes":[-2,0,1],"intervaloA":0,"intervaloB":2}'
$corrida = '{"funcionId":1,"metodo":"biseccion","convergio":true,"raiz":1.4142,"iteraciones":20,"tiempoNativoMs":0.8,"tiempoTotalMs":12.5}'

$filas = @(
    [pscustomobject]@{ Caso = "1. Sin token (GET /calculo/ping)";                         Esperado = 401; Obtenido = (Codigo "GET" "$gw/calculo/ping?a=2&b=3" $null $null) },
    [pscustomobject]@{ Caso = "2. Token con la firma alterada (GET /calculo/ping)";       Esperado = 401; Obtenido = (Codigo "GET" "$gw/calculo/ping?a=2&b=3" $alterado $null) },
    [pscustomobject]@{ Caso = "3. Docente llama al cálculo en C (GET /calculo/ping)";     Esperado = 200; Obtenido = (Codigo "GET" "$gw/calculo/ping?a=2&b=3" $docente $null) },
    [pscustomobject]@{ Caso = "4. Estudiante llama al cálculo en C (GET /calculo/ping)";  Esperado = 200; Obtenido = (Codigo "GET" "$gw/calculo/ping?a=2&b=3" $estudiante $null) },
    [pscustomobject]@{ Caso = "5. Docente registra función (POST /funciones)";            Esperado = 201; Obtenido = (Codigo "POST" "$gw/funciones" $docente $funcion) },
    [pscustomobject]@{ Caso = "6. Estudiante registra función (POST /funciones)";         Esperado = 403; Obtenido = (Codigo "POST" "$gw/funciones" $estudiante $funcion) },
    [pscustomobject]@{ Caso = "7. Estudiante registra corrida (POST /historial)";         Esperado = 201; Obtenido = (Codigo "POST" "$gw/historial" $estudiante $corrida) },
    [pscustomobject]@{ Caso = "8. Estudiante pide todo el historial (GET /historial)";    Esperado = 403; Obtenido = (Codigo "GET" "$gw/historial" $estudiante $null) },
    [pscustomobject]@{ Caso = "9. Docente pide todo el historial (GET /historial)";       Esperado = 200; Obtenido = (Codigo "GET" "$gw/historial" $docente $null) }
)

$filas | Format-Table -AutoSize | Out-String | Tee-Object -FilePath "docs\evidencia-stack-docker.txt"
