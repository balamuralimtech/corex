param(
    [string]$CsvFile = "coreMessages.translation.csv",
    [string]$OutputDir = "translation-packs/coreMessages"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $CsvFile)) {
    throw "CSV file not found: $CsvFile"
}

$rows = Import-Csv -Path $CsvFile -Encoding UTF8
if (-not $rows -or $rows.Count -eq 0) {
    throw "CSV file is empty: $CsvFile"
}

$headers = @($rows[0].PSObject.Properties.Name)
if ($headers.Count -lt 3 -or $headers[0] -ne "key" -or $headers[1] -ne "base") {
    throw "CSV format must start with columns: key,base,<locale...>"
}

$localeCodes = $headers | Select-Object -Skip 2

New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

foreach ($localeCode in $localeCodes) {
    $localeDir = Join-Path $OutputDir $localeCode
    New-Item -ItemType Directory -Force -Path $localeDir | Out-Null

    $packRows = foreach ($row in $rows) {
        [pscustomobject]@{
            key              = $row.key
            base             = $row.base
            target           = $row.$localeCode
            review_status    = ""
            reviewer_notes   = ""
        }
    }

    $packFile = Join-Path $localeDir ("coreMessages_{0}.csv" -f $localeCode)
    $packRows | Export-Csv -Path $packFile -NoTypeInformation -Encoding UTF8
    Write-Output ("Created {0}" -f $packFile)
}
