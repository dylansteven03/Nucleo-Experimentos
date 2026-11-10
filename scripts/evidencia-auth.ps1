# Evidencia de autenticación y autorización (entregable 06 del documento).
# Pide tokens a Keycloak y ejecuta las llamadas contra funcion-service.
# Requisitos: docker compose -f docker-compose.dev.yml up -d  y  funcion-service corriendo (puerto 8081).
# Uso (PowerShell, desde la raíz del repo):   .\scripts\evidencia-auth.ps1
# Guarda la salida en docs\evidencia-auth.txt para adjuntarla.

$keycloak = "http://localhost:8180/realms/numerico-realm/protocol/openid-connect/token"
$api      = "http://localhost:8081/funciones"

function Obtener-Token($usuario, $clave) {
    $json = curl.exe -s -X POST $keycloak -d "grant_type=password" -d "client_id=numerico-web" `
        -d "username=$usuario" -d "password=$clave"
    return ($json | ConvertFrom-Json).access_token
}

# El cuerpo JSON se manda desde un archivo temporal: PowerShell 5.1 pierde las comillas
# si se pasa el JSON directo como argumento a curl.exe.
function Codigo($metodo, $token, $cuerpo) {
    $parametros = @("-s", "-o", "NUL", "-w", "%{http_code}", "-X", $metodo, $api)
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
if (-not $docente -or -not $estudiante) { Write-Host "No se pudo obtener token. ¿Keycloak está arriba?"; exit 1 }

# Token con la firma alterada: se cambia un carácter dentro de la firma.
$partes = $docente.Split(".")
$firma  = $partes[2].ToCharArray()
$firma[10] = if ($firma[10] -eq 'A') { 'B' } else { 'A' }
$alterado = $partes[0] + "." + $partes[1] + "." + (-join $firma)

$funcion = '{"nombre":"x2-4","coeficientes":[-4,0,1],"intervaloA":0,"intervaloB":5}'

$filas = @(
    [pscustomobject]@{ Caso = "1. Sin cabecera de autorización (GET /funciones)";       Esperado = 401; Obtenido = (Codigo "GET" $null $null) },
    [pscustomobject]@{ Caso = "2. Token con la firma alterada (GET /funciones)";         Esperado = 401; Obtenido = (Codigo "GET" $alterado $null) },
    [pscustomobject]@{ Caso = "3. Estudiante intenta registrar una función (POST)";      Esperado = 403; Obtenido = (Codigo "POST" $estudiante $funcion) },
    [pscustomobject]@{ Caso = "4. Docente consulta el catálogo (GET)";                   Esperado = 200; Obtenido = (Codigo "GET" $docente $null) },
    [pscustomobject]@{ Caso = "5. Docente registra una función válida (POST)";           Esperado = 201; Obtenido = (Codigo "POST" $docente $funcion) },
    [pscustomobject]@{ Caso = "6. Docente envía grado 11, debe rechazarse (POST)";       Esperado = 400; Obtenido = (Codigo "POST" $docente '{"nombre":"g11","coeficientes":[0,0,0,0,0,0,0,0,0,0,0,1],"intervaloA":0,"intervaloB":1}') }
)

$filas | Format-Table -AutoSize | Out-String | Tee-Object -FilePath "docs\evidencia-auth.txt"