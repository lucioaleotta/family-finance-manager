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

is_app_running() {
  if [[ -f "$PID_FILE" ]]; then
    local pid
    pid="$(cat "$PID_FILE")"
    if kill -0 "$pid" 2>/dev/null; then
      return 0
    fi
  fi
  return 1
}

app_up() {
  if is_app_running; then
    echo "Spring Boot già avviato (PID $(cat "$PID_FILE"))."
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
  if ! [[ -f "$PID_FILE" ]]; then
    echo "Nessun PID file trovato per Spring Boot."
    return
  fi

  local pid
  pid="$(cat "$PID_FILE")"

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
  docker compose -f "$COMPOSE_FILE" up -d
}

db_down() {
  echo "Stop PostgreSQL Docker..."
  docker compose -f "$COMPOSE_FILE" down
}

show_status() {
  echo "=== Spring Boot ==="
  if is_app_running; then
    echo "RUNNING (PID $(cat "$PID_FILE"))"
  else
    echo "STOPPED"
  fi

  echo
  echo "=== Porta 8080 ==="
  lsof -nP -iTCP:8080 -sTCP:LISTEN || true

  echo
  echo "=== Docker PostgreSQL ==="
  docker compose -f "$COMPOSE_FILE" ps
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
      docker compose -f "$COMPOSE_FILE" logs -f postgres
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

usage() {
  cat <<EOF
Uso: ./scripts/dev.sh <comando>

Comandi:
  up           Avvia PostgreSQL + Spring Boot
  down         Ferma Spring Boot + PostgreSQL
  status       Stato app, porta 8080 e container
  logs [target]  Log runtime (target: app | db | all)

Esempi:
  ./scripts/dev.sh up
  ./scripts/dev.sh status
  ./scripts/dev.sh logs app
  ./scripts/dev.sh logs db
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
  *)
    usage
    exit 1
    ;;
esac
