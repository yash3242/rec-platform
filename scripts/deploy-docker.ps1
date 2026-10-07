Write-Host "=== Deploying REC Platform Docker Stack ===" -ForegroundColor Cyan
docker compose down
docker compose up -d --build
Write-Host "=== Deployment Triggered. Status: ===" -ForegroundColor Green
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
