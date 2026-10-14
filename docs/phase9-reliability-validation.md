# Phase 9 — Reliability Validation: Provisioning, Idempotency, Health & Rollback

## 1. Provisioned node and architecture

| Item | Value |
|---|---|
| Host | Windows 11 + Docker Desktop (WSL2 engine), server v29.8.2 |
| Target | Local staging node, `localhost`, `ansible_connection=local` |
| Backend | `rec-backend:v1.0.0` (Spring Boot 3.3.4, port `8080`) |
| Frontend | `rec-frontend:v1.0.0` (Nginx, port `5173 -> 80`) |
| Database | `postgres:16-alpine`, healthy, host port `5433 -> 5432` |
| Runner pattern | Ephemeral `ubuntu:22.04` container, repo mounted at `/workspace`, host Docker socket mounted at `/var/run/docker.sock` |

Playbooks:

- `ansible/rec-platform-deploy.yml` — baseline provisioning (packages, `recadmin` user/group, `/opt/rec-platform` tree) + image/container verification + HTTP/TCP health checks.
- `ansible/rec-platform-rollback.yml` — simulated failure (`docker stop rec-backend`, asserted stopped) + recovery (`docker start`, prune of exited cruft) + post-recovery health checks.
- Runner: `scripts/run-phase9-validation.ps1` (Step 1 compose deploy, Steps 2–4 Ansible in one ephemeral container, Step 3 host-side health checks with timings).

Raw per-step logs: `docs/phase9/pass1-deploy.log`, `docs/phase9/pass2-idempotency.log`, `docs/phase9/rollback.log`, `docs/phase9/runner-container.log`, `docs/phase9/health-checks.log`.

## 2. Pass 1 — Provisioning and deployment

```text
PLAY RECAP *********************************************************************
localhost                  : ok=19   changed=5    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

## 3. Pass 2 — Idempotency verification (same container, consecutive run)

```text
PLAY RECAP *********************************************************************
localhost                  : ok=19   changed=0    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

`changed=0` with `failed=0` proves the automation is fully idempotent.

## 4. Health-check results (2026-10-14T13:43:44+05:30, host-side)

```text
Backend GET /api/auth/roles -> 200 in 26 ms; body: ["ADMIN","GENERATOR","BUYER"]
Frontend GET / -> 200 in 22 ms
Database TCP 127.0.0.1:5433 -> open in 9 ms

Running containers:
rec-backend|Up 16 seconds|0.0.0.0:8080->8080/tcp, [::]:8080->8080/tcp
rec-frontend|Up 57 minutes|0.0.0.0:5173->80/tcp, [::]:5173->80/tcp
rec-postgres-db|Up About an hour (healthy)|0.0.0.0:5433->5432/tcp, [::]:5433->5432/tcp
```

Note `rec-backend|Up 16 seconds`: the backend had just been recovered by the rollback playbook, confirming recovery rather than mere uptime.

## 5. Rollback execution (simulated failure -> recovery)

Failure simulation asserted:

```text
"backend_running.stdout": "false"
...
"msg": "All assertions passed"
```

Post-recovery verification inside the playbook:

```text
"msg": "Rollback complete. backend=200 frontend=200 db=host.docker.internal:5433 open."
```

```text
PLAY RECAP *********************************************************************
localhost                  : ok=16   changed=3    unreachable=0    failed=0    skipped=0    rescued=0    ignored=0
```

Rollback design note: recovery uses plain `docker stop/start` (not compose) because the ephemeral Ubuntu runner ships only the `docker.io` client with no `compose` subcommand. Full orchestrated rebuilds stay on the Docker host (`docker compose up` in Step 1).

## 6. Rerun

```powershell
powershell -ExecutionPolicy Bypass -File C:\Users\Yash\Desktop\rec-platform\scripts\run-phase9-validation.ps1 -SkipBuild
```

Omit `-SkipBuild` to force image rebuilds via `docker compose up -d --build`.
