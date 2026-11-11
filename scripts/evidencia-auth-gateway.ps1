# Evidencia del api-gateway (RA-04 y RA-05, de punta a punta por el puerto 8080).
# Requisitos: docker compose -f docker-compose.dev.yml up -d  y corriendo: api-gateway (8080),
# funcion-service (8081) e historial-service (8083).
# Uso (PowerShell, desde la raíz del repo):   .\scripts\evidencia-auth-gateway.ps1
# Guarda la salida en docs\evidencia-auth-gateway.txt para adjuntarla.

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
        # PowerShell 5.1 pierde las comillas del JSON en la línea de comandos: se manda desde un archivo.
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
if (-not $docente -or -not $estudiante) { Write-Host "No se pudo obtener token. ¿Keycloak está arriba?"; exit 1 }

# Token con la firma alterada: se cambia un carácter dentro de la firma.
$partes = $docente.Split(".")
$firma  = $partes[2].ToCharArray()
$firma[10] = if ($firma[10] -eq 'A') { 'B' } else { 'A' }
$alterado = $partes[0] + "." + $partes[1] + "." + (-join $firma)

$funcion = '{"nombre":"x^2 - 2","coeficientes":[-2,0,1],"intervaloA":0,"intervaloB":2}'
$corrida = '{"funcionId":1,"metodo":"biseccion","convergio":true,"raiz":1.4142,"iteraciones":20,"tiempoNativoMs":0.8,"tiempoTotalMs":12.5}'

$filas = @(
    [pscustomobject]@{ Caso = "1. Sin cabecera de autorización (GET /funciones)";           Esperado = 401; Obtenido = (Codigo "GET" "$gw/funciones" $null $null) },
    [pscustomobject]@{ Caso = "2. Token con la firma alterada (GET /funciones)";            Esperado = 401; Obtenido = (Codigo "GET" "$gw/funciones" $alterado $null) },
    [pscustomobject]@{ Caso = "3. Docente lista funciones, ruta al servicio (GET /funciones)";   Esperado = 200; Obtenido = (Codigo "GET" "$gw/funciones" $docente $null) },
    [pscustomobject]@{ Caso = "4. Estudiante lista funciones (GET /funciones)";             Esperado = 200; Obtenido = (Codigo "GET" "$gw/funciones" $estudiante $null) },
    [pscustomobject]@{ Caso = "5. Estudiante registra función: el servicio lo prohíbe (POST /funciones)"; Esperado = 403; Obtenido = (Codigo "POST" "$gw/funciones" $estudiante $funcion) },
    [pscustomobject]@{ Caso = "6. Docente registra función (POST /funciones)";              Esperado = 201; Obtenido = (Codigo "POST" "$gw/funciones" $docente $funcion) },
    [pscustomobject]@{ Caso = "7. Estudiante registra corrida (POST /historial)";           Esperado = 201; Obtenido = (Codigo "POST" "$gw/historial" $estudiante $corrida) },
    [pscustomobject]@{ Caso = "8. Estudiante pide todo el historial (GET /historial)";      Esperado = 403; Obtenido = (Codigo "GET" "$gw/historial" $estudiante $null) },
    [pscustomobject]@{ Caso = "9. Docente pide todo el historial (GET /historial)";         Esperado = 200; Obtenido = (Codigo "GET" "$gw/historial" $docente $null) },
    [pscustomobject]@{ Caso = "10. Ruta que no existe con token válido (GET /noexiste)";    Esperado = 404; Obtenido = (Codigo "GET" "$gw/noexiste" $docente $null) }
)

$filas | Format-Table -AutoSize | Out-String | Tee-Object -FilePath "docs\evidencia-auth-gateway.txt"
