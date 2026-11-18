# Cambia la vigencia del access token del realm numerico-realm (RA-07), sin tocar el archivo del realm.
# Para la demostración: 60 segundos. Para dejarlo como estaba: 300.
#
# Uso (PowerShell, desde la raíz del repo, con el sistema arriba):
#   .\scripts\vigencia-token.ps1 -Segundos 60
#   .\scripts\vigencia-token.ps1 -Segundos 300
# La clave del administrador de Keycloak sale de la variable KEYCLOAK_ADMIN_PASSWORD (por defecto "cambiar",
# igual que en docker-compose.yml). Si usted la cambió, pásela con -Clave.
param(
    [int]$Segundos = 60,
    [string]$Clave = $(if ($env:KEYCLOAK_ADMIN_PASSWORD) { $env:KEYCLOAK_ADMIN_PASSWORD } else { "cambiar" })
)

$keycloak = "http://localhost:8180"
$realm    = "numerico-realm"

# 1. Token del administrador (realm master, cliente admin-cli).
$json = curl.exe -s -X POST "$keycloak/realms/master/protocol/openid-connect/token" `
    -d "grant_type=password" -d "client_id=admin-cli" -d "username=admin" -d "password=$Clave"
$admin = ($json | ConvertFrom-Json).access_token
if (-not $admin) {
    Write-Host "No se pudo obtener el token del administrador. Revise la clave (-Clave) y que Keycloak esté arriba."
    exit 1
}

# 2. Actualización parcial del realm: solo la vigencia. El cuerpo va desde un archivo temporal
#    porque PowerShell 5.1 pierde las comillas del JSON si se pasa directo a curl.exe.
$temporal = [System.IO.Path]::GetTempFileName()
[System.IO.File]::WriteAllText($temporal, "{`"accessTokenLifespan`":$Segundos}")
$codigo = curl.exe -s -o NUL -w "%{http_code}" -X PUT "$keycloak/admin/realms/$realm" `
    -H "Authorization: Bearer $admin" -H "Content-Type: application/json" --data-binary "@$temporal"
Remove-Item $temporal -ErrorAction SilentlyContinue
if ($codigo -ne "204") {
    Write-Host "Keycloak respondió $codigo al actualizar el realm (se esperaba 204)."
    exit 1
}

# 3. Comprobación: se lee el valor guardado.
$realmActual = curl.exe -s -H "Authorization: Bearer $admin" "$keycloak/admin/realms/$realm" | ConvertFrom-Json
Write-Host "Vigencia del access token en $realm : $($realmActual.accessTokenLifespan) segundos."
Write-Host "Los tokens que ya estaban emitidos conservan su vigencia anterior: cierre sesión e inicie otra vez."
