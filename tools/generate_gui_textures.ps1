Add-Type -AssemblyName System.Drawing

$textureDirectory = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\src\main\resources\assets\highlife\textures\gui'))

$colors = @{
    Outline = [System.Drawing.Color]::FromArgb(255, 25, 20, 17)
    WoodDark = [System.Drawing.Color]::FromArgb(255, 52, 34, 24)
    Wood = [System.Drawing.Color]::FromArgb(255, 79, 49, 31)
    WoodLight = [System.Drawing.Color]::FromArgb(255, 117, 72, 39)
    Panel = [System.Drawing.Color]::FromArgb(255, 28, 31, 28)
    PanelLight = [System.Drawing.Color]::FromArgb(255, 39, 44, 39)
    Slot = [System.Drawing.Color]::FromArgb(255, 18, 21, 20)
    SlotLight = [System.Drawing.Color]::FromArgb(255, 67, 70, 63)
    BrassDark = [System.Drawing.Color]::FromArgb(255, 128, 84, 25)
    Brass = [System.Drawing.Color]::FromArgb(255, 205, 147, 45)
    BrassLight = [System.Drawing.Color]::FromArgb(255, 244, 202, 83)
    GreenDark = [System.Drawing.Color]::FromArgb(255, 29, 68, 35)
    Green = [System.Drawing.Color]::FromArgb(255, 62, 122, 56)
    CyanDark = [System.Drawing.Color]::FromArgb(255, 18, 75, 82)
    Cyan = [System.Drawing.Color]::FromArgb(255, 42, 153, 166)
    Bone = [System.Drawing.Color]::FromArgb(255, 218, 215, 184)
    Dirt = [System.Drawing.Color]::FromArgb(255, 116, 76, 43)
}

function Fill {
    param($Graphics, $Color, [int]$X, [int]$Y, [int]$Width, [int]$Height)
    $brush = New-Object System.Drawing.SolidBrush $Color
    $Graphics.FillRectangle($brush, $X, $Y, $Width, $Height)
    $brush.Dispose()
}

function Draw-Slot {
    param($Graphics, [int]$X, [int]$Y)
    Fill $Graphics $colors.Outline $X $Y 18 18
    Fill $Graphics $colors.SlotLight ($X + 1) ($Y + 1) 16 16
    Fill $Graphics $colors.Slot ($X + 2) ($Y + 2) 14 14
}

function Draw-Frame {
    param($Graphics, $AccentDark, $Accent)

    Fill $Graphics $colors.Outline 0 0 176 166
    Fill $Graphics $colors.WoodDark 1 1 174 164
    Fill $Graphics $colors.Wood 3 3 170 160
    Fill $Graphics $colors.Panel 5 18 166 62
    Fill $Graphics $colors.PanelLight 7 20 162 58
    Fill $Graphics $colors.Panel 5 82 166 80

    Fill $Graphics $AccentDark 3 3 170 3
    Fill $Graphics $Accent 4 4 168 1
    Fill $Graphics $colors.BrassDark 3 3 5 5
    Fill $Graphics $colors.Brass 4 4 3 3
    Fill $Graphics $colors.BrassDark 168 3 5 5
    Fill $Graphics $colors.Brass 169 4 3 3
    Fill $Graphics $colors.BrassDark 3 158 5 5
    Fill $Graphics $colors.Brass 4 159 3 3
    Fill $Graphics $colors.BrassDark 168 158 5 5
    Fill $Graphics $colors.Brass 169 159 3 3

    for ($row = 0; $row -lt 3; $row++) {
        for ($column = 0; $column -lt 9; $column++) {
            Draw-Slot $Graphics (7 + $column * 18) (83 + $row * 18)
        }
    }
    for ($column = 0; $column -lt 9; $column++) {
        Draw-Slot $Graphics (7 + $column * 18) 141
    }

    Fill $Graphics $colors.WoodDark 5 136 166 3
    Fill $Graphics $colors.BrassDark 5 136 166 1
    Fill $Graphics $colors.BrassDark 74 22 2 52
    Fill $Graphics $colors.Brass 75 23 1 50
    Fill $Graphics $colors.BrassDark 108 22 2 52
    Fill $Graphics $colors.Brass 108 23 1 50
}

