param(
    [switch]$CheckOnly
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot

$dockerImage = 'ubuntu:22.04'
$playbookArgs = if ($CheckOnly) {
    '--check'
} else {
    ''
}

$bashSetup = @"
export DEBIAN_FRONTEND=noninteractive
apt-get update -qq
apt-get install -y --no-install-recommends python3 python3-pip python3-apt ca-certificates curl sudo
python3 -m pip install --no-cache-dir ansible
cd /workspace
ansible-playbook -i ansible/inventory/hosts.ini ansible/rec-platform-setup.yml $playbookArgs
"@

$dockerCmd = @(
    'run', '--rm',
    '-v', "$repoRoot`:/workspace",
    '-w', '/workspace',
    $dockerImage,
    'bash', '-c', $bashSetup
)

Write-Host "Running Ansible via ephemeral Ubuntu container..."
Write-Host "Repository: $repoRoot"
Write-Host "CheckOnly: $($CheckOnly.IsPresent)"

& docker @dockerCmd
$exitCode = $LASTEXITCODE

if ($exitCode -ne 0) {
    exit $exitCode
}

Write-Host "Ansible run completed."
exit 0
