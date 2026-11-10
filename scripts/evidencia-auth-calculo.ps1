# Evidencia de autenticación y autorización de calculo-service (RA-04 y RA-05).
# Requisitos: docker compose -f docker-compose.dev.yml up -d  y  calculo-service corriendo (puerto 8082).
# Uso (PowerShell, desde la raíz del repo):   .\scripts\evidencia-auth-calculo.ps1
# Guarda la salida en docs\evidencia-auth-calculo.txt para adjuntarla.

$keycloak = "http://localhost:8180/realms/numerico-realm/protocol/openid-connect/token"
$api      = "http://localhost:8082/calculo/ping?a=2&b=3"

function Obtener-Token($usuario, $clave) {
    $json = curl.exe -s -X POST $keycloak -d "grant_type=password" -d "client_id=numerico-web" `
        -d "username=$usuario" -d "password=$clave"
    return ($json | ConvertFrom-Json).access_token
}

function Codigo($token) {
    $parametros = @("-s", "-o", "NUL", "-w", "%{http_code}", $api)
    if ($token) { $parametros += @("-H", "Authorization: Bearer $token") }
    return (& curl.exe @parametros)
}

$docente    = Obtener-Token "docente1" "docente123"
$estudiante = Obtener-Token "estudiante1" "estudiante123"
if (-not $docente -or -not $estudiante) { Write-Host "No se pudo obtener token. ¿Keycloak está arriba?"; exit 1 }

# Token con la firma alterada: se cambia un carácter dentro de la firma.
$partes = $docente.Split(".")
$firma  = $partes[2].ToCharArray()
$firma[10] = if ($firma[10] -eq 'A') { 'B' } else { 'A' }
$alterado = $partes[0] + "." + $partes[1] + "." + (-join $firma)

$filas = @(
    [pscustomobject]@{ Caso = "1. Sin cabecera de autorización (GET /calculo/ping)";   Esperado = 401; Obtenido = (Codigo $null) },
    [pscustomobject]@{ Caso = "2. Token con la firma alterada (GET /calculo/ping)";    Esperado = 401; Obtenido = (Codigo $alterado) },
    [pscustomobject]@{ Caso = "3. Docente con token válido (GET /calculo/ping)";       Esperado = 200; Obtenido = (Codigo $docente) },
    [pscustomobject]@{ Caso = "4. Estudiante con token válido (GET /calculo/ping)";    Esperado = 200; Obtenido = (Codigo $estudiante) }
)

$filas | Format-Table -AutoSize | Out-String | Tee-Object -FilePath "docs\evidencia-auth-calculo.txt"
