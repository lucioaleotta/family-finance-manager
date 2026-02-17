#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"
COMPOSE_FILE="$BACKEND_DIR/docker/postgres.yml"
RUN_DIR="$BACKEND_DIR/.run"
LOG_DIR="$BACKEND_DIR/logs"
BACKEND_PID_FILE="$RUN_DIR/backend.pid"
FRONTEND_PID_FILE="$RUN_DIR/frontend.pid"
BACKEND_LOG="$LOG_DIR/backend.log"
FRONTEND_LOG="$LOG_DIR/frontend.log"

mkdir -p "$RUN_DIR" "$LOG_DIR"

is_backend_running() {
  if [[ -f "$BACKEND_PID_FILE" ]]; then
    local pid
    pid="$(cat "$BACKEND_PID_FILE")"
    if kill -0 "$pid" 2>/dev/null; then
      return 0
    fi
  fi
  return 1
}

is_frontend_running() {
  if [[ -f "$FRONTEND_PID_FILE" ]]; then
    local pid
    pid="$(cat "$FRONTEND_PID_FILE")"
    if kill -0 "$pid" 2>/dev/null; then
      return 0
    fi
  fi
  return 1
}

backend_up() {
  if is_backend_running; then
    echo "Spring Boot già avviato (PID $(cat "$BACKEND_PID_FILE"))."
    return
  fi

  echo "Avvio Spring Boot..."
  (
    cd "$BACKEND_DIR"
    nohup mvn spring-boot:run > "$BACKEND_LOG" 2>&1 &
    echo $! > "$BACKEND_PID_FILE"
  )
  echo "Spring Boot avviato (PID $(cat "$BACKEND_PID_FILE"))."
}

backend_down() {
  if ! [[ -f "$BACKEND_PID_FILE" ]]; then
    echo "Nessun PID file trovato per Spring Boot."
    return
  fi

  local pid
  pid="$(cat "$BACKEND_PID_FILE")"

  if kill -0 "$pid" 2>/dev/null; then
    echo "Stop Spring Boot (PID $pid)..."
    kill "$pid"
    sleep 1
    if kill -0 "$pid" 2>/dev/null; then
      echo "Processo backend ancora attivo, invio SIGKILL..."
      kill -9 "$pid" || true
    fi
    echo "Spring Boot fermato."
  else
    echo "PID backend $pid non attivo."
  fi

  rm -f "$BACKEND_PID_FILE"
}

frontend_up() {
  if ! [[ -d "$FRONTEND_DIR" ]]; then
    echo "Frontend non trovato in $FRONTEND_DIR (skip)."
    return
  fi

  if is_frontend_running; then
    echo "Frontend già avviato (PID $(cat "$FRONTEND_PID_FILE"))."
    return
  fi

  if ! command -v npm >/dev/null 2>&1; then
    echo "Errore: npm non disponibile, impossibile avviare il frontend."
    return
  fi

  echo "Avvio Frontend (Next.js)..."
  (
    cd "$FRONTEND_DIR"
    nohup npm run dev > "$FRONTEND_LOG" 2>&1 &
    echo $! > "$FRONTEND_PID_FILE"
  )
  echo "Frontend avviato (PID $(cat "$FRONTEND_PID_FILE"))."
}

frontend_down() {
  if ! [[ -f "$FRONTEND_PID_FILE" ]]; then
    echo "Nessun PID file trovato per Frontend."
    return
  fi

  local pid
  pid="$(cat "$FRONTEND_PID_FILE")"

  if kill -0 "$pid" 2>/dev/null; then
    echo "Stop Frontend (PID $pid)..."
    kill "$pid"
    sleep 1
    if kill -0 "$pid" 2>/dev/null; then
      echo "Processo frontend ancora attivo, invio SIGKILL..."
      kill -9 "$pid" || true
    fi
    echo "Frontend fermato."
  else
    echo "PID frontend $pid non attivo."
  fi

  rm -f "$FRONTEND_PID_FILE"
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
  if is_backend_running; then
    echo "RUNNING (PID $(cat "$BACKEND_PID_FILE"))"
  else
    echo "STOPPED"
  fi

  echo
  echo "=== Frontend (Next.js) ==="
  if is_frontend_running; then
    echo "RUNNING (PID $(cat "$FRONTEND_PID_FILE"))"
  else
    echo "STOPPED"
  fi

  echo
  echo "=== Porta 8080 ==="
  lsof -nP -iTCP:8080 -sTCP:LISTEN || true

  echo
  echo "=== Porta 3000 ==="
  lsof -nP -iTCP:3000 -sTCP:LISTEN || true

  echo
  echo "=== Docker PostgreSQL ==="
  docker compose -f "$COMPOSE_FILE" ps
}

show_logs() {
  local target="${1:-app}"

  case "$target" in
    app|backend)
      echo "Log backend: $BACKEND_LOG"
      touch "$BACKEND_LOG"
      tail -f "$BACKEND_LOG"
      ;;
    fe|frontend)
      echo "Log frontend: $FRONTEND_LOG"
      touch "$FRONTEND_LOG"
      tail -f "$FRONTEND_LOG"
      ;;
    db|postgres)
      docker compose -f "$COMPOSE_FILE" logs -f postgres
      ;;
    all)
      echo "Usa tre terminali:"
      echo "  ./scripts/dev.sh logs app"
      echo "  ./scripts/dev.sh logs fe"
      echo "  ./scripts/dev.sh logs db"
      ;;
    *)
      echo "Target log non valido: $target"
      echo "Valori supportati: app | fe | db | all"
      exit 1
      ;;
  esac
}

usage() {
  cat <<EOF
Uso: ./scripts/dev.sh <comando>

Comandi:
  up             Avvia PostgreSQL + Spring Boot + Frontend
  down           Ferma Frontend + Spring Boot + PostgreSQL
  status         Stato backend/frontend, porte 8080/3000 e container
  logs [target]  Log runtime (target: app | fe | db | all)

Esempi:
  ./scripts/dev.sh up
  ./scripts/dev.sh status
  ./scripts/dev.sh logs app
  ./scripts/dev.sh logs fe
  ./scripts/dev.sh logs db
  ./scripts/dev.sh down
EOF
}

command="${1:-}"

case "$command" in
  up)
    db_up
    backend_up
    frontend_up
    ;;
  down)
    frontend_down
    backend_down
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
