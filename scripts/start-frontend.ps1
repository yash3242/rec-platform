$ErrorActionPreference = 'Stop'
$env:Path = "C:\Program Files\nodejs;$env:Path"
Set-Location "$PSScriptRoot\..\frontend"
npm.cmd run dev
