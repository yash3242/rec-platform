param([string]$RollbackTag = "v1.0.0")
Write-Host "=== Rolling back stack to release tag: $RollbackTag ===" -ForegroundColor Yellow
docker compose down
# Re-tag previous release image to latest
docker tag rec-backend:$RollbackTag rec-backend:latest
docker tag rec-frontend:$RollbackTag rec-frontend:latest
docker compose up -d
Write-Host "=== Rollback complete. Containers online: ===" -ForegroundColor Green
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
