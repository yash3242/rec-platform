#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

printf '\nDry-run syntax/plan check:\n'
ansible-playbook -i ansible/inventory/hosts.ini ansible/rec-platform-setup.yml --check

printf '\nActual execution:\n'
ansible-playbook -i ansible/inventory/hosts.ini ansible/rec-platform-setup.yml
