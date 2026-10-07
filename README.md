# Automated Renewable Energy Certificate Platform

A full-stack MVP for asset onboarding, generation logging, and Renewable Energy Certificate issuance, trading, and retirement through strict teen workflows.

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

It installs/tool-binds:

```groovy
tools { maven 'Maven3'; jdk 'JDK17' }
```

and defines parameters:

```groovy
DEPLOY_ENV   defaultValue: 'local-staging'
SERVER_PORT  defaultValue: '8080'
```

Stages:

1. Checkout
2. Build Frontend
3. Build Backend
4. Run Selenium Tests
5. Archive Artifacts
6. Deploy embedded Tomcat Spring Boot JAR

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
