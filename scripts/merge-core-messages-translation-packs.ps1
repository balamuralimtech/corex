param(
    [string]$SourceDir = "translation-packs/coreMessages",
    [string]$OutputFile = "coreMessages.translation.csv"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $SourceDir)) {
    throw "Source directory not found: $SourceDir"
}

$packFiles = Get-ChildItem -Path $SourceDir -Recurse -Filter "coreMessages_*.csv" | Sort-Object Name
if (-not $packFiles -or $packFiles.Count -eq 0) {
    throw "No translation pack CSV files found under: $SourceDir"
}

$baseRows = $null
$localeMaps = @{}
$localeOrder = New-Object System.Collections.Generic.List[string]

foreach ($file in $packFiles) {
    $localeCode = [System.IO.Path]::GetFileNameWithoutExtension($file.Name).Substring("coreMessages_".Length)
    $rows = Import-Csv -Path $file.FullName -Encoding UTF8
    if (-not $rows -or $rows.Count -eq 0) {
        throw "Translation pack is empty: $($file.FullName)"
    }

    if ($null -eq $baseRows) {
        $baseRows = $rows
    } elseif ($rows.Count -ne $baseRows.Count) {
        throw "Row count mismatch in $($file.FullName)"
    }

    $localeOrder.Add($localeCode) | Out-Null
    $map = @{}
    foreach ($row in $rows) {
        $map[$row.key] = $row.target
    }
    $localeMaps[$localeCode] = $map
}

$mergedRows = foreach ($baseRow in $baseRows) {
    $row = [ordered]@{
        key  = $baseRow.key
        base = $baseRow.base
    }

    foreach ($localeCode in $localeOrder) {
        $row[$localeCode] = if ($localeMaps[$localeCode].ContainsKey($baseRow.key)) {
            $localeMaps[$localeCode][$baseRow.key]
        } else {
            ""
        }
    }

    [pscustomobject]$row
}

$mergedRows | Export-Csv -Path $OutputFile -NoTypeInformation -Encoding UTF8
Write-Output ("Merged translation packs into {0}" -f $OutputFile)
