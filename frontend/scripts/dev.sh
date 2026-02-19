#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Colori per output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Funzioni di utilità
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# Funzione per mostrare l'help
show_help() {
    cat << EOF
Family Finance Manager - Frontend Development Script

Usage: ./scripts/dev.sh [COMMAND]

Commands:
  start              Start frontend (Next.js on port 3000)
  stop               Stop frontend
  restart            Restart frontend
  logs               Show frontend logs
  help               Show this help message

Examples:
  ./scripts/dev.sh start
  ./scripts/dev.sh restart
  ./scripts/dev.sh logs
EOF
}

# Frontend functions
start_frontend() {
    log_info "Starting frontend (Next.js)..."
    cd "$PROJECT_ROOT"
    npm run dev > /dev/null 2>&1 &
    sleep 5
    log_info "Frontend started on http://localhost:3000 ✓"
}

stop_frontend() {
    log_info "Stopping frontend..."
    pkill -f "next dev" || true
    log_info "Frontend stopped ✓"
}

restart_frontend() {
    log_info "Restarting frontend..."
    stop_frontend
    sleep 2
    start_frontend
}

show_logs() {
    cd "$PROJECT_ROOT"
    if pgrep -f "next dev" > /dev/null 2>&1; then
        log_info "Frontend is running. Attaching to logs..."
        tail -f .next/debug.log 2>/dev/null || log_warn "Log file not found, showing process output instead..."
    else
        log_warn "Frontend is not running"
    fi
}

# Main logic
COMMAND="${1:-help}"

case "$COMMAND" in
    start)
        start_frontend
        ;;
    stop)
        stop_frontend
        ;;
    restart)
        restart_frontend
        ;;
    logs)
        show_logs
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        log_error "Unknown command: $COMMAND"
        show_help
        exit 1
        ;;
esac
