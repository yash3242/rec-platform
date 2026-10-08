param(
    [switch]$CheckOnly
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot

if ($CheckOnly) {
    wsl ansible-playbook -i "$repoRoot/ansible/inventory/hosts.ini" "$repoRoot/ansible/rec-platform-setup.yml" --check
} else {
    wsl ansible-playbook -i "$repoRoot/ansible/inventory/hosts.ini" "$repoRoot/ansible/rec-platform-setup.yml"
}
