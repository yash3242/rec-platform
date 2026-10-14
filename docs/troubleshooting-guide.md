# REC Platform — Troubleshooting & Disaster Recovery Guide

All entries below are failure modes actually encountered on this project
(Windows 11 + Docker Desktop WSL2), with the resolution that was applied.

## 1. Jenkins: `Tool type 'maven' does not have an install of 'Maven3'`

- **Symptom:** Builds #3–#5 failed instantly: no `Maven3`/`JDK17` installs configured.
- **Cause:** `Jenkinsfile` declared `tools { maven 'Maven3'; jdk 'JDK17' }` but Jenkins Global Tool Configuration had no such aliases.
- **Fix:** Removed the `tools{}` block entirely (`Jenkinsfile:1`). Agents use system `mvn`/`java` from PATH via `bat 'mvn clean test'`.
- **Verify:** `git log --oneline` shows `10233eb fix(pipeline): remove unconfigured tools block`.

## 2. Jenkins: `No such DSL method '﻿pipeline' found` (BOM crash)

- **Symptom:** Pipeline failed to parse with an invisible character before `pipeline`.
- **Cause:** PowerShell `Set-Content -Encoding utf8` wrote a UTF-8 BOM (`EF BB BF`) at byte 0.
- **Fix:** Rewrote `Jenkinsfile` as BOM-free ASCII/UTF-8 without BOM; verified with `Format-Hex` (first bytes `70 69 70 65 6C 69 6E 65` = `pipeline`).
- **Verify:** commit `e52e82e fix(pipeline): remove utf-8 bom character from jenkinsfile`.

## 3. Selenium E2E: `ERR_CONNECTION_REFUSED` on `:5173`

- **Symptom:** Journey 2 failed with `net::ERR_CONNECTION_REFUSED ... get {url=http://localhost:5173/assets}`; Journeys 3–4 timed out downstream.
- **Causes found (in order):**
  1. Dev/frontend server not running when `mvn test` executed from Jenkins.
  2. Nginx SPA conflict: `try_files $uri $uri/ /index.html` collided with the static `/assets/` folder, so `/assets` returned 301/file handling instead of the SPA.
  3. Headless Chrome proxy interference on `localhost` in some runs.
- **Fixes:** `frontend/nginx.conf` serves `/assets/` statically and falls back to `/index.html`; tests use explicit waits (`presenceOfElementLocated` before selecting options); base URL overridable via `FRONTEND_BASE_URL` (default `http://127.0.0.1:5173`); suite runs only against a live stack.
- **Verify:** `mvn test` → `Tests run: 7, Failures: 0, Errors: 0`.

## 4. Ephemeral Ubuntu Ansible runner on Windows (no WSL bash, Alpine `cytopia/ansible` lacks apt)

- **Symptom:** `wsl ansible-playbook` failed (`/bin/sh: bash: not found` — only Docker Desktop's minimal distro present); generic Ansible container failed with `python3-apt must be installed` (Alpine has no apt).
- **Fix:** `scripts/run-ansible-setup.ps1` launches an ephemeral `ubuntu:22.04` container mounting `${PWD}:/workspace`, installing `python3 python3-pip python3-apt ansible` before running the playbook; supports `-CheckOnly` (`--check`); `--rm` guarantees cleanup; exit code propagated.
- **Hardening:** playbook uses distro-portable packages (`postgresql-client`, `docker.io`, `docker-compose-v2`), `cache_valid_time: 3600`, and systemd/UFW tasks guarded by PID-1 detection with `ignore_errors: true` inside test containers.

## 5. Ansible check-mode crash: `file (/opt/rec-platform) is absent`

- **Symptom:** `Enforce directory permissions` failed in `--check` because the create task only reports `changed` without creating.
- **Fix:** Added `state: directory` to the enforce task (`ansible/rec-platform-setup.yml:67`) so check mode is safe and reruns converge.

## 6. Rollback deleted containers (compose CLI missing in runner image)

- **Symptom:** First rollback design ran `docker compose up -d` inside the ephemeral container (`docker.io` client has no `compose` plugin, `docker-compose` v1 absent, rc=127) **after** `docker rm -f` — leaving backend/frontend deleted.
- **Fix:** Stack restored immediately via host `docker compose up -d` (Postgres volume healthy throughout, zero data loss); rollback playbook redesigned to stop/start recovery (`docker stop` → assert stopped → `docker start` → health checks), with orchestrated rebuilds staying on the host. Rerun: rollback `ok=16 failed=0`, backend `Up 16 seconds`, all health checks 200.
- **Lesson:** destructive steps (`rm -f`) must never precede the restore mechanism in the same automation unit; assert the failure state explicitly before recovering.

## 7. Disaster recovery runbook (state preservation)

1. Confirm Postgres volume exists: `docker volume ls | findstr postgres_data`.
2. Never `docker compose down -v` (deletes the volume). Use `docker compose down` or `stop/start`.
3. Restore stack: `docker compose up -d`; wait for `rec-postgres-db (healthy)`.
4. Verify: backend `GET /api/auth/roles → 200`, frontend `GET / → 200`, TCP `127.0.0.1:5433` open.
5. Flyway validates schema v2 on backend boot — no manual migration needed.
6. Full drill: `scripts/run-phase9-validation.ps1 -SkipBuild` (omit the flag to rebuild images).
