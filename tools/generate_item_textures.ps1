Add-Type -AssemblyName System.Drawing

$textureDirectory = Join-Path $PSScriptRoot '..\src\main\resources\assets\highlife\textures\item'
$textureDirectory = [System.IO.Path]::GetFullPath($textureDirectory)

$palette = @{
    Outline = [System.Drawing.Color]::FromArgb(255, 24, 19, 18)
    DeepBrown = [System.Drawing.Color]::FromArgb(255, 67, 39, 24)
    Brown = [System.Drawing.Color]::FromArgb(255, 116, 70, 35)
    LightBrown = [System.Drawing.Color]::FromArgb(255, 174, 113, 52)
    Parchment = [System.Drawing.Color]::FromArgb(255, 220, 188, 119)
    ParchmentLight = [System.Drawing.Color]::FromArgb(255, 246, 222, 162)
    DeepGreen = [System.Drawing.Color]::FromArgb(255, 29, 73, 32)
    Green = [System.Drawing.Color]::FromArgb(255, 65, 132, 55)
    LightGreen = [System.Drawing.Color]::FromArgb(255, 127, 181, 72)
    DeepTeal = [System.Drawing.Color]::FromArgb(255, 18, 91, 91)
    Teal = [System.Drawing.Color]::FromArgb(255, 38, 164, 158)
    Cyan = [System.Drawing.Color]::FromArgb(255, 94, 220, 211)
    Highlight = [System.Drawing.Color]::FromArgb(255, 209, 255, 244)
    BrassDark = [System.Drawing.Color]::FromArgb(255, 137, 91, 27)
    Brass = [System.Drawing.Color]::FromArgb(255, 210, 153, 51)
    BrassLight = [System.Drawing.Color]::FromArgb(255, 247, 205, 91)
}

function New-Sprite {
    param(
        [string]$Name,
        [scriptblock]$Draw
    )

    $bitmap = New-Object System.Drawing.Bitmap 16, 16, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.Clear([System.Drawing.Color]::Transparent)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::None
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half

    & $Draw $graphics

    $outputPath = Join-Path $textureDirectory "$Name.png"
    $bitmap.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $graphics.Dispose()
    $bitmap.Dispose()
}

function New-Brush([System.Drawing.Color]$Color) {
    return New-Object System.Drawing.SolidBrush $Color
}

function New-Pen([System.Drawing.Color]$Color, [float]$Width = 1) {
    $pen = New-Object System.Drawing.Pen $Color, $Width
    $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Square
    $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Square
    return $pen
}

function Fill-Polygon {
    param(
        [System.Drawing.Graphics]$Graphics,
        [System.Drawing.Color]$Color,
        [int[][]]$Coordinates
    )

    $points = foreach ($coordinate in $Coordinates) {
        New-Object System.Drawing.Point $coordinate[0], $coordinate[1]
    }
    $brush = New-Brush $Color
    $Graphics.FillPolygon($brush, [System.Drawing.Point[]]$points)
    $brush.Dispose()
}

New-Sprite 'mystic_herb_seeds' {
    param($graphics)
    Fill-Polygon $graphics $palette.Outline @(@(3, 6), @(5, 4), @(10, 4), @(12, 6), @(13, 13), @(11, 15), @(4, 15), @(2, 13))
    Fill-Polygon $graphics $palette.Brown @(@(4, 7), @(5, 5), @(10, 5), @(11, 7), @(12, 13), @(10, 14), @(5, 14), @(3, 13))

    $darkPen = New-Pen $palette.DeepBrown
    $graphics.DrawLine($darkPen, 4, 7, 11, 7)
    $graphics.DrawLine($darkPen, 5, 6, 10, 6)
    $darkPen.Dispose()

    $stemPen = New-Pen $palette.DeepGreen
    $graphics.DrawLine($stemPen, 8, 5, 8, 1)
    $stemPen.Dispose()
    Fill-Polygon $graphics $palette.Green @(@(8, 3), @(5, 1), @(5, 3), @(7, 5))
    Fill-Polygon $graphics $palette.LightGreen @(@(8, 3), @(11, 1), @(11, 3), @(9, 5))

    $tealBrush = New-Brush $palette.Teal
    $graphics.FillRectangle($tealBrush, 7, 10, 2, 2)
    $tealBrush.Dispose()
    $highlightBrush = New-Brush $palette.Cyan
    $graphics.FillRectangle($highlightBrush, 7, 10, 1, 1)
    $highlightBrush.Dispose()
}