function Draw-Arrow {
    param($Graphics, $Color)
    Fill $Graphics $colors.Outline 79 45 15 5
    Fill $Graphics $colors.Outline 93 42 5 11
    Fill $Graphics $Color 80 46 14 3
    Fill $Graphics $Color 94 44 2 7
}

function Draw-HerbGhost {
    param($Graphics, [int]$X, [int]$Y, $Color)
    Fill $Graphics $Color ($X + 8) ($Y + 4) 2 10
    Fill $Graphics $Color ($X + 4) ($Y + 6) 5 3
    Fill $Graphics $Color ($X + 9) ($Y + 9) 5 3
}

function Draw-SeedGhost {
    param($Graphics, [int]$X, [int]$Y)
    Fill $Graphics $colors.Green ($X + 6) ($Y + 5) 3 3
    Fill $Graphics $colors.Brass ($X + 10) ($Y + 9) 3 3
}

function New-GuiTexture {
    param([string]$Name, $AccentDark, $Accent, [scriptblock]$DrawWorkspace)

    $bitmap = New-Object System.Drawing.Bitmap 176, 166, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
    Draw-Frame $graphics $AccentDark $Accent
    & $DrawWorkspace $graphics
    $bitmap.Save((Join-Path $textureDirectory "$Name.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $bitmap.Dispose()
}

New-GuiTexture 'infusion_wand_control' $colors.GreenDark $colors.Green {
    param($graphics)
    Draw-Slot $graphics 43 38
    Draw-HerbGhost $graphics 43 38 $colors.Green
    Draw-Arrow $graphics $colors.Green
    Fill $graphics $colors.GreenDark 116 27 44 38
    Fill $graphics $colors.Outline 119 30 38 32
    Fill $graphics $colors.Green 137 34 3 24
    Fill $graphics $colors.Brass 133 51 11 3
}

New-GuiTexture 'alchemy_flask_control' $colors.CyanDark $colors.Cyan {
    param($graphics)
    Draw-Slot $graphics 43 25
    Draw-HerbGhost $graphics 43 25 $colors.Green
    Draw-Slot $graphics 43 51
    Fill $graphics $colors.CyanDark 48 56 8 7
    Fill $graphics $colors.Cyan 50 54 4 2
    Draw-Arrow $graphics $colors.Cyan
    Fill $graphics $colors.CyanDark 116 27 44 38
    Fill $graphics $colors.Outline 119 30 38 32
    Fill $graphics $colors.Cyan 130 47 16 11
    Fill $graphics $colors.Cyan 134 36 8 12
    Fill $graphics $colors.Brass 132 34 12 3
}

New-GuiTexture 'seed_mixer' $colors.GreenDark $colors.Green {
    param($graphics)
    Draw-Slot $graphics 34 25
    Draw-SeedGhost $graphics 34 25
    Draw-Slot $graphics 52 25
    Draw-SeedGhost $graphics 52 25
    Draw-Slot $graphics 34 51
    Fill $graphics $colors.Dirt 39 57 8 7
    Fill $graphics $colors.WoodLight 40 56 6 2
    Draw-Slot $graphics 52 51
    Fill $graphics $colors.Bone 58 56 6 8
    Fill $graphics $colors.Bone 56 59 10 3
    Draw-Arrow $graphics $colors.Green
    Fill $graphics $colors.GreenDark 116 27 44 38
    Fill $graphics $colors.Outline 119 30 38 32
    Fill $graphics $colors.Green 135 42 7 14
    Fill $graphics $colors.Green 129 48 7 6
    Fill $graphics $colors.Green 142 46 7 6
    Fill $graphics $colors.Brass 132 56 14 3
}
