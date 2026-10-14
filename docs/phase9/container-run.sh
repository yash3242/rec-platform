set -e
export DEBIAN_FRONTEND=noninteractive
apt-get update -qq
apt-get install -y -qq --no-install-recommends python3 python3-pip python3-apt ca-certificates curl sudo docker.io >/dev/null
python3 -m pip install -q --no-cache-dir ansible
cd /workspace
exec > docs/phase9/runner-container.log 2>&1
echo "===== Step 2a: Deploy Pass 1 (provisioning) ====="
ansible-playbook -i ansible/inventory/hosts.ini ansible/rec-platform-deploy.yml 2>&1 | tee docs/phase9/pass1-deploy.log
echo "===== Step 2b: Deploy Pass 2 (idempotency) ====="
ansible-playbook -i ansible/inventory/hosts.ini ansible/rec-platform-deploy.yml 2>&1 | tee docs/phase9/pass2-idempotency.log
echo "===== Step 4: Rollback and recovery ====="
ansible-playbook -i ansible/inventory/hosts.ini ansible/rec-platform-rollback.yml 2>&1 | tee docs/phase9/rollback.log
echo "PHASE9_RUNNER_DONE"