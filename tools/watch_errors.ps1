param(
    [Parameter(Mandatory = $true)]
    [string]$ProjectRoot
)

$ErrorActionPreference = "Continue"

$logsDir = Join-Path $ProjectRoot "run\\logs"
$latestLog = Join-Path $logsDir "latest.log"
$errorLog = Join-Path $logsDir "weed_error_watch.log"

New-Item -ItemType Directory -Force -Path $logsDir | Out-Null
Add-Content -Path $errorLog -Value ("`n=== Error watch started {0:u} ===" -f (Get-Date))

Write-Output ("Watching: {0}" -f $latestLog)
Write-Output ("Writing errors to: {0}" -f $errorLog)

while (-not (Test-Path $latestLog)) {
    Start-Sleep -Milliseconds 500
}

$stackLinesLeft = 0

Get-Content -Path $latestLog -Wait | ForEach-Object {
    $line = $_
    $timestamped = ("{0:u} {1}" -f (Get-Date), $line)

    if ($line -match "ERROR|FATAL|Exception|Caused by:") {
        Add-Content -Path $errorLog -Value $timestamped
        Write-Output $timestamped
        if ($line -match "Exception|Caused by:") {
            $stackLinesLeft = 30
        } else {
            $stackLinesLeft = 0
        }
        return
    }

    if ($stackLinesLeft -gt 0) {
        if ($line -match "^\\s*at\\s" -or $line -match "^\\s*\\.\\.\\.\\s+\\d+\\s+more" -or $line -match "^\\s*Suppressed:") {
            Add-Content -Path $errorLog -Value $timestamped
            Write-Output $timestamped
            $stackLinesLeft--
            return
        }

        if ([string]::IsNullOrWhiteSpace($line)) {
            $stackLinesLeft = 0
        }
    }
}
