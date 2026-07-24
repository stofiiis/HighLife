Add-Type -AssemblyName System.Drawing

$textureDirectory = Join-Path $PSScriptRoot '..\src\main\resources\assets\highlife\textures\block'
$textureDirectory = [System.IO.Path]::GetFullPath($textureDirectory)

$palette = @{
    Transparent = [System.Drawing.Color]::Transparent
    Outline = [System.Drawing.Color]::FromArgb(255, 24, 19, 18)
    StoneDark = [System.Drawing.Color]::FromArgb(255, 49, 51, 48)
    Stone = [System.Drawing.Color]::FromArgb(255, 82, 84, 76)
    StoneLight = [System.Drawing.Color]::FromArgb(255, 132, 132, 116)
    WoodDark = [System.Drawing.Color]::FromArgb(255, 63, 37, 22)
    Wood = [System.Drawing.Color]::FromArgb(255, 111, 65, 31)
    WoodLight = [System.Drawing.Color]::FromArgb(255, 174, 108, 48)
    BrassDark = [System.Drawing.Color]::FromArgb(255, 126, 82, 23)
    Brass = [System.Drawing.Color]::FromArgb(255, 207, 146, 42)
    BrassLight = [System.Drawing.Color]::FromArgb(255, 244, 199, 75)
    GreenDark = [System.Drawing.Color]::FromArgb(255, 25, 67, 32)
    Green = [System.Drawing.Color]::FromArgb(255, 52, 125, 58)
    GreenLight = [System.Drawing.Color]::FromArgb(255, 104, 174, 77)
    TealDark = [System.Drawing.Color]::FromArgb(255, 16, 86, 83)
    Teal = [System.Drawing.Color]::FromArgb(255, 35, 157, 145)
    Cyan = [System.Drawing.Color]::FromArgb(255, 88, 219, 202)
    AmberDark = [System.Drawing.Color]::FromArgb(255, 112, 67, 25)
    Amber = [System.Drawing.Color]::FromArgb(255, 177, 109, 38)
    AmberLight = [System.Drawing.Color]::FromArgb(255, 225, 165, 67)
}

function New-Texture {
    param(
        [string]$Name,
        [scriptblock]$Draw
    )

    $bitmap = New-Object System.Drawing.Bitmap 16, 16, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.Clear($palette.Transparent)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half

    & $Draw $graphics

    $outputPath = Join-Path $textureDirectory "$Name.png"
    $bitmap.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $bitmap.Dispose()
}

function Fill-Rect {
    param(
        [System.Drawing.Graphics]$Graphics,
        [System.Drawing.Color]$Color,
        [int]$X,
        [int]$Y,
        [int]$Width,
        [int]$Height
    )

    $brush = New-Object System.Drawing.SolidBrush $Color
    $Graphics.FillRectangle($brush, $X, $Y, $Width, $Height)
    $brush.Dispose()
}

function Set-Pixel {
    param(
        [System.Drawing.Graphics]$Graphics,
        [System.Drawing.Color]$Color,
        [int]$X,
        [int]$Y
    )

    Fill-Rect $Graphics $Color $X $Y 1 1
}

function Draw-Leaf {
    param(
        [System.Drawing.Graphics]$Graphics,
        [int]$X,
        [int]$Y,
        [bool]$Flip = $false,
        [bool]$Large = $false
    )

    $width = if ($Large) { 4 } else { 3 }
    $height = if ($Large) { 3 } else { 2 }
    $highlightX = if ($Flip) { $X + 2 } else { $X + 1 }
    Fill-Rect $Graphics $palette.Outline $X $Y $width $height

    if ($Large) {
        Set-Pixel $Graphics $palette.Transparent $X $Y
        Set-Pixel $Graphics $palette.Transparent ($X + 3) ($Y + 2)
        Fill-Rect $Graphics $palette.Green ($X + 1) $Y 2 2
        Set-Pixel $Graphics $palette.GreenLight $highlightX $Y
    } else {
        Set-Pixel $Graphics $palette.Green ($X + 1) $Y
        Fill-Rect $Graphics $palette.Green $X ($Y + 1) 3 1
        if (!$Flip) {
            $highlightX = $X
        }
        Set-Pixel $Graphics $palette.GreenLight $highlightX ($Y + 1)
    }
}

New-Texture 'seed_mixer' {
    param($graphics)
    Fill-Rect $graphics $palette.StoneDark 0 0 16 16
    Fill-Rect $graphics $palette.Stone 1 1 14 14
    Fill-Rect $graphics $palette.StoneLight 2 2 12 1
    Fill-Rect $graphics $palette.StoneDark 2 13 12 1
    Fill-Rect $graphics $palette.StoneDark 3 4 2 2
    Fill-Rect $graphics $palette.StoneLight 11 9 2 2

    foreach ($point in @(@(1, 1), @(13, 1), @(1, 13), @(13, 13))) {
        Fill-Rect $graphics $palette.BrassDark $point[0] $point[1] 2 2
        Set-Pixel $graphics $palette.BrassLight $point[0] $point[1]
    }
    Fill-Rect $graphics $palette.TealDark 6 13 4 2
    Fill-Rect $graphics $palette.Teal 7 13 2 2
    Set-Pixel $graphics $palette.Cyan 7 13
}

New-Texture 'seed_mixer_bowl' {
    param($graphics)
    Fill-Rect $graphics $palette.StoneDark 0 0 16 16
    Fill-Rect $graphics $palette.Stone 1 1 14 14
    Fill-Rect $graphics $palette.StoneLight 2 2 12 3
    Fill-Rect $graphics $palette.StoneDark 2 12 12 3
    Fill-Rect $graphics $palette.StoneLight 3 6 2 4
    Fill-Rect $graphics $palette.StoneDark 11 6 2 4
    Set-Pixel $graphics $palette.Brass 3 3
    Set-Pixel $graphics $palette.BrassLight 4 3
}

