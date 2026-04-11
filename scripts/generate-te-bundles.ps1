param(
    [string[]]$Locales = @("te")
)

$ErrorActionPreference = "Stop"

function Protect-Tokens {
    param(
        [Parameter(Mandatory = $true)][string]$Text
    )

    $tokenPattern = '(\bUAT\b|\bZIP\b|\bISO\b|\bIP\b|\bAPI\b|\bDB\b|\bMC\b|\bSMTP\b|\bStartTLS\b|\bUTF-?8\b|\bID\b)'
    $matches = [regex]::Matches($Text, $tokenPattern)
    if ($matches.Count -eq 0) {
        return @{
            Protected = $Text
            Map = @{}
        }
    }

    $map = @{}
    $protected = [regex]::Replace($Text, $tokenPattern, {
            param($m)
            $key = "__PH$([Guid]::NewGuid().ToString('N'))__"
            $map[$key] = $m.Value
            return $key
        })

    return @{
        Protected = $protected
        Map = $map
    }
}

function Restore-Tokens {
    param(
        [Parameter(Mandatory = $true)][string]$Text,
        [Parameter(Mandatory = $true)][hashtable]$Map
    )

    $output = $Text
    foreach ($entry in $Map.GetEnumerator()) {
        $output = $output.Replace($entry.Key, $entry.Value)
    }
    return $output
}

function Invoke-GoogleTranslate {
    param(
        [Parameter(Mandatory = $true)][string]$Text,
        [Parameter(Mandatory = $true)][string]$TargetLang
    )

    if ([string]::IsNullOrWhiteSpace($Text)) {
        return $Text
    }

    $encoded = [System.Uri]::EscapeDataString($Text)
    $uri = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=$TargetLang&dt=t&q=$encoded"

    $maxAttempts = 4
    $response = $null
    for ($attempt = 1; $attempt -le $maxAttempts; $attempt++) {
        try {
            $response = Invoke-RestMethod -Uri $uri -Method Get -TimeoutSec 30
            break
        } catch {
            if ($attempt -eq $maxAttempts) {
                return $Text
            }
            Start-Sleep -Milliseconds (250 * $attempt)
        }
    }

    $segments = $response[0]
    if ($null -eq $segments) {
        return $Text
    }

    $translated = New-Object System.Text.StringBuilder
    foreach ($segment in $segments) {
        if ($null -ne $segment -and $segment.Count -gt 0 -and $null -ne $segment[0]) {
            [void]$translated.Append([string]$segment[0])
        }
    }
    $output = $translated.ToString()
    if ([string]::IsNullOrWhiteSpace($output)) {
        return $Text
    }
    return $output
}

function Should-SkipTranslation {
    param(
        [Parameter(Mandatory = $true)][string]$Value
    )

    if ([string]::IsNullOrWhiteSpace($Value)) {
        return $true
    }

    # Keep slugs/codes untouched (e.g., application-notifications, jdbc.url-like values).
    if ($Value -match '^[A-Za-z0-9._/\-]+$' -and $Value -match '[._/\-]') {
        return $true
    }

    # Keep uppercase acronyms/codes untouched.
    if ($Value -cmatch '^[A-Z0-9]{2,}$') {
        return $true
    }

    # Keep values that look like pure placeholders/tokens untouched.
    if ($Value -cmatch '^(?:\{[0-9]+\}|%[0-9]+\$s|%s|\b[A-Z0-9_]+\b)(?:\s*(?:,|/|-)\s*(?:\{[0-9]+\}|%[0-9]+\$s|%s|\b[A-Z0-9_]+\b))*$') {
        return $true
    }

    return $false
}

function Translate-TextSafely {
    param(
        [Parameter(Mandatory = $true)][string]$Text,
        [Parameter(Mandatory = $true)][string]$TargetLang
    )

    if ([string]::IsNullOrWhiteSpace($Text)) {
        return $Text
    }

    $placeholderPattern = '(\{[0-9]+\}|%[0-9]+\$s|%s)'
    $parts = [regex]::Split($Text, $placeholderPattern)
    $sb = New-Object System.Text.StringBuilder

    foreach ($part in $parts) {
        if ([string]::IsNullOrEmpty($part)) {
            continue
        }

        if ($part -match "^$placeholderPattern$") {
            [void]$sb.Append($part)
            continue
        }

        $leadingWs = ([regex]::Match($part, '^\s*')).Value
        $trailingWs = ([regex]::Match($part, '\s*$')).Value
        $partCore = $part.Trim()
        if ([string]::IsNullOrEmpty($partCore)) {
            [void]$sb.Append($part)
            continue
        }

        $protectedInfo = Protect-Tokens -Text $partCore
        $translated = Invoke-GoogleTranslate -Text $protectedInfo.Protected -TargetLang $TargetLang
        $restored = Restore-Tokens -Text $translated -Map $protectedInfo.Map
        [void]$sb.Append($leadingWs + $restored + $trailingWs)
    }

    return $sb.ToString()
}

function Translate-PropertiesFile {
    param(
        [Parameter(Mandatory = $true)][string]$SourcePath,
        [Parameter(Mandatory = $true)][string]$OutputPath,
        [Parameter(Mandatory = $true)][string]$TargetLang
    )

    Write-Host "Translating: $SourcePath -> $OutputPath"
    $lines = Get-Content -Path $SourcePath -Encoding UTF8
    $out = New-Object System.Collections.Generic.List[string]
    $cache = @{}

    foreach ($line in $lines) {
        if ($line -match '^\s*[#!]') {
            $out.Add($line)
            continue
        }

        if ($line -notmatch '^(.*?)=(.*)$') {
            $out.Add($line)
            continue
        }

        $key = $matches[1]
        $value = $matches[2]

        if (Should-SkipTranslation -Value $value) {
            $out.Add("$key=$value")
            continue
        }

        if ($cache.ContainsKey($value)) {
            $out.Add("$key=$($cache[$value])")
            continue
        }

        $leadingWs = ([regex]::Match($value, '^\s*')).Value
        $trailingWs = ([regex]::Match($value, '\s*$')).Value
        $trimmed = $value.Trim()

        $restored = Translate-TextSafely -Text $trimmed -TargetLang $TargetLang
        $final = "$leadingWs$restored$trailingWs"

        $cache[$value] = $final
        $out.Add("$key=$final")

        Start-Sleep -Milliseconds 25
    }

    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    $fullOutputPath = [System.IO.Path]::GetFullPath($OutputPath)
    [System.IO.File]::WriteAllLines($fullOutputPath, $out, $utf8NoBom)
}

$targetBases = @(
    @{
        Source = "corex-web/src/main/resources/coreAppMessages.properties"
        OutputPrefix = "corex-web/src/main/resources/coreAppMessages"
    },
    @{
        Source = "applications/carex/carex-web/src/main/resources/carexAppMessages.properties"
        OutputPrefix = "applications/carex/carex-web/src/main/resources/carexAppMessages"
    }
)

foreach ($locale in $Locales) {
    $splitLocales = $locale -split ","
    foreach ($localePart in $splitLocales) {
        $lang = $localePart.Trim().ToLowerInvariant()
        if ([string]::IsNullOrWhiteSpace($lang)) {
            continue
        }

        foreach ($target in $targetBases) {
            $output = "$($target.OutputPrefix)_$lang.properties"
            Translate-PropertiesFile -SourcePath $target.Source -OutputPath $output -TargetLang $lang
        }
    }
}

Write-Host ("Locale bundle generation completed for: " + ($Locales -join ", "))
