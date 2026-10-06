param(
    [Parameter(Mandatory=$true)]
    [string]$PackageZip
)

$ErrorActionPreference = 'Stop'
$root = Join-Path $env:APPDATA 'Akachi Launcher'
$launcher = Join-Path $root 'Launcher'
$minecraft = Join-Path $root 'Minecraft'

New-Item -ItemType Directory -Force -Path $launcher, $minecraft | Out-Null
Expand-Archive -LiteralPath $PackageZip -DestinationPath $launcher -Force

$startMenu = Join-Path $env:APPDATA 'Microsoft\Windows\Start Menu\Programs'
New-Item -ItemType Directory -Force -Path $startMenu | Out-Null
$shell = New-Object -ComObject WScript.Shell
$target = Join-Path $launcher 'AkachiLauncher.exe'

$startShortcut = $shell.CreateShortcut((Join-Path $startMenu 'Akachi Launcher.lnk'))
$startShortcut.TargetPath = $target
$startShortcut.WorkingDirectory = $launcher
$startShortcut.IconLocation = "$target,0"
$startShortcut.Description = 'Akachi Minecraft Launcher'
$startShortcut.Save()

$desktop = [Environment]::GetFolderPath([Environment+SpecialFolder]::Desktop)
if ($desktop) {
    $desktopShortcut = $shell.CreateShortcut((Join-Path $desktop 'Akachi Launcher.lnk'))
    $desktopShortcut.TargetPath = $target
    $desktopShortcut.WorkingDirectory = $launcher
    $desktopShortcut.IconLocation = "$target,0"
    $desktopShortcut.Description = 'Akachi Minecraft Launcher'
    $desktopShortcut.Save()
}

Start-Process -FilePath $target -WorkingDirectory $launcher