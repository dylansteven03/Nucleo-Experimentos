# Deja el historial con al menos N corridas, que es el entorno que pide el escenario de RQ-01
# ("historial con al menos 500 corridas ya cargadas"). Las corridas son sintéticas y se registran a nombre de
# estudiante2 para no llenar el "Mi historial" de docente1 ni de estudiante1.
#
# Requisitos: sistema arriba (docker compose up -d --build) y esperar ~1 minuto.
# Uso (PowerShell, desde la raíz del repo):   .\scripts\cargar-historial.ps1          (500 por defecto)
#                                             .\scripts\cargar-historial.ps1 -Total 800
# Es idempotente: solo registra las que faltan. Para volver a empezar de cero: docker compose down -v

param([int]$Total = 500)

$keycloak = "http://localhost:8180/realms/numerico-realm/protocol/openid-connect/token"
$gw       = "http://localhost:8080"

function Obtener-Token($usuario, $clave) {
    $json = curl.exe -s -X POST $keycloak -d "grant_type=password" -d "client_id=numerico-web" `
        -d "username=$usuario" -d "password=$clave"
    return ($json | ConvertFrom-Json).access_token
}

function Contar-Historial($token) {
    $cuerpo = curl.exe -s -H "Authorization: Bearer $token" "$gw/historial"
    $lista = $cuerpo | ConvertFrom-Json
    if ($null -eq $lista) { return 0 }
    return @($lista).Count
}

$docente = Obtener-Token "docente1" "docente123"
if (-not $docente) { Write-Host "No se pudo obtener el token. ¿Keycloak ya arrancó?"; exit 1 }

$existentes = Contar-Historial $docente
$faltan = $Total - $existentes
Write-Host "El historial tiene $existentes corridas; la meta es $Total."
if ($faltan -le 0) { Write-Host "No hace falta cargar más."; exit 0 }

# Una corrida típica del caso fijo de RQ-01 (Newton sobre x^5 - 2).
$corrida = '{"funcionId":1,"metodo":"newton","convergio":true,"raiz":1.1486983549970351,"iteraciones":6,"tiempoNativoMs":0.08,"tiempoTotalMs":14.5}'
$archivo = [System.IO.Path]::GetTempFileName()
[System.IO.File]::WriteAllText($archivo, $corrida)

$estudiante = Obtener-Token "estudiante2" "estudiante123"
for ($i = 1; $i -le $faltan; $i++) {
    # El token dura pocos minutos: se renueva cada 100 registros.
    if ($i % 100 -eq 0) { $estudiante = Obtener-Token "estudiante2" "estudiante123" }
    $codigo = curl.exe -s -o NUL -w "%{http_code}" -X POST "$gw/historial" `
        -H "Authorization: Bearer $estudiante" -H "Content-Type: application/json" --data-binary "@$archivo"
    if ($codigo -ne "201") {
        Remove-Item $archivo -ErrorAction SilentlyContinue
        Write-Host "El registro $i respondió $codigo (se esperaba 201). Se detiene."
        exit 1
    }
}
Remove-Item $archivo -ErrorAction SilentlyContinue

$ahora = Contar-Historial $docente
Write-Host "Listo: el historial tiene ahora $ahora corridas."
