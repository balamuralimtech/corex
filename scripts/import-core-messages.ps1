param(
    [string]$CsvFile = "coreMessages.translation.csv",
    [string]$ResourceDir = "corex-web/src/main/resources"
)

$ErrorActionPreference = "Stop"

function Get-PropertyMap {
    param([string]$Path)

    $map = [ordered]@{}
    foreach ($line in Get-Content -Path $Path -Encoding UTF8) {
        if ($line -match '^\s*$' -or $line -match '^\s*[#!]') {
            continue
        }

        $separatorIndex = $line.IndexOf('=')
        if ($separatorIndex -lt 0) {
            continue
        }

        $key = $line.Substring(0, $separatorIndex).Trim()
        $value = $line.Substring($separatorIndex + 1)
        if (-not $map.Contains($key)) {
            $map[$key] = $value
        }
    }

    return $map
}

function Write-PropertyMap {
    param(
        [string]$Path,
        [hashtable]$Map
    )

    $lines = foreach ($entry in $Map.GetEnumerator()) {
        "$($entry.Key)=$($entry.Value)"
    }

    Set-Content -Path $Path -Value $lines -Encoding UTF8
}

if (-not (Test-Path $CsvFile)) {
    throw "CSV file not found: $CsvFile"
}

$rows = Import-Csv -Path $CsvFile -Encoding UTF8
if (-not $rows) {
    throw "CSV file is empty: $CsvFile"
}

$headers = @($rows[0].PSObject.Properties.Name)
if ($headers.Count -lt 3 -or $headers[0] -ne "key" -or $headers[1] -ne "base") {
    throw "CSV format must start with columns: key,base,<locale...>"
}

$basePath = Join-Path $ResourceDir "coreMessages.properties"
$baseMap = [ordered]@{}
foreach ($row in $rows) {
    $baseMap[$row.key] = $row.base
}
Write-PropertyMap -Path $basePath -Map $baseMap

$localeCodes = $headers | Select-Object -Skip 2
foreach ($localeCode in $localeCodes) {
    $localePath = Join-Path $ResourceDir ("coreMessages_{0}.properties" -f $localeCode)
    $existingMap = if (Test-Path $localePath) { Get-PropertyMap -Path $localePath } else { [ordered]@{} }
    $localeMap = [ordered]@{}

    foreach ($row in $rows) {
        $value = $row.$localeCode
        if ([string]::IsNullOrEmpty($value)) {
            if ($existingMap.Contains($row.key)) {
                $value = $existingMap[$row.key]
            } else {
                $value = $row.base
            }
        }
        $localeMap[$row.key] = $value
    }

    Write-PropertyMap -Path $localePath -Map $localeMap
}

Write-Output "Imported translations from $CsvFile"
