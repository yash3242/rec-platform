# REC Platform — Limitations & Future Enhancements Roadmap

## 1. Current technical limitations

| # | Limitation | Impact |
|---|---|---|
| 1 | Single-node Docker Compose, no orchestration | No self-healing beyond restart policies; no rolling updates |
| 2 | Local image tags (`rec-backend:v1.0.0`) — no remote registry | Builds are not shareable/reproducible across nodes; no image promotion pipeline |
| 3 | Secrets in compose env + `.env` (JWT secret, DB/seed passwords) | Not audit-safe; rotation is manual; visible in `docker inspect` |
| 4 | Single Postgres instance, volume on one host | No HA/failover; backups are manual (`pg_dump`); PITR absent |
| 5 | No centralized logging/metrics (stdout + `docs/phase9/*.log` only) | Blind spots in production incidents; no alerting/SLAs |
| 6 | Jenkins on one Windows host (port 8081) | Single point of failure; agents not scalable |
| 7 | Selenium gate needs a live `:5173` stack | Flaky-by-environment; cannot run hermetically in CI |
| 8 | Ansible targets `localhost` only | No multi-node fleet management yet |
| 9 | No TLS on `:8080`/`:5173` | Not internet-ready; JWTs and credentials travel in cleartext locally |
| 10 | JWT revocation absent | Logout is client-side only; compromised tokens live until expiry |

## 2. Enterprise scale-out plan

### Kubernetes (EKS/GKE)

- Containerize as today, then ship Helm charts: `backend` Deployment + HPA (CPU/RPS), `frontend` Deployment + Ingress (TLS via cert-manager), Postgres via managed RDS/Cloud SQL (not in-cluster).
- Rolling updates with readiness probes on `/api/auth/roles`; PodDisruptionBudgets for availability.
- Namespaces per environment (`staging`, `prod`); Jenkins or Argo CD GitOps promotion `main → staging → prod`.

### Terraform for cloud infrastructure

- Modules: VPC/networking, EKS/GKE cluster, managed Postgres, container registry (ECR/GCR), Jenkins controller/agents or managed CI.
- Remote state in versioned object storage with locking; `terraform plan` as a PR gate, `apply` only from `main`.

### HashiCorp Vault for secrets

- Replace `.env`/compose env with Vault Agent injector or external-secrets: DB credentials (dynamic, auto-rotating), JWT signing keys (versioned rotation), seed passwords (one-time, then forced reset).
- Audit every secret read; break-glass revocation on incident.

### Prometheus/Grafana monitoring

- Metrics: Spring Boot Actuator + Micrometer → Prometheus; Nginx + Postgres exporters; Selenium gate pass-rate and pipeline duration dashboards in Grafana.
- Alerts: API 5xx rate, p95 latency, JVM heap/GC, Postgres connections/replication lag, Jenkins failure streak, Ansible `failed>0`.
- Logs: structured JSON to Loki/ELK; keep `PLAY RECAP` idempotency checks as deployment annotations.

## 3. Suggested sequencing

1. Remote registry + image promotion (`:v1.0.0` → `:stable`).
2. Vault for DB/JWT secrets; remove secrets from compose.
3. Terraform-managed staging cluster; migrate Compose → Helm.
4. Prometheus/Grafana + alerting; TLS everywhere.
5. Hermetic Selenium (ephemeral stack per build) + contract tests.
