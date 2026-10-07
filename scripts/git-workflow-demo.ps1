<#
.SYNOPSIS
Automates an intentional merge conflict and resolution for the REC Platform feature workflow.

.DESCRIPTION
- Ensures current branch is main
- Creates feature branch feature/status-workflow
- Makes a change to a shared file on the feature branch
- Switches back to main, makes a conflicting change to same file
- Attempts merge and resolves conflict via ours/union strategy
- Tags the resulting commit as v1.0.0-mvp
- Prints decorated Git commit graph
#>
param(
  [string]$RepoRoot = (Join-Path $PSScriptRoot ".."),
  [string]$FeatureBranch = "feature/status-workflow",
  [string]$MainBranch = "main"
)

$ErrorActionPreference = 'Stop'
$env:Path += ';C:\Program Files\Git\cmd'
Set-Location $RepoRoot

Write-Host "Current branch:" (git rev-parse --abbrev-ref HEAD)

# Ensure main is local and up to date
git checkout $MainBranch
git pull origin $MainBranch 2>$null || Write-Host "No remote configured; using local main only."

# Create feature branch and introduce change
git checkout -b $FeatureBranch

$sharedFile = "README.md"
$featureMarker = "\n\n### Feature Workflow\nStatus workflow feature branch changes.\n"
if (!(Get-Content $sharedFile -Raw | Select-String -Quiet "Feature Workflow")) {
    Add-Content -Path $sharedFile -Value $featureMarker
}

git add $sharedFile
git commit -m "feature: introduce status workflow notes in README"

# Switch back to main and make conflicting change
git checkout $MainBranch

$mainMarker = "\n\n### Main Workflow\nMain branch updated status workflow documentation.\n"
if (!(Get-Content $sharedFile -Raw | Select-String -Quiet "Main Workflow")) {
    Add-Content -Path $sharedFile -Value $mainMarker
}

git add $sharedFile
git commit -m "docs: update workflow notes on main"

# Merge and intentionally conflict
$mergeExit = 0
git merge --no-ff $FeatureBranch -m "merge: integrate status workflow feature"
$mergeExit = $LASTEXITCODE

if ($mergeExit -ne 0) {
    Write-Host "Merge conflict detected. Resolving using ours for workflow docs."
    git checkout --ours $sharedFile
    git add $sharedFile

    # If merge still has no commit, create it
    if (Test-Path "$RepoRoot\.git\MERGE_HEAD") {
        git commit -m "merge: integrate feature/status-workflow and resolve README conflict"
    } else {
        git commit -m "resolve: merge conflict on README"
    }
}

# Tag release
git tag v1.0.0-mvp

Write-Host "\nGit commit graph:"


# Print decorated graph
git log --graph --oneline --all
