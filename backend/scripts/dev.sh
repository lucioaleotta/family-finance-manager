#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_FILE="$BACKEND_DIR/docker/postgres.yml"
RUN_DIR="$BACKEND_DIR/.run"
LOG_DIR="$BACKEND_DIR/logs"
PID_FILE="$RUN_DIR/backend.pid"
APP_LOG="$LOG_DIR/backend.log"

mkdir -p "$RUN_DIR" "$LOG_DIR"

docker_compose() {
  if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
    docker compose "$@"
  elif command -v docker-compose >/dev/null 2>&1; then
    docker-compose "$@"
  else
    echo "Docker Compose non trovato. Installa Docker Desktop o docker-compose." >&2
    return 127
  fi
}

is_app_running() {
  if get_app_pid >/dev/null 2>&1; then
    return 0
  fi
  return 1
}

get_app_pid() {
  if [[ -f "$PID_FILE" ]]; then
    local pid
    pid="$(cat "$PID_FILE")"
    if kill -0 "$pid" 2>/dev/null; then
      echo "$pid"
      return 0
    fi
  fi

  local detected_pid
  detected_pid="$(pgrep -f "spring-boot:run" | head -n 1 || true)"
  if [[ -n "$detected_pid" ]]; then
    echo "$detected_pid"
    return 0
  fi

  return 1
}

app_up() {
  local running_pid
  if running_pid="$(get_app_pid)"; then
    echo "$running_pid" > "$PID_FILE"
    echo "Spring Boot già avviato (PID $running_pid)."
    return
  fi

  echo "Avvio Spring Boot..."
  (
    cd "$BACKEND_DIR"
    nohup mvn spring-boot:run > "$APP_LOG" 2>&1 &
    echo $! > "$PID_FILE"
  )
  echo "Spring Boot avviato (PID $(cat "$PID_FILE"))."
}

app_down() {
  local pid
  if ! pid="$(get_app_pid)"; then
    echo "Nessun processo Spring Boot trovato."
    rm -f "$PID_FILE"
    return
  fi

  if kill -0 "$pid" 2>/dev/null; then
    echo "Stop Spring Boot (PID $pid)..."
    kill "$pid"
    sleep 1
    if kill -0 "$pid" 2>/dev/null; then
      echo "Processo ancora attivo, invio SIGKILL..."
      kill -9 "$pid" || true
    fi
    echo "Spring Boot fermato."
  else
    echo "PID $pid non attivo."
  fi

  rm -f "$PID_FILE"
}

db_up() {
  echo "Avvio PostgreSQL Docker..."
  docker_compose -f "$COMPOSE_FILE" up -d
}

db_down() {
  echo "Stop PostgreSQL Docker..."
  docker_compose -f "$COMPOSE_FILE" down
}

show_status() {
  echo "=== Spring Boot ==="
  local running_pid
  if running_pid="$(get_app_pid)"; then
    echo "$running_pid" > "$PID_FILE"
    echo "RUNNING (PID $running_pid)"
  else
    echo "STOPPED"
  fi

  echo
  echo "=== Porta 8080 ==="
  if ! lsof -nP -iTCP:8080 -sTCP:LISTEN 2>/dev/null; then
    echo "Nessun processo in ascolto su 8080"
  fi

  echo
  echo "=== Docker PostgreSQL ==="
  docker_compose -f "$COMPOSE_FILE" ps
}

show_logs() {
  local target="${1:-app}"

  case "$target" in
    app)
      echo "Log app: $APP_LOG"
      touch "$APP_LOG"
      tail -f "$APP_LOG"
      ;;
    db|postgres)
      docker_compose -f "$COMPOSE_FILE" logs -f postgres
      ;;
    all)
      echo "Usa due terminali:"
      echo "  ./scripts/dev.sh logs app"
      echo "  ./scripts/dev.sh logs db"
      ;;
    *)
      echo "Target log non valido: $target"
      echo "Valori supportati: app | db | all"
      exit 1
      ;;
  esac
}

db_export() {
  local output_file="${1:-}"
  if [[ -n "$output_file" ]]; then
    "$SCRIPT_DIR/db-export.sh" "$output_file"
  else
    "$SCRIPT_DIR/db-export.sh"
  fi
}

db_import() {
  local backup_file="${1:-}"
  if [[ -z "$backup_file" ]]; then
    echo "Uso: ./scripts/dev.sh db-import <path-backup.dump>"
    exit 1
  fi

  "$SCRIPT_DIR/db-import.sh" "$backup_file"
}

db_smoke_test() {
  local output_file="${1:-}"
  if [[ -n "$output_file" ]]; then
    "$SCRIPT_DIR/db-smoke-test.sh" "$output_file"
  else
    "$SCRIPT_DIR/db-smoke-test.sh"
  fi
}

usage() {
  cat <<EOF
Uso: ./scripts/dev.sh <comando>

Comandi:
  up           Avvia PostgreSQL + Spring Boot
  down         Ferma Spring Boot + PostgreSQL
  status       Stato app, porta 8080 e container
  logs [target]  Log runtime (target: app | db | all)
  db-export [file] Export dump database PostgreSQL
  db-import <file> Import dump database PostgreSQL
  db-smoke-test [file] Esegue smoke test backup/restore con cleanup automatico

Esempi:
  ./scripts/dev.sh up
  ./scripts/dev.sh status
  ./scripts/dev.sh logs app
  ./scripts/dev.sh logs db
  ./scripts/dev.sh db-export
  ./scripts/dev.sh db-import ./backups/finance_YYYYmmdd_HHMMSS.dump
  ./scripts/dev.sh db-smoke-test
  ./scripts/dev.sh down
EOF
}

command="${1:-}"

case "$command" in
  up)
    db_up
    app_up
    ;;
  down)
    app_down
    db_down
    ;;
  status)
    show_status
    ;;
  logs)
    show_logs "${2:-app}"
    ;;
  db-export)
    db_export "${2:-}"
    ;;
  db-import)
    db_import "${2:-}"
    ;;
  db-smoke-test)
    db_smoke_test "${2:-}"
    ;;
  *)
    usage
    exit 1
    ;;
esac
