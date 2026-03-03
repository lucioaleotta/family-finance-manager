#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_USER="${DB_USER:-finance}"
DB_PASSWORD="${DB_PASSWORD:-finance}"
DB_CONTAINER="${DB_CONTAINER:-finance-postgres}"
SOURCE_DB_NAME="${SOURCE_DB_NAME:-finance}"
SMOKE_DB_NAME="${SMOKE_DB_NAME:-finance_smoke}"

TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
BACKUP_FILE="${1:-$SCRIPT_DIR/../backups/smoke_test_${TIMESTAMP}.dump}"

if [[ "$SOURCE_DB_NAME" == "$SMOKE_DB_NAME" ]]; then
  echo "Errore: SOURCE_DB_NAME e SMOKE_DB_NAME devono essere diversi." >&2
  exit 1
fi

container_exists() {
  docker ps -a --format '{{.Names}}' | grep -Fxq "$DB_CONTAINER"
}

is_container_running() {
  docker ps --format '{{.Names}}' | grep -Fxq "$DB_CONTAINER"
}

run_psql_admin() {
  local sql="$1"
  if command -v docker >/dev/null 2>&1 && is_container_running; then
    docker exec -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" \
      psql -U "$DB_USER" -d postgres -v ON_ERROR_STOP=1 -c "$sql"
  elif command -v psql >/dev/null 2>&1; then
    PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres -v ON_ERROR_STOP=1 -c "$sql"
  else
    echo "Errore: impossibile eseguire psql (né container attivo né client locale disponibile)." >&2
    exit 1
  fi
}

cleanup() {
  echo "Cleanup: rimozione database di smoke test '$SMOKE_DB_NAME'..."
  run_psql_admin "DROP DATABASE IF EXISTS ${SMOKE_DB_NAME};" || true
}

trap cleanup EXIT

if command -v docker >/dev/null 2>&1 && container_exists && ! is_container_running; then
  docker start "$DB_CONTAINER" >/dev/null
fi

echo "[1/4] Export database sorgente '$SOURCE_DB_NAME'..."
DB_NAME="$SOURCE_DB_NAME" "$SCRIPT_DIR/db-export.sh" "$BACKUP_FILE"

echo "[2/4] Preparo database smoke '$SMOKE_DB_NAME'..."
run_psql_admin "DROP DATABASE IF EXISTS ${SMOKE_DB_NAME};"
run_psql_admin "CREATE DATABASE ${SMOKE_DB_NAME};"

echo "[3/4] Restore dump su '$SMOKE_DB_NAME'..."
DB_NAME="$SMOKE_DB_NAME" FORCE=true "$SCRIPT_DIR/db-import.sh" "$BACKUP_FILE"

echo "[4/4] Verifica tabelle ripristinate..."
TABLES_COUNT_RAW="$(
  if command -v docker >/dev/null 2>&1 && is_container_running; then
    docker exec -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" \
      psql -U "$DB_USER" -d "$SMOKE_DB_NAME" -At -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public';"
  else
    PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$SMOKE_DB_NAME" -At -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public';"
  fi
)"
TABLES_COUNT="$(echo "$TABLES_COUNT_RAW" | tr -d '[:space:]')"

if [[ -z "$TABLES_COUNT" || "$TABLES_COUNT" -eq 0 ]]; then
  echo "Smoke test fallito: nessuna tabella trovata su '$SMOKE_DB_NAME'." >&2
  exit 1
fi

echo "Smoke test OK: backup/restore verificato (tabelle ripristinate: $TABLES_COUNT)."
echo "Backup smoke creato: $BACKUP_FILE"
