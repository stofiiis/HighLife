Add-Type -AssemblyName System.Drawing

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$output = Join-Path $root "src/main/resources/assets/highlife/textures/gui"

function New-Canvas {
    $bitmap = [System.Drawing.Bitmap]::new(176, 166, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    for ($y = 0; $y -lt 166; $y++) {
        for ($x = 0; $x -lt 176; $x++) {
            $base = if ($y -lt 78) { "#18231B" } else { "#202821" }
            if ((($x + $y) % 7) -eq 0) {
                $base = if ($y -lt 78) { "#1B281E" } else { "#232C24" }
            }
            $bitmap.SetPixel($x, $y, [System.Drawing.ColorTranslator]::FromHtml($base))
        }
    }
    return $bitmap
}

function Set-Rect($bitmap, $x, $y, $width, $height, $color) {
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $brush = [System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml($color))
    $graphics.FillRectangle($brush, $x, $y, $width, $height)
    $brush.Dispose()
    $graphics.Dispose()
}

function Draw-Frame($bitmap, $x, $y, $width, $height, $accent) {
    Set-Rect $bitmap $x $y $width $height "#080D09"
    Set-Rect $bitmap ($x + 1) ($y + 1) ($width - 2) ($height - 2) "#344437"
    Set-Rect $bitmap ($x + 2) ($y + 2) ($width - 4) ($height - 4) $accent
    Set-Rect $bitmap ($x + 3) ($y + 3) ($width - 6) ($height - 6) "#1C271E"
}

function Draw-Slot($bitmap, $x, $y, $accent) {
    Set-Rect $bitmap ($x - 2) ($y - 2) 20 20 "#070A08"
    Set-Rect $bitmap ($x - 1) ($y - 1) 18 18 "#536056"
    Set-Rect $bitmap $x $y 16 16 "#0E130F"
    Set-Rect $bitmap ($x + 1) ($y + 1) 14 1 $accent
    Set-Rect $bitmap ($x + 1) ($y + 2) 1 13 "#344238"
}

function Draw-Inventory($bitmap, $accent) {
    Draw-Frame $bitmap 3 78 170 85 $accent
    Set-Rect $bitmap 7 82 162 56 "#182019"
    Set-Rect $bitmap 7 140 162 20 "#151C16"

    for ($row = 0; $row -lt 3; $row++) {
        for ($column = 0; $column -lt 9; $column++) {
            Draw-Slot $bitmap (8 + $column * 18) (84 + $row * 18) $accent
        }
    }
    for ($column = 0; $column -lt 9; $column++) {
        Draw-Slot $bitmap (8 + $column * 18) 142 $accent
    }
}

function Draw-Arrow($bitmap, $accent) {
    Set-Rect $bitmap 78 44 17 7 "#080C09"
    Set-Rect $bitmap 79 45 15 5 "#334138"
    Set-Rect $bitmap 94 41 4 13 "#080C09"
    Set-Rect $bitmap 95 42 4 11 "#334138"
    Set-Rect $bitmap 99 44 4 7 "#080C09"
    Set-Rect $bitmap 100 45 4 5 "#334138"
    Set-Rect $bitmap 80 46 13 1 $accent
}

function Draw-TopPanel($bitmap, $accent) {
    Draw-Frame $bitmap 3 3 170 73 $accent
    Set-Rect $bitmap 7 14 162 58 "#151E17"
    Set-Rect $bitmap 8 15 160 1 "#435748"
    Set-Rect $bitmap 74 20 2 48 "#33483A"
    Set-Rect $bitmap 108 20 2 48 "#33483A"
    Set-Rect $bitmap 75 20 1 48 $accent
    Draw-Arrow $bitmap $accent
}

function Draw-DeviceBadge($bitmap, $accent, $kind) {
    Draw-Frame $bitmap 120 28 38 38 $accent
    Set-Rect $bitmap 126 34 26 26 "#0C140E"

    if ($kind -eq "wand") {
        Set-Rect $bitmap 130 51 17 3 $accent
        Set-Rect $bitmap 134 47 13 3 $accent
        Set-Rect $bitmap 143 43 5 5 "#91C66D"
        Set-Rect $bitmap 146 40 3 3 "#B9DD83"
    } elseif ($kind -eq "flask") {
        Set-Rect $bitmap 137 37 5 7 "#7FD8E7"
        Set-Rect $bitmap 133 44 13 12 "#326B72"
        Set-Rect $bitmap 135 48 9 6 "#67D3E3"
        Set-Rect $bitmap 138 34 3 3 "#A7EEF2"
    } else {
        Set-Rect $bitmap 135 39 8 17 "#6EA457"
        Set-Rect $bitmap 129 46 20 5 "#6EA457"
        Set-Rect $bitmap 132 42 5 4 "#8DC86B"
        Set-Rect $bitmap 141 42 5 4 "#8DC86B"
        Set-Rect $bitmap 132 57 14 2 "#B89A43"
    }
}

function Save-Gui($name, $kind, $accent) {
    $bitmap = New-Canvas
    Draw-TopPanel $bitmap $accent
    Draw-Inventory $bitmap $accent

    if ($kind -eq "wand") {
        Draw-Slot $bitmap 44 39 $accent
    } elseif ($kind -eq "flask") {
        Draw-Slot $bitmap 44 26 $accent
        Draw-Slot $bitmap 44 52 $accent
    } else {
        Draw-Slot $bitmap 35 26 $accent
        Draw-Slot $bitmap 53 26 $accent
        Draw-Slot $bitmap 35 52 $accent
        Draw-Slot $bitmap 53 52 $accent
    }

    Draw-DeviceBadge $bitmap $accent $kind
    $path = Join-Path $output $name
    $bitmap.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bitmap.Dispose()
}

New-Item -ItemType Directory -Force -Path $output | Out-Null
Save-Gui "infusion_wand_control.png" "wand" "#4D8B5A"
Save-Gui "alchemy_flask_control.png" "flask" "#397E87"
Save-Gui "seed_mixer.png" "mixer" "#64834D"
