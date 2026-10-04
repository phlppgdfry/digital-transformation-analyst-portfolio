#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
docker compose exec -T oracle bash -c 'exec sqlplus -s "LAB/$APP_USER_PASSWORD@//localhost:1521/FREEPDB1"'
