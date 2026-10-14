# REC Platform — Viva Defense & Evaluation Cheat Sheet

## 1. Q&A (senior-level, concise)

1. **What does the system do?** Role-based lifecycle for renewable-energy assets → generation logs → minted RECs → marketplace listing → purchase → retirement, with JWT auth and audit history.
2. **Why Ansible over Puppet/Chef?** Agentless (SSH/WinRM only), YAML idempotent tasks ideal for our Desired-State baseline, ephemeral-runner friendly on Windows/Docker; Puppet/Chef need agents and heavier infra for a single-node staging target.
3. **Why Jenkins here?** Declarative `Jenkinsfile` colocated with code; 4 explicit stages (checkout → test gate → docker build → health check) with JUnit publishing; simplest self-hosted fit for a Windows Docker Desktop lab.
4. **How does Jenkins gate bad code?** `mvn clean test` runs 3 unit + 4 Selenium tests; any failure/ERROR fails the stage, JUnit + screenshots archive as evidence, and later stages (build/deploy) never run.
5. **How was idempotency proven?** Same deploy playbook run twice in one container session: Pass 1 `ok=19 changed=5 failed=0`, Pass 2 `ok=19 changed=0 failed=0`. `changed=0` on a converged state is the operational definition of idempotency.
6. **Why two passes in ONE container?** Separate fresh containers would each report changes (new filesystem); idempotency is only meaningful as rerun-against-converged-state.
7. **What is `changed` vs `failed` vs `ok`?** `ok` = task verified state; `changed` = task mutated state; `failed` = task errored. Green idempotent rerun: high `ok`, `changed=0`, `failed=0`.
8. **How is DB persistence preserved during rollbacks?** Postgres data lives in the named `postgres_data` volume, independent of container lifecycle; rollback uses `stop/start` (never `down -v`); Flyway validates (not rebuilds) schema on boot.
9. **Why did the first rollback design fail, and what changed?** It ran `docker rm -f` before a compose restore, but the runner image had no compose CLI (rc=127) — containers were deleted. Redesigned to stop/start recovery; orchestrated rebuilds stay on the host.
10. **Why an ephemeral Ubuntu container for Ansible on Windows?** WSL had no usable distro and Alpine images lack `apt`/`python3-apt`; `ubuntu:22.04` + mounted repo + mounted docker.sock gives a reproducible, `--rm`-cleaned runner.
11. **Why mount `/var/run/docker.sock`?** So in-container `docker` CLI and health probes act on the host's stack; without it the playbook could only manage the container's own filesystem.
12. **How do health checks differ across layers?** Ansible `uri` (HTTP 200 on backend/frontend) + `wait_for` (TCP 5433) inside the runner; host-side `Invoke-WebRequest` with timings as authoritative evidence.
13. **What does `/api/auth/roles` prove?** Backend booted, Spring context + JWT filter chain up, publicly reachable contract (`["ADMIN","GENERATOR","BUYER"]`) — cheap, auth-free readiness probe.
14. **How are roles enforced?** JWT role claim → Spring `@PreAuthorize` on controllers + service-level ownership checks (generators see only own assets/logs); Selenium Journey 1 asserts role-based redirects.
15. **Why did `localhost` vs `127.0.0.1` matter?** Headless Chrome/proxy handling of `localhost` flaked; `127.0.0.1` bypasses proxy resolution. Base URL is env-overridable (`FRONTEND_BASE_URL`).
16. **What was the Jenkins BOM failure?** PowerShell wrote a UTF-8 BOM before `pipeline`; Groovy saw `﻿pipeline` as an unknown DSL method. Fixed with BOM-free ASCII, verified via `Format-Hex`.
17. **Why no `tools{}` block in Jenkinsfile?** Aliases `Maven3`/`JDK17` were never defined in Global Tool Configuration; agents already have both on PATH, so direct `bat 'mvn ...'` is deterministic.
18. **How are Selenium flakes controlled?** Explicit waits (`presenceOfElementLocated` before interacting), seeded users, unique generated e-mails/asset codes per run (no dirty-state collisions), failure screenshots in `target/screenshots/`.
19. **Why `docker.io` instead of `docker-ce` in the playbook?** Stock Ubuntu archives (no Docker apt repo setup) — portable across runners; documented drift vs the spec's `docker-ce` naming.
20. **Single-node Compose limits?** No HA, no rolling deploys, local tags, manual secrets — see `limitations-and-future-scope.md` for the K8s/Terraform/Vault/Prometheus path.
21. **How would you take this to production tomorrow?** Push images to a registry, Vault the secrets, Terraform a managed cluster + managed Postgres, TLS ingress, Prometheus/Grafana alerts, hermetic Selenium per build.

## 2. Live demo script (exact commands)

```powershell
# 0. Repo + pipeline definition
cd C:\Users\Yash\Desktop\rec-platform
git log --oneline -5
Get-Content Jenkinsfile

# 1. Running stack
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# 2. Health (authoritative)
Invoke-WebRequest -Uri 'http://localhost:8080/api/auth/roles' -UseBasicParsing
Invoke-WebRequest -Uri 'http://localhost:5173' -UseBasicParsing | Select-Object StatusCode

# 3. Quality gate (7 tests green)
cd C:\Users\Yash\Desktop\rec-platform\backend
mvn test
# expect: Tests run: 7, Failures: 0, Errors: 0 — BUILD SUCCESS

# 4. Phase 9 full drill (deploy x2 + rollback + health)
powershell -ExecutionPolicy Bypass -File C:\Users\Yash\Desktop\rec-platform\scripts\run-phase9-validation.ps1 -SkipBuild
Get-Content ..\docs\phase9\health-checks.log

# 5. Evidence
Select-String -Path ..\docs\phase9\*.log -Pattern 'PLAY RECAP' -Context 0,2
```

Jenkins click-path: `http://localhost:8081` → `rec-platform-pipeline` → **Build Now** → expect green through Health Check verification.
