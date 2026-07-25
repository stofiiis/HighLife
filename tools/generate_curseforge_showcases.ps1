Add-Type -AssemblyName System.Drawing

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$assets = Join-Path $root "src/main/resources/assets/highlife/textures"
$output = Join-Path $root "curseforge_assets"

function Draw-CenteredText($graphics, $text, $font, $brush, $centerX, $y) {
    $size = $graphics.MeasureString($text, $font)
    $graphics.DrawString($text, $font, $brush, $centerX - ($size.Width / 2), $y)
}

function Draw-Showcase($backgroundPath, $outputName, $title, $subtitle, $entries) {
    $background = [System.Drawing.Image]::FromFile($backgroundPath)
    $canvas = [System.Drawing.Bitmap]::new($background.Width, $background.Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($canvas)
    $graphics.DrawImage($background, 0, 0, $canvas.Width, $canvas.Height)

    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
    $headerBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(190, 5, 12, 8))
    $graphics.FillRectangle($headerBrush, 0, 0, $canvas.Width, 132)

    $titleFont = [System.Drawing.Font]::new("Bahnschrift SemiBold", 42, [System.Drawing.FontStyle]::Bold)
    $subtitleFont = [System.Drawing.Font]::new("Bahnschrift", 18, [System.Drawing.FontStyle]::Regular)
    $labelFont = [System.Drawing.Font]::new("Bahnschrift SemiBold", 19, [System.Drawing.FontStyle]::Bold)
    $titleBrush = [System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml("#F1E7C2"))
    $subtitleBrush = [System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml("#9FC890"))
    $labelBrush = [System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml("#F5F0DE"))
    $labelBack = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(205, 7, 13, 10))

    Draw-CenteredText $graphics $title $titleFont $titleBrush ($canvas.Width / 2) 18
    Draw-CenteredText $graphics $subtitle $subtitleFont $subtitleBrush ($canvas.Width / 2) 82

    $centers = @(188, 478, 768, 1058, 1348)
    for ($i = 0; $i -lt $entries.Count; $i++) {
        $entry = $entries[$i]
        $sprite = [System.Drawing.Image]::FromFile($entry.Path)
        $targetSize = if ($entry.Size) { $entry.Size } else { 170 }
        $scale = [Math]::Min($targetSize / $sprite.Width, $targetSize / $sprite.Height)
        $width = [Math]::Max(1, [int]($sprite.Width * $scale))
        $height = [Math]::Max(1, [int]($sprite.Height * $scale))
        $x = [int]($centers[$i] - ($width / 2))
        $y = [int](445 - ($height / 2))

        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
        $shadow = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(115, 0, 0, 0))
        $graphics.FillEllipse($shadow, $centers[$i] - 92, 510, 184, 34)
        $shadow.Dispose()
        $graphics.DrawImage($sprite, $x, $y, $width, $height)
        $sprite.Dispose()

        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.FillRectangle($labelBack, $centers[$i] - 130, 570, 260, 52)
        Draw-CenteredText $graphics $entry.Label $labelFont $labelBrush $centers[$i] 582
    }

    New-Item -ItemType Directory -Force -Path $output | Out-Null
    $canvas.Save((Join-Path $output $outputName), [System.Drawing.Imaging.ImageFormat]::Png)

    $headerBrush.Dispose()
    $titleBrush.Dispose()
    $subtitleBrush.Dispose()
    $labelBrush.Dispose()
    $labelBack.Dispose()
    $titleFont.Dispose()
    $subtitleFont.Dispose()
    $labelFont.Dispose()
    $graphics.Dispose()
    $canvas.Dispose()
    $background.Dispose()
}

$growEntries = @(
    @{ Label = "Mystic Herb Seeds"; Path = Join-Path $assets "item/mystic_herb_seeds.png"; Size = 175 },
    @{ Label = "Fresh Herb Bundle"; Path = Join-Path $assets "item/mystic_herb_bundle.png"; Size = 175 },
    @{ Label = "Dried Herb"; Path = Join-Path $assets "item/dried_mystic_herb.png"; Size = 175 },
    @{ Label = "Drying Rack"; Path = Join-Path $assets "item/drying_rack.png"; Size = 190 },
    @{ Label = "Seed Mixer"; Path = Join-Path $assets "block/seed_mixer.png"; Size = 190 }
)

$toolEntries = @(
    @{ Label = "Rolling Paper"; Path = Join-Path $assets "item/rolling_paper.png"; Size = 175 },
    @{ Label = "Herb Roll"; Path = Join-Path $assets "item/herb_roll.png"; Size = 175 },
    @{ Label = "Infusion Wand"; Path = Join-Path $assets "item/infusion_wand.png"; Size = 175 },
    @{ Label = "Alchemy Flask"; Path = Join-Path $assets "item/alchemy_flask.png"; Size = 175 },
    @{ Label = "Herb Cookie"; Path = Join-Path $assets "item/herb_cookie.png"; Size = 175 }
)

Draw-Showcase (Join-Path $root "art_sources/curseforge_grow_process_background.png") "01-grow-and-process.png" "GROW & PROCESS" "From rare seeds to refined herbal ingredients" $growEntries
Draw-Showcase (Join-Path $root "art_sources/curseforge_infusion_tools_background.png") "02-infusion-toolkit.png" "INFUSION TOOLKIT" "Craft, load and master every herbal tool" $toolEntries
