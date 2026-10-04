#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if [ ! -f .env ]; then
  python3 - <<'PYGEN'
import secrets
from pathlib import Path
Path('.env').write_text('ORACLE_PASSWORD=Adm'+secrets.token_hex(12)+'\nAPP_PASSWORD=App'+secrets.token_hex(12)+'\nLAB_PORT=18081\n')
Path('.env').chmod(0o600)
PYGEN
fi
docker compose up -d --build
