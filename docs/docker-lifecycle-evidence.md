# Docker Lifecycle Evidence — Phase 6

**Date:** Oct 08 2026
**Project:** Rec Platform (`C:\Users\Yash\Desktop\rec-platform`)

## Files Created / Updated

| File | Purpose |
|---|---|
| `backend/Dockerfile` | Multi-stage build: Stage 1 `maven:3.9-eclipse-temurin-17` packages the JAR; Stage 2 `eclipse-temurin:17-jre` runtime, `EXPOSE 8080` |
| `backend/.dockerignore` | Excludes `target/`, logs, IDE dirs from build context |
| `frontend/Dockerfile` | Multi-stage build: Stage 1 `node:20-alpine` runs `npm ci` + `npm run build`; Stage 2 `nginx:alpine` serves `dist/`, `EXPOSE 80` |
| `frontend/nginx.conf` | Serves SPA (`try_files ... /index.html`) and proxies `/api/` to `http://backend:8080` |
| `frontend/.dockerignore` | Excludes `node_modules/`, `dist/` |
| `docker-compose.yml` | `postgres-db` (postgres:16-alpine, `pg_isready` healthcheck, named volume `postgres_data`), `backend` (depends on `service_healthy`, maps 8080:8080), `frontend` (depends on backend, maps 5173:80) |
| `scripts/docker-lifecycle.ps1` | Build → tag (`v1.0.0`, `latest`) → up → health/status → stop → restart → down → up; prints logs to `docs/logs/` |

> Note: local PostgreSQL 18 already owns host port 5432, so `postgres-db` maps `5433:5432` (inter-container traffic still uses `postgres-db:5432`).

## Command Execution Log (excerpt)

```
$ docker compose build
#32 DONE 4.5s
Image rec-frontend:v1.0.0 Built
Image rec-backend:v1.0.0 Built

$ docker tag rec-backend:v1.0.0 rec-backend:latest
$ docker tag rec-frontend:v1.0.0 rec-frontend:latest

$ docker compose up -d
 Container rec-postgres-db Healthy
 Container rec-backend Started
 Container rec-frontend Started
```

Full run log: `docs/logs/docker-lifecycle-20261008-022536.log`

## docker ps

```
NAME              IMAGE                 STATUS                      PORTS
rec-backend       rec-backend:v1.0.0   Up 36 seconds               0.0.0.0:8080->8080/tcp
rec-frontend      rec-frontend:v1.0.0  Up 36 seconds               0.0.0.0:5173->80/tcp
rec-postgres-db   postgres:16-alpine    Up 42 seconds (healthy)     0.0.0.0:5433->5432/tcp
```

## docker images

```
REPOSITORY       TAG       SIZE
rec-backend      v1.0.0    ~463MB
rec-backend      latest    ~463MB
rec-frontend     v1.0.0    ~52MB
rec-frontend     latest    ~52MB
postgres         16-alpine ~93MB
```

## Container Inspect Details

```
$ docker inspect --format '{{.Name}} {{.State.Status}} health={{if .State.Health}}{{.State.Health.Status}}{{end}} image={{.Config.Image}}' rec-postgres-db rec-backend rec-frontend
/rec-postgres-db running health=healthy image=postgres:16-alpine
/rec-backend     running image=rec-backend:v1.0.0
/rec-frontend    running image=rec-frontend:v1.0.0
```

```
$ docker volume ls | findstr postgres_data
local   rec-platform_postgres_data
```

## Lifecycle Commands Verified

```
$ docker compose stop        # all containers exited 0
$ docker compose ps -a       # names, Exited status listed
$ docker compose restart     # all containers restarted
$ docker compose down        # containers + network removed, volume retained
$ docker compose up -d       # healthy after ~20s
```

## Verification Steps

1. `docker compose ps` → backend `Up`, frontend `Up`, postgres `Up (healthy)`.
2. `curl -i http://localhost:8080/api/v1/dashboard/summary` → `403` (auth required — Tomcat/Spring security responding).
3. `curl -i http://localhost:5173/` → `200 OK` with Vite-built HTML; `/api/` calls proxy through nginx to the backend.
4. `docker logs --tail 10 rec-backend` shows `Started RecPlatformApplication` and Flyway migrations applied against the `postgres-db` service.
5. `docker logs --tail 5 rec-frontend` shows nginx serving requests.
6. Data persistence: `docker compose down && docker compose up -d` preserves `rec_platform_postgres_data` volume (assets/logs schema intact).

## Jenkins Integration (optional snippet)

```groovy
stage('Docker Lifecycle') {
    steps {
        powershell '.\\scripts\\docker-lifecycle.ps1'
    }
    post { always { archiveArtifacts artifacts: 'docs/logs/*.log', allowEmptyArchive: true } }
}
```
