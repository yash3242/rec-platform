# Automated Renewable Energy Certificate Platform

A full-stack MVP for creating, tracking, reviewing, approving, issuing, and retiring Renewable Energy Certificates with role-based workflow and persistent storage.

## Tech Stack

- Backend: Java 17, Spring Boot 3, Spring Security JWT, Spring Data JPA, Flyway, PostgreSQL
- Frontend: React, TypeScript, Vite, React Router, Axios
- Local DB: Docker Compose / PostgreSQL
- Build: Maven, npm

## Architecture

```text
React frontend
  -> REST API
    -> Spring Boot controllers
      -> services / workflow rules
        -> repositories
          -> PostgreSQL
```

The backend is layered: controllers handle HTTP, services contain business/workflow logic, repositories access the database, and DTOs separate API contracts from JPA entities.

## Roles and Permissions

| Role | Permissions |
|---|---|
| `ADMIN` | Full oversight, user status management, all RECs, all transitions |
| `PRODUCER` | Create/edit own CREATED or REJECTED RECs, submit, view own records |
| `REVIEWER` | Start review, approve/reject under-review RECs |
| `MANAGER` | Issue approved RECs, retire issued RECs |

## REC Lifecycle

```text
CREATED -> SUBMITTED -> UNDER_REVIEW -> APPROVED -> ISSUED -> RETIRED
                           \-> REJECTED -> CREATED
```

Transitions are validated centrally in `RecWorkflowService`. All changes are stored in `rec_status_history`.

> Main note: status history remains the audit trail of record for REC lifecycle changes.

## Main Entities

- `roles`
- `users`
- `recs`
- `rec_status_history`

## Database

Flyway migrations create the schema and seed roles. For persistence, use PostgreSQL.

```bash
cd rec-platform
docker compose up -d postgres
```

If Docker is unavailable, create a PostgreSQL database named `rec_platform` and a user `rec_user`.

## Environment Variables

Copy `.env.example` to `.env` and replace placeholders.

Important variables:

- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`, `JWT_EXPIRATION_MS`
- `APP_SEED_*_EMAIL`, `APP_SEED_*_PASSWORD`

Do not commit `.env` or real secrets.

## Run Backend

```bash
cd rec-platform/backend
# Use Maven from your installation or downloaded tools path
mvn spring-boot:run
```

Default API port: `8080`

## Run Frontend

```bash
cd rec-platform/frontend
npm install
npm run dev
```

Default frontend URL: `http://localhost:5173`

## API Overview

Base URL: `/api/v1`

### Auth

- `POST /auth/register`
- `POST /auth/login`
- `GET /auth/me`

### Users

- `GET /users` — ADMIN only
- `PATCH /users/{id}/status` — ADMIN only

### RECs

- `POST /recs`
- `GET /recs` with filters:
  `recCode`, `producerId`, `energySource`, `status`, `startFrom`, `endTo`, `minCertQty`, `maxCertQty`, `page`, `size`, `sort`
- `GET /recs/{id}`
- `PUT /recs/{id}`
- `PATCH /recs/{id}/status`
- `GET /recs/{id}/history`

### Dashboard

- `GET /dashboard/summary`

## Demo Seed Users

If seed environment variables are provided, these users are created on startup:

- Admin
- Producer
- Reviewer
- Manager

Use the passwords configured in your local `.env`.

## Security Notes

- Passwords are hashed with BCrypt.
- JWTs are signed with `JWT_SECRET`.
- API endpoints are protected except login/register.
- Producers can only access their own records.
- Status changes are restricted by role and validated by workflow.
- Secrets are read from environment variables, not hard-coded.

## Future Improvements

- Admin UI for user/role management
- More detailed REC validation
- Server-side pagination in all views
- Audit export
- Refresh tokens and logout revocation
- File upload for generation evidence
