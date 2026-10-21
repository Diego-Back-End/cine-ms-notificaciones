# Levanta ms-notificaciones en local usando las variables de .env.local
Set-Location $PSScriptRoot

$env:Path = "$env:SystemRoot\System32\WindowsPowerShell\v1.0;$env:Path"

$envFile = Join-Path $PSScriptRoot ".env.local"

if (-not (Test-Path $envFile)) {
    Write-Host ""
    Write-Host "No existe el archivo .env.local." -ForegroundColor Yellow
    Write-Host "Copia .env.local.example a .env.local y completa los valores (correo y RabbitMQ)." -ForegroundColor Yellow
    Write-Host "Ejemplo:" -ForegroundColor Yellow
    Write-Host "  Copy-Item .env.local.example .env.local" -ForegroundColor Cyan
    Write-Host ""
    exit 1
}

Get-Content $envFile | ForEach-Object {
    $linea = $_.Trim()
    if ($linea -eq "" -or $linea.StartsWith("#")) { return }

    $idx = $linea.IndexOf("=")
    if ($idx -lt 1) { return }

    $nombre = $linea.Substring(0, $idx).Trim()
    $valor = $linea.Substring($idx + 1).Trim()

    Set-Item -Path "Env:$nombre" -Value $valor
}

.\mvnw.cmd spring-boot:run
