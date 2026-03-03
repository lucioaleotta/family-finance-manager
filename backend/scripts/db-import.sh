#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 ]]; then
  echo "Uso: $0 <path-backup.dump>" >&2
  exit 1
fi

BACKUP_FILE="$1"
if [[ ! -f "$BACKUP_FILE" ]]; then
  echo "Errore: file backup non trovato: $BACKUP_FILE" >&2
  exit 1
fi

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-finance}"
DB_USER="${DB_USER:-finance}"
DB_PASSWORD="${DB_PASSWORD:-finance}"
DB_CONTAINER="${DB_CONTAINER:-finance-postgres}"
FORCE="${FORCE:-false}"

is_container_running() {
  docker ps --format '{{.Names}}' | grep -Fxq "$DB_CONTAINER"
}

confirm_restore() {
  if [[ "$FORCE" == "true" ]]; then
    return 0
  fi

  echo "ATTENZIONE: restore su database '$DB_NAME' sovrascriverà i dati correnti."
  read -r -p "Confermi restore da '$BACKUP_FILE'? [y/N] " answer
  if [[ "$answer" != "y" && "$answer" != "Y" ]]; then
    echo "Restore annullato."
    exit 0
  fi
}

restore_from_container() {
  echo "Import DB su container Docker '$DB_CONTAINER' da $BACKUP_FILE"
  cat "$BACKUP_FILE" | docker exec -i -e PGPASSWORD="$DB_PASSWORD" "$DB_CONTAINER" \
    pg_restore --clean --if-exists --no-owner --no-privileges -h localhost -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME"
}

restore_from_local() {
  if ! command -v pg_restore >/dev/null 2>&1; then
    echo "Errore: pg_restore non trovato e container '$DB_CONTAINER' non attivo." >&2
    exit 1
  fi

  echo "Import DB su host locale ${DB_HOST}:${DB_PORT}/${DB_NAME} da $BACKUP_FILE"
  PGPASSWORD="$DB_PASSWORD" pg_restore --clean --if-exists --no-owner --no-privileges \
    -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" "$BACKUP_FILE"
}

confirm_restore

if command -v docker >/dev/null 2>&1 && is_container_running; then
  restore_from_container
else
  restore_from_local
fi

echo "Restore completato da: $BACKUP_FILE"
