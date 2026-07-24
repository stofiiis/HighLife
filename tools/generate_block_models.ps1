$modelDirectory = Join-Path $PSScriptRoot '..\src\main\resources\assets\highlife\models\block'
$modelDirectory = [System.IO.Path]::GetFullPath($modelDirectory)

function New-Faces {
    param([string]$Texture)

    return [ordered]@{
        north = [ordered]@{ texture = $Texture }
        east = [ordered]@{ texture = $Texture }
        south = [ordered]@{ texture = $Texture }
        west = [ordered]@{ texture = $Texture }
        up = [ordered]@{ texture = $Texture }
        down = [ordered]@{ texture = $Texture }
    }
}

function New-Element {
    param(
        [double[]]$From,
        [double[]]$To,
        [string]$Texture,
        [hashtable]$Rotation = $null
    )

    $element = [ordered]@{
        from = $From
        to = $To
    }
    if ($null -ne $Rotation) {
        $element.rotation = $Rotation
    }
    $element.faces = New-Faces $Texture
    return $element
}

function Get-BlockDisplay {
    return [ordered]@{
        thirdperson_righthand = [ordered]@{
            rotation = @(75, 45, 0)
            translation = @(0, 2.5, 0)
            scale = @(0.375, 0.375, 0.375)
        }
        thirdperson_lefthand = [ordered]@{
            rotation = @(75, 45, 0)
            translation = @(0, 2.5, 0)
            scale = @(0.375, 0.375, 0.375)
        }
        firstperson_righthand = [ordered]@{
            rotation = @(0, 45, 0)
            scale = @(0.4, 0.4, 0.4)
        }
        firstperson_lefthand = [ordered]@{
            rotation = @(0, 225, 0)
            scale = @(0.4, 0.4, 0.4)
        }
        gui = [ordered]@{
            rotation = @(30, 225, 0)
            scale = @(0.72, 0.72, 0.72)
        }
        ground = [ordered]@{
            translation = @(0, 3, 0)
            scale = @(0.25, 0.25, 0.25)
        }
        fixed = [ordered]@{
            scale = @(0.5, 0.5, 0.5)
        }
    }
}

function Save-Model {
    param(
        [string]$Name,
        [hashtable]$Model
    )

    $path = Join-Path $modelDirectory "$Name.json"
    $json = $Model | ConvertTo-Json -Depth 20
    [System.IO.File]::WriteAllText($path, $json + [Environment]::NewLine, (New-Object System.Text.UTF8Encoding $false))
}

$seedMixerElements = @(
    (New-Element @(1, 0, 1) @(15, 3, 15) '#base'),
    (New-Element @(2, 3, 2) @(14, 5, 14) '#base'),
    (New-Element @(5, 5, 5) @(11, 6, 11) '#inner'),
    (New-Element @(3, 5, 3) @(13, 9, 5) '#bowl'),
    (New-Element @(3, 5, 11) @(13, 9, 13) '#bowl'),
    (New-Element @(3, 5, 5) @(5, 9, 11) '#bowl'),
    (New-Element @(11, 5, 5) @(13, 9, 11) '#bowl'),
    (New-Element @(7, 8, 7) @(9, 15, 9) '#pestle' ([ordered]@{
        origin = @(8, 8, 8)
        axis = 'z'
        angle = 22.5
        rescale = $true
    })),
    (New-Element @(7, 1, 0.5) @(9, 3, 1) '#inner')
)

$seedMixerModel = [ordered]@{
    credit = 'stofiiis'
    ambientocclusion = $true
    render_type = 'cutout'
    textures = [ordered]@{
        particle = 'highlife:block/seed_mixer'
        base = 'highlife:block/seed_mixer'
        bowl = 'highlife:block/seed_mixer_bowl'
        inner = 'highlife:block/seed_mixer_inner'
        pestle = 'highlife:block/seed_mixer_pestle'
    }
    elements = $seedMixerElements
    display = Get-BlockDisplay
}
Save-Model 'seed_mixer' $seedMixerModel

$rackElements = @(
    (New-Element @(1, 0, 6) @(3, 16, 10) '#wood'),
    (New-Element @(13, 0, 6) @(15, 16, 10) '#wood'),
    (New-Element @(1, 14, 6) @(15, 16, 10) '#wood'),
    (New-Element @(1, 1, 6) @(15, 3, 10) '#wood'),
    (New-Element @(3, 5, 7) @(13, 6, 9) '#wood'),
    (New-Element @(3, 9, 7) @(13, 10, 9) '#wood')
)

$rackModel = [ordered]@{
    credit = 'stofiiis'
    ambientocclusion = $true
    render_type = 'cutout'
    textures = [ordered]@{
        particle = 'highlife:block/drying_rack'
        wood = 'highlife:block/drying_rack'
    }
    elements = $rackElements
    display = Get-BlockDisplay
}
Save-Model 'drying_rack' $rackModel

$occupiedElements = @($rackElements)
$occupiedElements += @(
    (New-Element @(3.5, 3, 7.75) @(6.5, 13, 8.25) '#herb'),
    (New-Element @(6.5, 3, 7.75) @(9.5, 13, 8.25) '#herb'),
    (New-Element @(9.5, 3, 7.75) @(12.5, 13, 8.25) '#herb')
)

$occupiedRackModel = [ordered]@{
    credit = 'stofiiis'
    ambientocclusion = $true
    render_type = 'cutout'
    textures = [ordered]@{
        particle = 'highlife:block/drying_rack'
        wood = 'highlife:block/drying_rack'
        herb = 'highlife:block/drying_rack_occupied'
    }
    elements = $occupiedElements
    display = Get-BlockDisplay
}
Save-Model 'drying_rack_occupied' $occupiedRackModel
