# Automated Renewable Energy Certificate Platform

![build](https://img.shields.io/badge/build-passing-brightgreen) ![tests](https://img.shields.io/badge/tests-7%2F7-brightgreen) ![idempotency](https://img.shields.io/badge/idempotency-changed%3D0-blue) ![stack](https://img.shields.io/badge/stack-spring%20boot%20%7C%20react%20%7C%20postgres%20%7C%20docker-orange)

A full-stack, containerized MVP for asset onboarding, generation logging, and Renewable Energy Certificate issuance, trading, and retirement through strict role-based workflows — shipped via a Jenkins-gated CI pipeline and Ansible-managed provisioning.

## Prerequisites

| Requirement | Version / detail |
|---|---|
| Windows 11 + Docker Desktop (WSL2) | Engine 29.x, Compose v2 |
| Java / Maven (CI agents + local) | OpenJDK 17, Maven 3.9+ on PATH |
| Node.js (frontend builds) | Node 20+, npm |
| Jenkins | LTS on `http://localhost:8081`, job `rec-platform-pipeline` |
| Google Chrome | Headless Selenium runs via WebDriverManager |
| Ports free | `5173` frontend, `8080` backend, `5433` DB, `8081` Jenkins |

## Architecture summary

```text
Browser/Selenium --> React/Nginx :5173 --> Spring Boot API :8080 --> PostgreSQL :5433/:5432
                          Git push --> Jenkins (test gate -> docker build -> health check)
                                           --> Ansible (setup -> deploy x2 -> rollback drill)
```

Full diagrams and component map: `docs/architecture-and-pipeline.md`.

## Project phases and evidence

| Phase | Title | Key evidence |
|---|---|---|
| 1 | MVP features, roles, workflows | `backend/`, `frontend/`, state machines in README |
| 2–3 | Declarative Jenkinsfile + embedded Tomcat deploy | `Jenkinsfile`, `backend/Dockerfile` |
| 4–5 | Selenium E2E + CI quality gate | `backend/src/test/.../e2e/`, `docs/ci-quality-gate.md` |
| 6 | Docker containers | `docker-compose.yml`, `docs/docker-lifecycle-evidence.md` |
| 7 | Jenkins–Docker CD pipeline | `Jenkinsfile`, `scripts/deploy-docker.ps1` |
| 8 | Ansible configuration management | `ansible/`, `docs/configuration-spec.md`, `docs/ansible-execution-log.md` |
| 9 | Provisioning, idempotency, rollback | `scripts/run-phase9-validation.ps1`, `docs/phase9-reliability-validation.md`, `docs/phase9/` logs |
| 10 | Release docs + viva pack | `docs/architecture-and-pipeline.md`, `docs/troubleshooting-guide.md`, `docs/limitations-and-future-scope.md`, `docs/viva-defense-guide.md` |

## Tech Stack

- Backend: Java 17, Spring Boot 3, Spring Security JWT, Spring Data JPA, Flyway, PostgreSQL
- Frontend: React, TypeScript, Vite, React Router, Axios
- Browser automation: Selenium WebDriver, WebDriverManager, JUnit 5
- CI: Jenkins declarative pipeline, Maven build, npm build, Surefire XML, screenshot artifacts
- Local database: Docker Compose / PostgreSQL

## Architecture

```text
React/Vite frontend (port 5173)
  -> REST API
    -> Spring Boot controllers
      -> services / workflow rules
        -> JPA repositories
          -> PostgreSQL (port 5432)
```

Backend package layout:

```text
backend/src/main/java/com/platform/recs/
  config/     security, CORS, frontend/test setup
  controller/ REST endpoints
  dto/        request/response contracts
  entity/     JPA entities
  enumtype/   domain enums
  exception/  exception handling and response shape
  repository/ Spring Data JPA repositories
  security/   JWT utilities and filters
  service/    business rules, state machines, dashboard KPIs
```

Selenium E2E tests run from:

```text
backend/src/test/java/com/platform/recs/e2e/
```

## Roles

| Role | Permissions |
|---|---|
| `ADMIN` | Verify/suspend assets, verify/reject/mint generation logs, all REC transitions, user oversight |
| `GENERATOR` | Onboard assets, view own assets/logs/minted RECs, list RECs |
| `BUYER` | Browse listed REC marketplace, purchase listed RECs, retire transferred RECs |

## State Machines

### Asset

```text
PENDING_VERIFICATION -> ACTIVE -> SUSPENDED
```

### Generation Log

```text
SUBMITTED -> VERIFIED -> MINTED
     \----> REJECTED
```

### REC

```text
ISSUED -> LISTED -> TRANSFERRED -> RETIRED
```

Transitions are validated centrally in `AssetWorkflowService`, `GenerationLogWorkflowService`, and `RecWorkflowService`. Status changes are recorded in `status_history` with actor, old/new status, and comment.

## Main Entities

- `roles`
- `users`
- `assets`
- `generation_logs`
- `recs`
- `status_history`

## Database

Flyway migrations run on backend startup and create all required tables. If using Docker:

```bash
cd C:\Users\Yash\Desktop\rec-platform
docker compose up -d postgres
```

If using a local PostgreSQL, the intended database is:

```text
Host: localhost
Port: 5432
Database: rec_platform
User: postgres
```

Configure local secrets in `.env` using `.env.example` as a template. Do not commit `.env` or real production secrets.

## Environment Configuration

`rec-platform/scripts/start-backend.ps1` automatically loads `rec-platform/.env`.
Important values:

```env
DB_URL=jdbc:postgresql://localhost:5432/rec_platform
DB_USERNAME=postgres
DB_PASSWORD=...
JWT_SECRET=...
APP_SEED_ADMIN_EMAIL=admin@example.com
APP_SEED_ADMIN_PASSWORD=...
APP_SEED_GENERATOR_EMAIL=generator@example.com
APP_SEED_GENERATOR_PASSWORD=...
APP_SEED_BUYER_EMAIL=buyer@example.com
APP_SEED_BUYER_PASSWORD=...
```

## Running

Backend:

```powershell
cd C:\Users\Yash\Desktop\rec-platform\backend
mvn spring-boot:run
```

Frontend:

```powershell
cd C:\Users\Yash\Desktop\rec-platform\frontend
npm.cmd run dev
```

Backend API:

```text
http://localhost:8080/api/v1
```

Frontend:

```text
http://localhost:5173
```

## API Overview

### Auth

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`

### Assets

- `POST /api/v1/assets`
- `GET /api/v1/assets?energySource=&status=&page=&size=`
- `GET /api/v1/assets/{id}`
- `PUT /api/v1/assets/{id}`
- `PATCH /api/v1/assets/{id}/status`
- `GET /api/v1/assets/{id}/history`

### Generation Logs

- `POST /api/v1/generation-logs`
- `GET /api/v1/generation-logs?energySource=&vintageYear=&status=&page=&size=`
- `GET /api/v1/generation-logs/{id}`
- `PUT /api/v1/generation-logs/{id}`
- `PATCH /api/v1/generation-logs/{id}/status`
- `GET /api/v1/generation-logs/{id}/history`

### RECs

- `GET /api/v1/recs?energySource=&vintageYear=&status=&page=&size=`
- `GET /api/v1/recs/{id}`
- `PATCH /api/v1/recs/{id}/status`
- `POST /api/v1/recs/{id}/purchase`
- `GET /api/v1/recs/{id}/history`

### Dashboard

- `GET /api/v1/dashboard/summary`

## Test Automation

Backend unit tests:

```powershell
cd C:\Users\Yash\Desktop\rec-platform\backend
mvn test
```

The Selenium suite expects both backend and frontend to be running before `mvn test`. It includes four journeys:

1. Authentication, registration, and role-based redirect validation.
2. Generator onboarding a solar/wind asset and submitting generation logs.
3. Admin review, verification, and REC minting.
4. Buyer browsing the marketplace, purchasing an active REC, and executing certificate retirement.

Failure screenshots are saved under:

```text
backend/target/screenshots/
```

## Jenkins CI/CD

The declarative pipeline is in:

```text
rec-platform/Jenkinsfile
```

It uses system `mvn`/`java` directly (no `tools{}` block — see `docs/troubleshooting-guide.md` §1).

Stages:

1. Checkout
2. Quality Gate / Test execution (`mvn clean test` + JUnit report)
3. Docker Build & Verification (`docker compose up -d --build`)
4. Health Check verification (`/api/auth/roles` + `/` must return 200)

If tests fail, the pipeline fails before deployment.

## Git Workflow Demonstration

Script:

```text
rec-platform/scripts/git-workflow-demo.ps1
```

It demonstrates:

- creating `feature/status-workflow`
- introducing a merge conflict in a shared file
- resolving the conflict
- merging into `main`
- tagging `v1.0.0-mvp`
- printing `git log --graph --oneline --all`

## CI Quality Gate Demonstration

Document:

```text
rec-platform/docs/ci-quality-gate.md
```

It explains how to modify an assertion, observe failed build, then restore and verify green build.