New-Sprite 'mystic_herb_bundle' {
    param($graphics)
    $outlinePen = New-Pen $palette.Outline 2
    $graphics.DrawLine($outlinePen, 8, 14, 8, 5)
    $outlinePen.Dispose()

    Fill-Polygon $graphics $palette.Outline @(@(8, 8), @(2, 6), @(1, 3), @(4, 2), @(8, 5))
    Fill-Polygon $graphics $palette.Green @(@(7, 7), @(3, 6), @(2, 3), @(4, 3), @(7, 5))
    Fill-Polygon $graphics $palette.LightGreen @(@(7, 6), @(4, 5), @(3, 3), @(4, 3))

    Fill-Polygon $graphics $palette.Outline @(@(8, 8), @(14, 6), @(15, 3), @(12, 2), @(8, 5))
    Fill-Polygon $graphics $palette.Green @(@(9, 7), @(13, 6), @(14, 3), @(12, 3), @(9, 5))
    Fill-Polygon $graphics $palette.LightGreen @(@(9, 6), @(12, 5), @(13, 3), @(12, 3))

    Fill-Polygon $graphics $palette.Outline @(@(8, 7), @(5, 3), @(7, 0), @(10, 1), @(11, 4))
    Fill-Polygon $graphics $palette.Green @(@(8, 6), @(6, 3), @(7, 1), @(9, 2), @(10, 4))
    Fill-Polygon $graphics $palette.LightGreen @(@(8, 4), @(7, 2), @(8, 2), @(9, 4))

    $stemPen = New-Pen $palette.DeepGreen
    $graphics.DrawLine($stemPen, 8, 13, 8, 6)
    $graphics.DrawLine($stemPen, 8, 8, 3, 4)
    $graphics.DrawLine($stemPen, 8, 8, 13, 4)
    $stemPen.Dispose()

    $tiePen = New-Pen $palette.LightBrown 2
    $graphics.DrawLine($tiePen, 6, 11, 10, 12)
    $tiePen.Dispose()
}

New-Sprite 'dried_mystic_herb' {
    param($graphics)
    $outlinePen = New-Pen $palette.Outline 3
    $graphics.DrawLine($outlinePen, 4, 14, 10, 2)
    $outlinePen.Dispose()
    $branchPen = New-Pen $palette.LightBrown
    $graphics.DrawLine($branchPen, 4, 14, 10, 2)
    $graphics.DrawLine($branchPen, 7, 8, 3, 5)
    $graphics.DrawLine($branchPen, 8, 6, 13, 4)
    $graphics.DrawLine($branchPen, 6, 10, 11, 11)
    $branchPen.Dispose()

    Fill-Polygon $graphics $palette.DeepBrown @(@(3, 6), @(0, 4), @(1, 7), @(5, 9))
    Fill-Polygon $graphics $palette.LightBrown @(@(3, 6), @(1, 5), @(2, 7), @(5, 8))
    Fill-Polygon $graphics $palette.DeepBrown @(@(11, 5), @(15, 3), @(14, 7), @(9, 8))
    Fill-Polygon $graphics $palette.LightBrown @(@(11, 5), @(14, 4), @(13, 6), @(10, 7))
    Fill-Polygon $graphics $palette.DeepBrown @(@(10, 10), @(14, 10), @(13, 13), @(8, 12))
    Fill-Polygon $graphics $palette.LightBrown @(@(10, 10), @(13, 11), @(12, 12), @(9, 11))
    Fill-Polygon $graphics $palette.DeepBrown @(@(9, 3), @(11, 0), @(13, 2), @(10, 5))
    Fill-Polygon $graphics $palette.Brass @(@(10, 3), @(11, 1), @(12, 2), @(10, 4))
}

New-Sprite 'herb_roll' {
    param($graphics)
    $outlineBrush = New-Brush $palette.Outline
    $graphics.FillRectangle($outlineBrush, 2, 2, 11, 11)
    $graphics.FillEllipse($outlineBrush, 10, 1, 5, 5)
    $graphics.FillEllipse($outlineBrush, 1, 10, 5, 5)
    $outlineBrush.Dispose()

    $paperBrush = New-Brush $palette.Parchment
    $graphics.FillRectangle($paperBrush, 3, 3, 9, 9)
    $graphics.FillEllipse($paperBrush, 10, 2, 4, 3)
    $graphics.FillEllipse($paperBrush, 2, 11, 3, 3)
    $paperBrush.Dispose()
    $lightBrush = New-Brush $palette.ParchmentLight
    $graphics.FillRectangle($lightBrush, 4, 3, 6, 1)
    $graphics.FillRectangle($lightBrush, 3, 4, 1, 5)
    $lightBrush.Dispose()

    $stemPen = New-Pen $palette.DeepTeal
    $graphics.DrawLine($stemPen, 5, 9, 9, 5)
    $graphics.DrawLine($stemPen, 7, 7, 5, 6)
    $graphics.DrawLine($stemPen, 7, 7, 9, 8)
    $stemPen.Dispose()

    $sealBrush = New-Brush $palette.Teal
    $graphics.FillEllipse($sealBrush, 8, 8, 5, 5)
    $sealBrush.Dispose()
    $shineBrush = New-Brush $palette.Cyan
    $graphics.FillRectangle($shineBrush, 9, 9, 1, 1)
    $shineBrush.Dispose()
}

