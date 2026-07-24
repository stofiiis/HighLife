Add-Type -AssemblyName System.Drawing

$textureDirectory = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\src\main\resources\assets\highlife\textures\mob_effect'))

$palette = @{
    Transparent = [System.Drawing.Color]::Transparent
    Outline = [System.Drawing.Color]::FromArgb(255, 22, 24, 22)
    GreenDark = [System.Drawing.Color]::FromArgb(255, 25, 82, 46)
    Green = [System.Drawing.Color]::FromArgb(255, 61, 158, 79)
    GreenLight = [System.Drawing.Color]::FromArgb(255, 135, 223, 112)
    CyanDark = [System.Drawing.Color]::FromArgb(255, 15, 89, 100)
    Cyan = [System.Drawing.Color]::FromArgb(255, 54, 176, 190)
    CyanLight = [System.Drawing.Color]::FromArgb(255, 145, 235, 226)
    AmberDark = [System.Drawing.Color]::FromArgb(255, 120, 65, 24)
    Amber = [System.Drawing.Color]::FromArgb(255, 208, 127, 42)
    AmberLight = [System.Drawing.Color]::FromArgb(255, 248, 198, 91)
    HazeDark = [System.Drawing.Color]::FromArgb(255, 58, 69, 73)
    Haze = [System.Drawing.Color]::FromArgb(255, 105, 127, 130)
    HazeLight = [System.Drawing.Color]::FromArgb(255, 180, 207, 202)
}

function Fill {
    param($Graphics, $Color, [int]$X, [int]$Y, [int]$Width, [int]$Height)
    $brush = New-Object System.Drawing.SolidBrush $Color
    $Graphics.FillRectangle($brush, $X, $Y, $Width, $Height)
    $brush.Dispose()
}

function New-Icon {
    param([string]$Name, [scriptblock]$Draw)
    $bitmap = New-Object System.Drawing.Bitmap 18, 18, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.Clear($palette.Transparent)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
    & $Draw $graphics
    $bitmap.Save((Join-Path $textureDirectory "$Name.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $bitmap.Dispose()
}

New-Icon 'serenity' {
    param($g)
    Fill $g $palette.Outline 7 1 4 16
    Fill $g $palette.GreenDark 8 2 2 14
    Fill $g $palette.Outline 2 4 7 5
    Fill $g $palette.Green 3 5 6 3
    Fill $g $palette.GreenLight 4 5 3 1
    Fill $g $palette.Outline 9 8 7 5
    Fill $g $palette.Green 9 9 6 3
    Fill $g $palette.GreenLight 12 9 3 1
    Fill $g $palette.Outline 4 11 5 4
    Fill $g $palette.Green 5 12 4 2
}

New-Icon 'peace' {
    param($g)
    Fill $g $palette.Outline 7 1 4 16
    Fill $g $palette.CyanDark 8 2 2 14
    Fill $g $palette.Outline 2 4 7 5
    Fill $g $palette.Cyan 3 5 6 3
    Fill $g $palette.CyanLight 3 5 3 1
    Fill $g $palette.Outline 9 4 7 5
    Fill $g $palette.Cyan 9 5 6 3
    Fill $g $palette.CyanLight 12 5 3 1
    Fill $g $palette.Outline 4 12 10 4
    Fill $g $palette.Cyan 5 12 8 3
    Fill $g $palette.CyanLight 7 12 4 1
}

New-Icon 'thirst' {
    param($g)
    Fill $g $palette.Outline 7 1 4 3
    Fill $g $palette.Outline 5 4 8 3
    Fill $g $palette.Outline 3 7 12 7
    Fill $g $palette.Outline 5 14 8 3
    Fill $g $palette.AmberDark 8 3 2 3
    Fill $g $palette.Amber 6 5 6 3
    Fill $g $palette.Amber 4 8 10 5
    Fill $g $palette.Amber 6 13 6 2
    Fill $g $palette.AmberLight 6 7 3 4
    Fill $g $palette.Outline 9 7 2 3
    Fill $g $palette.Outline 8 10 2 2
    Fill $g $palette.Outline 7 12 2 3
}

New-Icon 'haze' {
    param($g)
    Fill $g $palette.Outline 2 5 14 9
    Fill $g $palette.HazeDark 3 4 5 3
    Fill $g $palette.HazeDark 10 3 4 4
    Fill $g $palette.Haze 3 7 12 6
    Fill $g $palette.HazeLight 4 7 9 2
    Fill $g $palette.Outline 5 9 8 4
    Fill $g $palette.HazeLight 6 10 6 2
    Fill $g $palette.HazeDark 8 10 2 2
    Fill $g $palette.Outline 4 14 10 2
}
