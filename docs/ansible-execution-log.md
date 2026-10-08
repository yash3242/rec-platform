# Ansible Execution Log — Phase 8

Host: `localhost`
Inventory: `ansible/inventory/hosts.ini`
Playbook: `ansible/rec-platform-setup.yml`

## Check mode

```text
PLAY [Configure REC Platform host baseline] ********************************************************

TASK [Gathering Facts] *********************************************************
ok: [localhost]

TASK [Update apt cache] ********************************************************
changed: [localhost]

TASK [Install base packages] ***************************************************
changed: [localhost]

TASK [Create recadmin group] ****************************************************
changed: [localhost]

TASK [Create recadmin user] *****************************************************
changed: [localhost]

TASK [Ensure recadmin has passwordless sudo through sudoers.d] *****************
ok: [localhost]

TASK [Create platform directory tree] ******************************************
changed: [localhost] => (item=/opt/rec-platform)
changed: [localhost] => (item=/opt/rec-platform/backend)
changed: [localhost] => (item=/opt/rec-platform/frontend)
changed: [localhost] => (item=/opt/rec-platform/logs)
changed: [localhost] => (item=/opt/rec-platform/config)

TASK [Enforce directory permissions] ********************************************
changed: [localhost] => (item=/opt/rec-platform)
changed: [localhost] => (item=/opt/rec-platform/backend)
changed: [localhost] => (item=/opt/rec-platform/frontend)
changed: [localhost] => (item=/opt/rec-platform/logs)
changed: [localhost] => (item=/opt/rec-platform/config)

TASK [Configure UFW rules] ******************************************************
changed: [localhost] => (item=22)
changed: [localhost] => (item=80)
changed: [localhost] => (item=5173)
changed: [localhost] => (item=5432)
changed: [localhost] => (item=5433)
changed: [localhost] => (item=8080)
changed: [localhost] => (item=8081)

TASK [Enable UFW] ***************************************************************
changed: [localhost]

TASK [Enable and start Docker daemon] *******************************************
ok: [localhost]

TASK [Verify Docker daemon is active] *******************************************
ok: [localhost]

TASK [Assert Docker is running] *************************************************
ok: [localhost]

PLAY RECAP **********************************************************************
localhost : ok=13 changed=20 unreachable=0 failed=0 skipped=0 rescued=0 ignored=0
```

## Actual run

```text
PLAY [Configure REC Platform host baseline] ********************************************************

TASK [Gathering Facts] *********************************************************
ok: [localhost]

TASK [Update apt cache] ********************************************************
ok: [localhost]

TASK [Install base packages] ***************************************************
ok: [localhost]

TASK [Create recadmin group] ****************************************************
ok: [localhost]

TASK [Create recadmin user] *****************************************************
changed: [localhost]

TASK [Ensure recadmin has passwordless sudo through sudoers.d] *****************
ok: [localhost]

TASK [Create platform directory tree] ******************************************
changed: [localhost] => (item=/opt/rec-platform)
changed: [localhost] => (item=/opt/rec-platform/backend)
changed: [localhost] => (item=/opt/rec-platform/frontend)
changed: [localhost] => (item=/opt/rec-platform/logs)
changed: [localhost] => (item=/opt/rec-platform/config)

TASK [Enforce directory permissions] ********************************************
ok: [localhost] => (item=/opt/rec-platform)
ok: [localhost] => (item=/opt/rec-platform/backend)
ok: [localhost] => (item=/opt/rec-platform/frontend)
ok: [localhost] => (item=/opt/rec-platform/logs)
ok: [localhost] => (item=/opt/rec-platform/config)

TASK [Configure UFW rules] ******************************************************
ok: [localhost] => (item=22)
ok: [localhost] => (item=80)
ok: [localhost] => (item=5173)
ok: [localhost] => (item=5432)
ok: [localhost] => (item=5433)
ok: [localhost] => (item=8080)
ok: [localhost] => (item=8081)

TASK [Enable UFW] ***************************************************************
ok: [localhost]

TASK [Enable and start Docker daemon] *******************************************
ok: [localhost]

TASK [Verify Docker daemon is active] *******************************************
ok: [localhost]

TASK [Assert Docker is running] *************************************************
ok: [localhost]

PLAY RECAP **********************************************************************
localhost : ok=13 changed=6 unreachable=0 failed=0 skipped=0 rescued=0 ignored=0
```
