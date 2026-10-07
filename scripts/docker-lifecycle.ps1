<#
.SYNOPSIS
  Docker image build/tag/run/lifecycle script for the REC platform.
.DESCRIPTION
  Builds backend and frontend images, tags them with v1.0.0 and latest,
  starts containers via docker compose, runs health/status checks,
  exercises stop/restart/remove lifecycle, and prints command logs.
#>
param(
    [switch]$SkipBuild
)

$ErrorActionPreference = 'Continue'
$Root = Split-Path -Parent $PSScriptRoot
Set-Location $Root
$LogDir = Join-Path $Root 'docs\logs'
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
$LogFile = Join-Path $LogDir ("docker-lifecycle-{0:yyyyMMdd-HHmmss}.log" -f (Get-Date))

function Log($msg) {
    $line = "[{0:yyyy-MM-dd HH:mm:ss}] $msg" -f (Get-Date)
    Write-Host $line
    Add-Content -Path $LogFile -Value $line
}

function Run($cmd) {
    Log "`$ $cmd"
    $out = Invoke-Expression $cmd 2>&1 | Out-String
    Add-Content -Path $LogFile -Value $out
    Write-Host $out
    return $out
}

Log "=== Docker Lifecycle Starting (root: $Root) ==="
Run "docker version --format 'Client {{.Client.Version}} / Server {{.Server.Version}}'"
Run "docker compose version"

if (-not $SkipBuild) {
    Log '--- Build images ---'
    Run 'docker compose build'
    Log '--- Tag images ---'
    Run 'docker tag rec-backend:v1.0.0 rec-backend:latest'
    Run 'docker tag rec-frontend:v1.0.0 rec-frontend:latest'
    Run 'docker images | findstr /I "rec-backend rec-frontend"'
}

Log '--- Start containers ---'
Run 'docker compose up -d'
Start-Sleep -Seconds 20
Run 'docker compose ps'
Run 'docker inspect --format "{{.Name}} health={{if .State.Health}}{{.State.Health.Status}}{{else}}n/a{{end}}" rec-postgres-db rec-backend rec-frontend'

Log '--- Lifecycle: stop ---'
Run 'docker compose stop'
Run 'docker compose ps -a'

Log '--- Lifecycle: restart ---'
Run 'docker compose restart'
Start-Sleep -Seconds 15
Run 'docker compose ps'

Log '--- Lifecycle: remove (keep volume) ---'
Run 'docker compose down'
Run 'docker compose ps -a'
Run 'docker volume ls | findstr postgres_data'

Log '--- Final start + logs tail ---'
Run 'docker compose up -d'
Start-Sleep -Seconds 20
Run 'docker compose ps'
Run 'docker logs --tail 10 rec-backend'
Run 'docker logs --tail 5 rec-frontend'

Log "=== Docker Lifecycle Complete. Log: $LogFile ==="