New-Sprite 'herb_cookie' {
    param($graphics)
    $outlineBrush = New-Brush $palette.Outline
    $graphics.FillEllipse($outlineBrush, 1, 1, 14, 14)
    $outlineBrush.Dispose()
    $cookieBrush = New-Brush $palette.Brown
    $graphics.FillEllipse($cookieBrush, 2, 2, 12, 12)
    $cookieBrush.Dispose()
    $lightBrush = New-Brush $palette.LightBrown
    $graphics.FillEllipse($lightBrush, 3, 3, 10, 9)
    $lightBrush.Dispose()

    $chipBrush = New-Brush $palette.DeepGreen
    foreach ($point in @(@(5, 5), @(10, 4), @(11, 9), @(5, 11), @(4, 8))) {
        $graphics.FillRectangle($chipBrush, $point[0], $point[1], 2, 2)
    }
    $chipBrush.Dispose()
    $runePen = New-Pen $palette.Green
    $graphics.DrawArc($runePen, 6, 6, 4, 4, 60, 250)
    $runePen.Dispose()
}

New-Sprite 'infusion_wand' {
    param($graphics)
    $outlinePen = New-Pen $palette.Outline 4
    $graphics.DrawLine($outlinePen, 3, 13, 11, 4)
    $outlinePen.Dispose()
    $woodPen = New-Pen $palette.Brown 2
    $graphics.DrawLine($woodPen, 3, 13, 11, 4)
    $woodPen.Dispose()

    Fill-Polygon $graphics $palette.Outline @(@(10, 5), @(10, 1), @(13, 0), @(15, 2), @(13, 5))
    Fill-Polygon $graphics $palette.Cyan @(@(11, 4), @(11, 2), @(13, 1), @(14, 2), @(13, 4))
    $shineBrush = New-Brush $palette.Highlight
    $graphics.FillRectangle($shineBrush, 12, 1, 1, 1)
    $shineBrush.Dispose()

    $brassPen = New-Pen $palette.Brass 2
    $graphics.DrawLine($brassPen, 9, 6, 12, 4)
    $graphics.DrawLine($brassPen, 2, 14, 4, 12)
    $brassPen.Dispose()
    Fill-Polygon $graphics $palette.Green @(@(7, 8), @(5, 7), @(5, 10), @(7, 10))
    Fill-Polygon $graphics $palette.LightGreen @(@(8, 8), @(10, 7), @(9, 10), @(7, 10))
}

New-Sprite 'alchemy_flask' {
    param($graphics)
    $outlineBrush = New-Brush $palette.Outline
    $graphics.FillRectangle($outlineBrush, 6, 1, 5, 5)
    $graphics.FillEllipse($outlineBrush, 2, 4, 13, 12)
    $outlineBrush.Dispose()

    $glassBrush = New-Brush ([System.Drawing.Color]::FromArgb(255, 123, 197, 199))
    $graphics.FillRectangle($glassBrush, 7, 2, 3, 4)
    $graphics.FillEllipse($glassBrush, 3, 5, 11, 10)
    $glassBrush.Dispose()

    $liquidBrush = New-Brush $palette.Teal
    $graphics.FillEllipse($liquidBrush, 4, 8, 9, 6)
    $graphics.FillRectangle($liquidBrush, 4, 10, 9, 2)
    $liquidBrush.Dispose()
    $cyanBrush = New-Brush $palette.Cyan
    $graphics.FillRectangle($cyanBrush, 5, 9, 2, 1)
    $graphics.FillRectangle($cyanBrush, 5, 11, 1, 1)
    $cyanBrush.Dispose()
    $highlightBrush = New-Brush $palette.Highlight
    $graphics.FillRectangle($highlightBrush, 5, 6, 1, 2)
    $highlightBrush.Dispose()

    $brassPen = New-Pen $palette.Brass 2
    $graphics.DrawLine($brassPen, 4, 9, 13, 9)
    $graphics.DrawLine($brassPen, 4, 14, 12, 14)
    $graphics.DrawLine($brassPen, 6, 4, 11, 4)
    $brassPen.Dispose()
    $corkBrush = New-Brush $palette.LightBrown
    $graphics.FillRectangle($corkBrush, 7, 1, 3, 2)
    $corkBrush.Dispose()
}
