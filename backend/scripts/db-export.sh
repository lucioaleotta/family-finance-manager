#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
BACKUP_DIR="$BACKEND_DIR/backups"

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-finance}"
DB_USER="${DB_USER:-finance}"
DB_PASSWORD="${DB_PASSWORD:-finance}"
DB_CONTAINER="${DB_CONTAINER:-finance-postgres}"

TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
OUTPUT_FILE="${1:-$BACKUP_DIR/${DB_NAME}_${TIMESTAMP}.dump}"

mkdir -p "$(dirname "$OUTPUT_FILE")"

is_container_running() {
  docker ps --format '{{.Names}}' | grep -Fxq "$DB_CONTAINER"
}

export_from_container() {
  echo "Export DB da container Docker '$DB_CONTAINER' -> $OUTPUT_FILE"
  docker exec -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" \
    pg_dump -Fc -h localhost -p "$DB_PORT" -U "$DB_USER" "$DB_NAME" > "$OUTPUT_FILE"
}

export_from_local() {
  if ! command -v pg_dump >/dev/null 2>&1; then
    echo "Errore: pg_dump non trovato e container '$DB_CONTAINER' non attivo." >&2
    exit 1
  fi

  echo "Export DB da host locale ${DB_HOST}:${DB_PORT}/${DB_NAME} -> $OUTPUT_FILE"
  PGPASSWORD="$DB_PASSWORD" pg_dump -Fc -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" "$DB_NAME" > "$OUTPUT_FILE"
}

if command -v docker >/dev/null 2>&1 && is_container_running; then
  export_from_container
else
  export_from_local
fi

if [[ ! -s "$OUTPUT_FILE" ]]; then
  echo "Errore: backup creato ma vuoto: $OUTPUT_FILE" >&2
  exit 1
fi

echo "Backup completato: $OUTPUT_FILE"
