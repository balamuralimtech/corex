param()

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent $scriptDir
$installerJar = Join-Path $repoRoot "corex-installer/target/corex-installer-jar-with-dependencies.jar"

if (-not (Test-Path -LiteralPath $installerJar)) {
    Write-Host "Installer jar not found. Building corex-installer first..."
    mvn -pl corex-installer -am package
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to build corex-installer."
    }
}

& java -jar $installerJar
