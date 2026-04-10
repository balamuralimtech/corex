param(
    [string]$ResourceDir = "corex-web/src/main/resources",
    [string]$BaseFile = "coreMessages.properties",
    [string]$OutputFile = "coreMessages.translation.csv"
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

$basePath = Join-Path $ResourceDir $BaseFile
if (-not (Test-Path $basePath)) {
    throw "Base bundle not found: $basePath"
}

$baseMap = Get-PropertyMap -Path $basePath
$localeFiles = Get-ChildItem -Path $ResourceDir -Filter "coreMessages_*.properties" | Sort-Object Name
$localeCodes = @()
$localeMaps = @{}

foreach ($file in $localeFiles) {
    $localeCode = [System.IO.Path]::GetFileNameWithoutExtension($file.Name).Substring("coreMessages_".Length)
    $localeCodes += $localeCode
    $localeMaps[$localeCode] = Get-PropertyMap -Path $file.FullName
}

$rows = foreach ($entry in $baseMap.GetEnumerator()) {
    $row = [ordered]@{
        key  = $entry.Key
        base = $entry.Value
    }

    foreach ($localeCode in $localeCodes) {
        $row[$localeCode] = if ($localeMaps[$localeCode].Contains($entry.Key)) {
            $localeMaps[$localeCode][$entry.Key]
        } else {
            ""
        }
    }

    [pscustomobject]$row
}

$rows | Export-Csv -Path $OutputFile -Encoding UTF8 -NoTypeInformation
Write-Output "Exported $($rows.Count) keys to $OutputFile"