New-Texture 'seed_mixer_inner' {
    param($graphics)
    Fill-Rect $graphics $palette.Outline 0 0 16 16
    Fill-Rect $graphics $palette.GreenDark 1 1 14 14
    Fill-Rect $graphics $palette.Green 3 4 4 3
    Fill-Rect $graphics $palette.GreenLight 9 3 3 4
    Fill-Rect $graphics $palette.TealDark 5 10 5 3
    Set-Pixel $graphics $palette.Cyan 6 10
    Set-Pixel $graphics $palette.BrassLight 11 9
}

New-Texture 'seed_mixer_pestle' {
    param($graphics)
    Fill-Rect $graphics $palette.StoneDark 0 0 16 16
    Fill-Rect $graphics $palette.Stone 2 0 12 16
    Fill-Rect $graphics $palette.StoneLight 3 0 3 16
    Fill-Rect $graphics $palette.BrassDark 2 12 12 3
    Fill-Rect $graphics $palette.Brass 3 12 10 2
    Set-Pixel $graphics $palette.BrassLight 4 12
}

New-Texture 'drying_rack' {
    param($graphics)
    Fill-Rect $graphics $palette.WoodDark 0 0 16 16
    Fill-Rect $graphics $palette.Wood 1 1 14 14
    Fill-Rect $graphics $palette.WoodLight 2 2 12 2
    Fill-Rect $graphics $palette.WoodDark 2 7 12 2
    Fill-Rect $graphics $palette.WoodLight 2 10 12 2
    Fill-Rect $graphics $palette.WoodDark 2 14 12 1
    foreach ($point in @(@(1, 1), @(13, 1), @(1, 13), @(13, 13))) {
        Set-Pixel $graphics $palette.Brass $point[0] $point[1]
        Set-Pixel $graphics $palette.BrassLight ($point[0] + 1) $point[1]
    }
}

New-Texture 'drying_rack_occupied' {
    param($graphics)
    Fill-Rect $graphics $palette.AmberDark 7 1 2 3
    Fill-Rect $graphics $palette.Brass 6 0 4 1
    Fill-Rect $graphics $palette.Amber 4 4 4 3
    Fill-Rect $graphics $palette.Amber 8 5 4 3
    Fill-Rect $graphics $palette.AmberDark 3 6 4 4
    Fill-Rect $graphics $palette.AmberDark 9 7 4 4
    Fill-Rect $graphics $palette.Amber 5 9 6 4
    Fill-Rect $graphics $palette.AmberDark 6 12 4 3
    Set-Pixel $graphics $palette.AmberLight 5 4
    Set-Pixel $graphics $palette.AmberLight 9 5
    Set-Pixel $graphics $palette.AmberLight 6 9
    Set-Pixel $graphics $palette.GreenDark 4 8
    Set-Pixel $graphics $palette.GreenDark 11 9
}

New-Texture 'mystic_herb_crop_stage0' {
    param($graphics)
    Fill-Rect $graphics $palette.Outline 7 10 3 6
    Fill-Rect $graphics $palette.GreenDark 8 9 1 7
    Draw-Leaf $graphics 5 9 $false $false
    Draw-Leaf $graphics 9 8 $true $false
    Set-Pixel $graphics $palette.Teal 8 11
}

New-Texture 'mystic_herb_crop_stage1' {
    param($graphics)
    Fill-Rect $graphics $palette.Outline 7 5 3 11
    Fill-Rect $graphics $palette.GreenDark 8 4 1 12
    Draw-Leaf $graphics 4 10 $false $true
    Draw-Leaf $graphics 9 8 $true $true
    Draw-Leaf $graphics 5 5 $false $false
    Draw-Leaf $graphics 9 4 $true $false
    Set-Pixel $graphics $palette.Teal 8 7
}

New-Texture 'mystic_herb_crop_stage2' {
    param($graphics)
    Fill-Rect $graphics $palette.Outline 7 2 3 14
    Fill-Rect $graphics $palette.GreenDark 8 1 1 15
    Draw-Leaf $graphics 3 11 $false $true
    Draw-Leaf $graphics 10 10 $true $true
    Draw-Leaf $graphics 4 7 $false $true
    Draw-Leaf $graphics 9 5 $true $true
    Draw-Leaf $graphics 5 2 $false $false
    Draw-Leaf $graphics 9 1 $true $false
    Set-Pixel $graphics $palette.Teal 8 5
    Set-Pixel $graphics $palette.Cyan 10 5
}

New-Texture 'mystic_herb_crop_stage3' {
    param($graphics)
    Fill-Rect $graphics $palette.Outline 7 1 3 15
    Fill-Rect $graphics $palette.GreenDark 8 0 1 16
    Draw-Leaf $graphics 2 12 $false $true
    Draw-Leaf $graphics 11 11 $true $true
    Draw-Leaf $graphics 3 8 $false $true
    Draw-Leaf $graphics 10 7 $true $true
    Draw-Leaf $graphics 4 4 $false $true
    Draw-Leaf $graphics 9 3 $true $true
    Draw-Leaf $graphics 5 0 $false $false
    Draw-Leaf $graphics 9 0 $true $false
    foreach ($point in @(@(4, 7), @(11, 6), @(5, 3))) {
        Set-Pixel $graphics $palette.BrassDark $point[0] $point[1]
        Set-Pixel $graphics $palette.BrassLight ($point[0] + 1) $point[1]
    }
    Set-Pixel $graphics $palette.Teal 8 4
    Set-Pixel $graphics $palette.Cyan 10 3
}
