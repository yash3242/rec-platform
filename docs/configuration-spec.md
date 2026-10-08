# REC Platform — Phase 8 Configuration Management Specification

## Scope
This document specifies the operating system baseline, service account, directories, and network policy for the REC Platform staging/local Linux host. It is designed so the same inventory can target WSL Ubuntu, a local VM, or a remote staging host.

## Target topology

```
[rec_servers]
localhost ansible_connection=local
```

## System packages

| Package | Purpose |
|---|---|
| docker-ce | Docker Engine daemon |
| docker-ce-cli | Docker CLI |
| containerd.io | Container runtime |
| docker-buildx-plugin | Docker Buildx |
| docker-compose-plugin | Docker Compose v2 plugin |
| openjdk-17-jdk | Backend build/runtime toolchain |
| postgresql-client-16 | PostgreSQL client tooling |
| git | Source checkout |
| curl | HTTP health probes |
| ufw | Host firewall |
| iptables | Network traffic control rules |
| ca-certificates | HTTPS package mirrors |
| software-properties-common | APT repository helpers |

## User & groups

| Item | Value |
|---|---|
| User | `recadmin` |
| Group | `recadmin` |
| Shell | `/bin/bash` |
| Sudo | Full sudo via `/etc/sudoers.d/recadmin` |
| Extra groups | `docker`, `sudo`, `deploy` optional |

## Directory hierarchy and permissions

| Path | Purpose | Owner | Mode |
|---|---|---|---|
| `/opt/rec-platform` | Platform root | `recadmin:recadmin` | `0755` |
| `/opt/rec-platform/backend` | Backend source/config | `recadmin:recadmin` | `0755` |
| `/opt/rec-platform/frontend` | Frontend source/config | `recadmin:recadmin` | `0755` |
| `/opt/rec-platform/logs` | Runtime logs | `recadmin:recadmin` | `0755` |
| `/opt/rec-platform/config` | Environment/config files | `recadmin:recadmin` | `0755` |
| Files under `/opt/rec-platform` | Configuration/source metadata | `recadmin:recadmin` | `0644` |

## Network / firewall rules

| Direction | Port | Protocol | Purpose |
|---|---|---|---|
| Inbound | `8080` | TCP | Backend API |
| Inbound | `5173` | TCP | Frontend UI / Nginx development proxy |
| Inbound | `80` | TCP | Frontend UI / Nginx |
| Inbound | `5432` | TCP | PostgreSQL DB |
| Inbound | `5433` | TCP | PostgreSQL alternate/exposed port |
| Inbound | `8081` | TCP | Jenkins CI |
| Inbound | `22` | TCP | SSH |

## Service expectations

| Service | Expected state |
|---|---|
| `docker` | enabled and running |
| `ufw` | enabled |
| `postgresql` | running when container alternatives are not used |
| `nginx` | running when frontend is deployed outside Docker |
