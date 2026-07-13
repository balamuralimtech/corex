param(
    [string]$HostName = "127.0.0.1",
    [string]$Port = "3306",
    [string]$User,
    [string]$Password,
    [string]$Database,
    [ValidateSet("corex", "shipx", "carex", "payrollx", "all")]
    [string]$Target,
    [switch]$AppOnly
)

$ErrorActionPreference = "Stop"

function Fail {
    param([string]$Message)
    throw $Message
}

function Trim-Line {
    param([string]$Line)
    if ($null -eq $Line) { return "" }
    return $Line.Trim()
}

function Invoke-MySqlFile {
    param([string]$SqlPath)

    $args = @(
        "--host=$HostName",
        "--port=$Port",
        "--user=$User",
        "--database=$Database"
    )

    if ($Password) {
        $args += "--password=$Password"
    }

    Get-Content -Raw -Path $SqlPath | & mysql @args
    if ($LASTEXITCODE -ne 0) {
        Fail "mysql failed while executing $SqlPath"
    }
}

function Invoke-Manifest {
    param(
        [string]$ManifestPath,
        [string]$BaseDir,
        [string]$Label
    )

    if (-not (Test-Path -LiteralPath $ManifestPath)) {
        Fail "Manifest not found: $ManifestPath"
    }

    Write-Host "Installing $Label schema from $ManifestPath"

    $executed = $false
    foreach ($rawLine in Get-Content -Path $ManifestPath) {
        $line = Trim-Line $rawLine
        if ([string]::IsNullOrWhiteSpace($line)) { continue }
        if ($line.StartsWith("#")) { continue }

        $sqlPath = Join-Path $BaseDir $line
        if (-not (Test-Path -LiteralPath $sqlPath)) {
            Fail "SQL file listed in $ManifestPath does not exist: $line"
        }

        Write-Host "  -> $line"
        Invoke-MySqlFile -SqlPath $sqlPath
        $executed = $true
    }

    if (-not $executed) {
        Write-Host "  -> no SQL files listed; skipped"
    }
}

if (-not $User) { Fail "--User is required" }
if (-not $Database) { Fail "--Database is required" }
if (-not $Target) { Fail "--Target is required" }
if (-not (Get-Command mysql -ErrorAction SilentlyContinue)) {
    Fail "mysql client is required but was not found in PATH"
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent $scriptDir

$manifestCorex = Join-Path $repoRoot "corex-db/install-order.txt"
$manifestShipx = Join-Path $repoRoot "applications/shipx/shipx-db/install-order.txt"
$manifestCarex = Join-Path $repoRoot "applications/carex/carex-db/install-order.txt"
$manifestPayrollx = Join-Path $repoRoot "applications/payrollx/payrollx-db/install-order.txt"

function Install-Corex {
    Invoke-Manifest -ManifestPath $manifestCorex -BaseDir (Join-Path $repoRoot "corex-db") -Label "CoreX"
}

function Install-Shipx {
    Invoke-Manifest -ManifestPath $manifestShipx -BaseDir (Join-Path $repoRoot "applications/shipx/shipx-db") -Label "ShipX"
}

function Install-Carex {
    Invoke-Manifest -ManifestPath $manifestCarex -BaseDir (Join-Path $repoRoot "applications/carex/carex-db") -Label "CareX"
}

function Install-Payrollx {
    Invoke-Manifest -ManifestPath $manifestPayrollx -BaseDir (Join-Path $repoRoot "applications/payrollx/payrollx-db") -Label "PayrollX"
}

switch ($Target) {
    "corex" { Install-Corex }
    "shipx" {
        if (-not $AppOnly) { Install-Corex }
        Install-Shipx
    }
    "carex" {
        if (-not $AppOnly) { Install-Corex }
        Install-Carex
    }
    "payrollx" {
        if (-not $AppOnly) { Install-Corex }
        Install-Payrollx
    }
    "all" {
        Install-Corex
        Install-Shipx
        Install-Carex
        Install-Payrollx
    }
}

Write-Host "Database installation complete for target: $Target"
