param(
    [switch]$SkipBuild
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$evidenceDir = Join-Path $repoRoot 'docs\phase9'
New-Item -ItemType Directory -Force -Path $evidenceDir | Out-Null

$dockerImage = 'ubuntu:22.04'

function Invoke-Step {
    param([string]$Name, [scriptblock]$Body)
    Write-Host ""
    Write-Host "===== $Name ====="
    & $Body
    if ($LASTEXITCODE -ne 0 -and $ErrorActionPreference -eq 'Stop') {
        throw "Step failed: $Name (exit $LASTEXITCODE)"
    }
}

# Step 0: preflight
Invoke-Step 'Preflight: Docker daemon and compose file' {
    docker info --format '{{.ServerVersion}}'
    if (!(Test-Path (Join-Path $repoRoot 'docker-compose.yml'))) { throw 'docker-compose.yml missing' }
}

# Step 1: initial provisioning and deployment on the Windows host
Invoke-Step 'Step 1: Provisioning and deployment (docker compose up)' {
    Set-Location $repoRoot
    if ($SkipBuild) {
        docker compose up -d
    } else {
        docker compose up -d --build
    }
    docker compose ps -a
}

Write-Host 'Waiting for backend health (host-side, up to 3 minutes)...'
$deadline = (Get-Date).AddMinutes(3)
$backendOk = $false
while ((Get-Date) -lt $deadline) {
    try {
        $r = Invoke-WebRequest -Uri 'http://localhost:8080/api/auth/roles' -UseBasicParsing -TimeoutSec 10
        if ($r.StatusCode -eq 200) { $backendOk = $true; break }
    } catch { Start-Sleep -Seconds 10 }
}
if (-not $backendOk) { throw 'Backend did not become healthy at http://localhost:8080/api/auth/roles' }

# Steps 2-4 run inside ONE ephemeral Ubuntu container so Pass 2 idempotency
# is measured against the same filesystem state as Pass 1.
$runnerScript = @'
set -e
export DEBIAN_FRONTEND=noninteractive
apt-get update -qq
apt-get install -y -qq --no-install-recommends python3 python3-pip python3-apt ca-certificates curl sudo docker.io >/dev/null
python3 -m pip install -q --no-cache-dir ansible
cd /workspace
exec > docs/phase9/runner-container.log 2>&1
echo "===== Step 2a: Deploy Pass 1 (provisioning) ====="
ansible-playbook -i ansible/inventory/hosts.ini ansible/rec-platform-deploy.yml 2>&1 | tee docs/phase9/pass1-deploy.log
echo "===== Step 2b: Deploy Pass 2 (idempotency) ====="
ansible-playbook -i ansible/inventory/hosts.ini ansible/rec-platform-deploy.yml 2>&1 | tee docs/phase9/pass2-idempotency.log
echo "===== Step 4: Rollback and recovery ====="
ansible-playbook -i ansible/inventory/hosts.ini ansible/rec-platform-rollback.yml 2>&1 | tee docs/phase9/rollback.log
echo "PHASE9_RUNNER_DONE"
'@

Invoke-Step 'Steps 2-4: Ansible deploy twice plus rollback (ephemeral Ubuntu container)' {
    Set-Location $repoRoot
    $containerLog = Join-Path $evidenceDir 'runner-container.log'
    # Write the container script to a file (LF endings) to avoid nested-quoting
    # issues when passing a multi-line script through docker + bash -c.
    $shPath = Join-Path $evidenceDir 'container-run.sh'
    [IO.File]::WriteAllText($shPath, ($runnerScript -replace "`r`n", "`n"))
    # NOTE: $ErrorActionPreference is relaxed for this call only: the Docker
    # CLI relays container stderr (e.g. debconf notices) which PowerShell
    # would otherwise turn into terminating errors. Real failures are still
    # caught via the container exit code below. All meaningful output lands in
    # docs/phase9/runner-container.log via the exec redirect in the script.
    $prevEap = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try {
        & docker run --rm `
            -v "$repoRoot`:/workspace" `
            -v '/var/run/docker.sock:/var/run/docker.sock' `
            -w /workspace `
            $dockerImage `
            bash /workspace/docs/phase9/container-run.sh
        $dockerExit = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $prevEap
    }
    if ($dockerExit -ne 0) { throw "Ansible container run failed (exit $dockerExit). See $containerLog" }
    Write-Host "Ansible container output saved to $containerLog"
}

# Step 3 (host-side authoritative health check with timings)
Invoke-Step 'Step 3: Application health checks (host-side)' {
    $healthLog = Join-Path $evidenceDir 'health-checks.log'
    $lines = @()
    $lines += "Timestamp: $(Get-Date -Format o)"
    $sw = New-Object System.Diagnostics.Stopwatch
    $sw.Start()
    $b = Invoke-WebRequest -Uri 'http://localhost:8080/api/auth/roles' -UseBasicParsing -TimeoutSec 30
    $sw.Stop()
    $lines += "Backend GET /api/auth/roles -> $($b.StatusCode) in $($sw.ElapsedMilliseconds) ms; body: $($b.Content.Trim())"
    $sw.Restart()
    $f = Invoke-WebRequest -Uri 'http://localhost:5173' -UseBasicParsing -TimeoutSec 30
    $sw.Stop()
    $lines += "Frontend GET / -> $($f.StatusCode) in $($sw.ElapsedMilliseconds) ms"
    $tcp = New-Object Net.Sockets.TcpClient
    $sw.Restart()
    $tcp.Connect('127.0.0.1', 5433)
    $sw.Stop()
    $lines += "Database TCP 127.0.0.1:5433 -> open in $($sw.ElapsedMilliseconds) ms"
    $tcp.Close()
    $lines += ''
    $lines += 'Running containers:'
    $lines += (docker compose ps --format '{{.Name}}|{{.Status}}|{{.Ports}}')
    $lines | Tee-Object -FilePath $healthLog
}

Write-Host ''
Write-Host 'Phase 9 validation complete. Evidence under docs/phase9/.'
exit 0
