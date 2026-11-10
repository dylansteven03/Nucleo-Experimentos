# Evidencia de autenticación y autorización de historial-service (RA-04, RA-05 y RA-06).
# Requisitos: docker compose -f docker-compose.dev.yml up -d  y  historial-service corriendo (puerto 8083).
# Uso (PowerShell, desde la raíz del repo):   .\scripts\evidencia-auth-historial.ps1
# Guarda la salida en docs\evidencia-auth-historial.txt para adjuntarla.

$keycloak = "http://localhost:8180/realms/numerico-realm/protocol/openid-connect/token"
$base     = "http://localhost:8083/historial"

function Obtener-Token($usuario, $clave) {
    $json = curl.exe -s -X POST $keycloak -d "grant_type=password" -d "client_id=numerico-web" `
        -d "username=$usuario" -d "password=$clave"
    return ($json | ConvertFrom-Json).access_token
}

# Claim "sub" del token (la parte central del JWT, en base64url).
function Sub-Del-Token($token) {
    $p = $token.Split(".")[1].Replace("-", "+").Replace("_", "/")
    switch ($p.Length % 4) { 2 { $p += "==" } 3 { $p += "=" } }
    $bytes = [Convert]::FromBase64String($p)
    return ([System.Text.Encoding]::UTF8.GetString($bytes) | ConvertFrom-Json).sub
}

# Si $salida es "codigo" devuelve solo el código HTTP; si es "cuerpo", el cuerpo de la respuesta.
function Llamar($metodo, $url, $token, $cuerpo, $salida) {
    $parametros = @("-s", "-X", $metodo, $url)
    if ($salida -eq "codigo") { $parametros = @("-s", "-o", "NUL", "-w", "%{http_code}", "-X", $metodo, $url) }
    if ($token) { $parametros += @("-H", "Authorization: Bearer $token") }
    $archivo = $null
    if ($cuerpo) {
        # PowerShell 5.1 pierde las comillas del JSON si va en la línea de comandos: se manda desde un archivo.
        $archivo = [System.IO.Path]::GetTempFileName()
        [System.IO.File]::WriteAllText($archivo, $cuerpo)
        $parametros += @("-H", "Content-Type: application/json", "--data-binary", "@$archivo")
    }
    $respuesta = & curl.exe @parametros
    if ($archivo) { Remove-Item $archivo -ErrorAction SilentlyContinue }
    return $respuesta
}

$docente    = Obtener-Token "docente1" "docente123"
$estudiante = Obtener-Token "estudiante1" "estudiante123"
if (-not $docente -or -not $estudiante) { Write-Host "No se pudo obtener token. ¿Keycloak está arriba?"; exit 1 }

# Token con la firma alterada: se cambia un carácter dentro de la firma.
$partes = $docente.Split(".")
$firma  = $partes[2].ToCharArray()
$firma[10] = if ($firma[10] -eq 'A') { 'B' } else { 'A' }
$alterado = $partes[0] + "." + $partes[1] + "." + (-join $firma)

$corrida = '{"funcionId":1,"metodo":"biseccion","convergio":true,"raiz":1.4142,"iteraciones":20,"tiempoNativoMs":0.8,"tiempoTotalMs":12.5}'
$invalida = '{"funcionId":1,"metodo":"newton","convergio":true,"iteraciones":5,"tiempoNativoMs":1.0,"tiempoTotalMs":20.0}'

$codEstPost = Llamar "POST" $base $estudiante $corrida "codigo"
$codDocPost = Llamar "POST" $base $docente $corrida "codigo"

# RA-06: el historial propio del estudiante solo trae filas con su propio sub.
$subEstudiante = Sub-Del-Token $estudiante
$propias = @(Llamar "GET" "$base/mio" $estudiante $null "cuerpo" | ConvertFrom-Json)
$soloPropias = ($propias.Count -gt 0) -and (@($propias | Where-Object { $_.usuario -ne $subEstudiante }).Count -eq 0)

$filas = @(
    [pscustomobject]@{ Caso = "1. Sin cabecera de autorización (GET /historial/mio)";        Esperado = "401";  Obtenido = (Llamar "GET" "$base/mio" $null $null "codigo") },
    [pscustomobject]@{ Caso = "2. Token con la firma alterada (GET /historial/mio)";         Esperado = "401";  Obtenido = (Llamar "GET" "$base/mio" $alterado $null "codigo") },
    [pscustomobject]@{ Caso = "3. Estudiante registra una corrida (POST /historial)";        Esperado = "201";  Obtenido = $codEstPost },
    [pscustomobject]@{ Caso = "4. Docente registra una corrida (POST /historial)";           Esperado = "201";  Obtenido = $codDocPost },
    [pscustomobject]@{ Caso = "5. Estudiante ve su historial (GET /historial/mio)";          Esperado = "200";  Obtenido = (Llamar "GET" "$base/mio" $estudiante $null "codigo") },
    [pscustomobject]@{ Caso = "6. Estudiante pide todo el historial (GET /historial)";       Esperado = "403";  Obtenido = (Llamar "GET" $base $estudiante $null "codigo") },
    [pscustomobject]@{ Caso = "7. Docente pide todo el historial (GET /historial)";          Esperado = "200";  Obtenido = (Llamar "GET" $base $docente $null "codigo") },
    [pscustomobject]@{ Caso = "8. Corrida inválida: convergió sin raíz (POST /historial)";   Esperado = "400";  Obtenido = (Llamar "POST" $base $estudiante $invalida "codigo") },
    [pscustomobject]@{ Caso = "9. RA-06: /mio del estudiante solo trae filas con su sub";    Esperado = "True"; Obtenido = "$soloPropias" }
)

$filas | Format-Table -AutoSize | Out-String | Tee-Object -FilePath "docs\evidencia-auth-historial.txt"
