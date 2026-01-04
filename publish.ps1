# Quick Publish Script for Windows PowerShell
$ORG_NAME = "AethorStudios"
$REPO_NAME = "AethorQuests"

function Write-Success { param($msg) Write-Host $msg -ForegroundColor Green }
function Write-Info { param($msg) Write-Host $msg -ForegroundColor Cyan }
function Write-Warning { param($msg) Write-Host $msg -ForegroundColor Yellow }

Write-Info "AethorQuests - GitHub Publish Script"
Write-Host ""

if (-not (Test-Path ".git")) {
    Write-Info "Initializing Git..."
    git init
    Write-Success "[OK] Git initialized"
} else {
    Write-Warning "Git already initialized"
}

$status = git status --porcelain
if ($status) {
    Write-Info "Committing changes..."
    git add .
    git commit -m "Initial commit: AethorQuests"
    Write-Success "[OK] Committed"
}

$remotes = git remote
if ($remotes -notcontains "origin") {
    Write-Info "Adding remote..."
    git remote add origin "https://github.com/$ORG_NAME/$REPO_NAME.git"
    Write-Success "[OK] Remote added"
}

Write-Info "Setting main branch..."
git branch -M main

Write-Host ""
$confirm = Read-Host "Push to GitHub now? (y/n)"
if ($confirm -eq "y") {
    git push -u origin main
    if ($LASTEXITCODE -eq 0) {
        Write-Success "[OK] Pushed to GitHub!"
        Write-Host "Repo: https://github.com/$ORG_NAME/$REPO_NAME"
    }
}
