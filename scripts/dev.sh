#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
BACKEND_DIR="$PROJECT_ROOT/backend"
FRONTEND_DIR="$PROJECT_ROOT/frontend"
DOCKER_DIR="$PROJECT_ROOT/backend/docker"
LOCAL_DOCKER_COMPOSE_FILE="$PROJECT_ROOT/docker-compose.local.yml"

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

docker_compose() {
    if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
        docker compose "$@"
    elif command -v docker-compose >/dev/null 2>&1; then
        docker-compose "$@"
    else
        log_error "Docker Compose non trovato. Installa Docker Desktop o docker-compose."
        return 127
    fi
}

# Funzione per mostrare l'help
show_help() {
    cat << EOF
Family Finance Manager - Development Script

Usage: ./dev.sh [COMMAND] [OPTIONS]

Commands:
  up                Start all services (database, backend, frontend)
    up-prod           Start all services with backend profile prod
  down              Stop all services
  restart           Restart all services
  status            Show status of all services
  logs              Show logs from all services
  
  db-start          Start database only
  db-stop           Stop database only
  db-restart        Restart database only
  db-logs           Show database logs
  
  backend-start     Start backend only
    backend-start-prod Start backend only with profile prod
  backend-stop      Stop backend only
  backend-restart   Restart backend only
  backend-logs      Show backend logs
  backend-build     Build backend (compile + skip tests)
  
  frontend-start    Start frontend only
  frontend-stop     Stop frontend only
  frontend-restart  Restart frontend only
  frontend-logs     Show frontend logs

    docker-local-up      Start full local Docker stack (db, backend, frontend)
    docker-local-up-fast Start local Docker stack without rebuild
    docker-local-down    Stop full local Docker stack
    docker-local-logs    Show logs from local Docker stack
    docker-local-status  Show status of local Docker stack
  
  help              Show this help message

Examples:
  ./dev.sh up                    # Start all services
    ./dev.sh up-prod               # Start all services with backend prod profile
  ./dev.sh db-restart            # Restart only database
  ./dev.sh backend-build         # Build backend
  ./dev.sh frontend-logs         # Show frontend logs
    ./dev.sh docker-local-up       # Start full Docker local stack
    ./dev.sh docker-local-up-fast  # Start Docker local stack without rebuild
EOF
}

# Database functions
start_database() {
    log_info "Starting database..."
    cd "$DOCKER_DIR"
    docker_compose -f postgres.yml up -d
    sleep 3
    log_info "Database started ✓"
}

stop_database() {
    log_info "Stopping database..."
    cd "$DOCKER_DIR"
    docker_compose -f postgres.yml down
    log_info "Database stopped ✓"
}

restart_database() {
    stop_database
    sleep 2
    start_database
}

db_logs() {
    cd "$DOCKER_DIR"
    docker_compose -f postgres.yml logs -f
}

# Backend functions
start_backend() {
    log_info "Starting backend..."
    cd "$BACKEND_DIR"
    mvn spring-boot:run -DskipTests > /dev/null 2>&1 &
    sleep 10
    log_info "Backend started on http://localhost:8080 ✓"
}

start_backend_prod() {
    local required_vars=(
        "JWT_ISSUER"
        "JWT_SECRET"
        "JWT_ACCESS_TOKEN_MINUTES"
        "PASSWORD_RESET_TOKEN_MINUTES"
        "PASSWORD_RESET_URL"
        "PASSWORD_RESET_FROM_EMAIL"
    )

    local missing=0
    for var_name in "${required_vars[@]}"; do
        if [[ -z "${!var_name:-}" ]]; then
            log_error "Missing required env var for prod profile: $var_name"
            missing=1
        fi
    done

    if [[ "$missing" -eq 1 ]]; then
        log_warn "Example: SPRING_PROFILES_ACTIVE=prod JWT_ISSUER=... JWT_SECRET=... ./scripts/dev.sh up-prod"
        return 1
    fi

    log_info "Starting backend with prod profile..."
    cd "$BACKEND_DIR"
    SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run -DskipTests > /dev/null 2>&1 &
    sleep 10
    log_info "Backend (prod profile) started on http://localhost:8080 ✓"
}

stop_backend() {
    log_info "Stopping backend..."
    pkill -f "spring-boot:run" || true
    log_info "Backend stopped ✓"
}

restart_backend() {
    stop_backend
    sleep 2
    start_backend
}

build_backend() {
    log_info "Building backend..."
    cd "$BACKEND_DIR"
    mvn clean compile -DskipTests
    log_info "Backend built ✓"
}

backend_logs() {
    cd "$BACKEND_DIR"
    tail -f logs/backend.log 2>/dev/null || log_warn "Log file not found"
}

# Frontend functions
start_frontend() {
    log_info "Starting frontend..."
    cd "$FRONTEND_DIR"
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
    stop_frontend
    sleep 2
    start_frontend
}

frontend_logs() {
    cd "$FRONTEND_DIR"
    tail -f .next/debug.log 2>/dev/null || log_warn "Log file not found"
}

# Combined functions
start_all() {
    log_info "Starting all services..."
    start_database
    start_backend
    start_frontend
    log_info "All services started ✓"
    show_status
}

start_all_prod() {
    log_info "Starting all services (backend prod profile)..."
    start_database
    start_backend_prod
    start_frontend
    log_info "All services started (backend prod profile) ✓"
    show_status
}

stop_all() {
    log_info "Stopping all services..."
    stop_frontend
    stop_backend
    stop_database
    log_info "All services stopped ✓"
}

restart_all() {
    stop_all
    sleep 2
    start_all
}

show_status() {
    echo -e "\n${YELLOW}=== Service Status ===${NC}"
    
    # Database
    cd "$DOCKER_DIR"
    if docker_compose -f postgres.yml ps 2>/dev/null | grep -q "Up"; then
        echo -e "Database:  ${GREEN}✓ Running${NC}"
    else
        echo -e "Database:  ${RED}✗ Stopped${NC}"
    fi
    
    # Backend
    if pgrep -f "spring-boot:run" > /dev/null 2>&1; then
        echo -e "Backend:   ${GREEN}✓ Running${NC} (http://localhost:8080)"
    else
        echo -e "Backend:   ${RED}✗ Stopped${NC}"
    fi
    
    # Frontend
    if pgrep -f "next dev" > /dev/null 2>&1; then
        echo -e "Frontend:  ${GREEN}✓ Running${NC} (http://localhost:3000)"
    else
        echo -e "Frontend:  ${RED}✗ Stopped${NC}"
    fi
    echo ""
}

show_all_logs() {
    log_info "Showing logs (press Ctrl+C to exit)..."
    log_warn "Use individual commands for detailed logs:"
    echo "  ./dev.sh db-logs"
    echo "  ./dev.sh backend-logs"
    echo "  ./dev.sh frontend-logs"
}

# Local Docker full-stack functions
start_local_docker_stack() {
    if [[ ! -f "$LOCAL_DOCKER_COMPOSE_FILE" ]]; then
        log_error "File non trovato: $LOCAL_DOCKER_COMPOSE_FILE"
        return 1
    fi

    log_info "Starting local Docker stack..."
    cd "$PROJECT_ROOT"
    docker_compose -f "$LOCAL_DOCKER_COMPOSE_FILE" up -d --build
    log_info "Local Docker stack started ✓"
    log_info "Frontend: http://localhost:3000 | Backend: http://localhost:8080 | PostgreSQL: localhost:5432"
}

start_local_docker_stack_fast() {
    if [[ ! -f "$LOCAL_DOCKER_COMPOSE_FILE" ]]; then
        log_error "File non trovato: $LOCAL_DOCKER_COMPOSE_FILE"
        return 1
    fi

    log_info "Starting local Docker stack (fast, no rebuild)..."
    cd "$PROJECT_ROOT"
    docker_compose -f "$LOCAL_DOCKER_COMPOSE_FILE" up -d
    log_info "Local Docker stack started ✓"
    log_info "Frontend: http://localhost:3000 | Backend: http://localhost:8080 | PostgreSQL: localhost:5432"
}

stop_local_docker_stack() {
    if [[ ! -f "$LOCAL_DOCKER_COMPOSE_FILE" ]]; then
        log_error "File non trovato: $LOCAL_DOCKER_COMPOSE_FILE"
        return 1
    fi

    log_info "Stopping local Docker stack..."
    cd "$PROJECT_ROOT"
    docker_compose -f "$LOCAL_DOCKER_COMPOSE_FILE" down
    log_info "Local Docker stack stopped ✓"
}

local_docker_stack_logs() {
    if [[ ! -f "$LOCAL_DOCKER_COMPOSE_FILE" ]]; then
        log_error "File non trovato: $LOCAL_DOCKER_COMPOSE_FILE"
        return 1
    fi

    cd "$PROJECT_ROOT"
    docker_compose -f "$LOCAL_DOCKER_COMPOSE_FILE" logs -f
}

local_docker_stack_status() {
    if [[ ! -f "$LOCAL_DOCKER_COMPOSE_FILE" ]]; then
        log_error "File non trovato: $LOCAL_DOCKER_COMPOSE_FILE"
        return 1
    fi

    cd "$PROJECT_ROOT"
    docker_compose -f "$LOCAL_DOCKER_COMPOSE_FILE" ps
}

# Main logic
COMMAND="${1:-help}"

case "$COMMAND" in
    up)
        start_all
        ;;
    up-prod)
        start_all_prod
        ;;
    down)
        stop_all
        ;;
    restart)
        restart_all
        ;;
    status)
        show_status
        ;;
    logs)
        show_all_logs
        ;;
    db-start)
        start_database
        ;;
    db-stop)
        stop_database
        ;;
    db-restart)
        restart_database
        ;;
    db-logs)
        db_logs
        ;;
    backend-start)
        start_backend
        ;;
    backend-start-prod)
        start_backend_prod
        ;;
    backend-stop)
        stop_backend
        ;;
    backend-restart)
        restart_backend
        ;;
    backend-logs)
        backend_logs
        ;;
    backend-build)
        build_backend
        ;;
    frontend-start)
        start_frontend
        ;;
    frontend-stop)
        stop_frontend
        ;;
    frontend-restart)
        restart_frontend
        ;;
    frontend-logs)
        frontend_logs
        ;;
    docker-local-up)
        start_local_docker_stack
        ;;
    docker-local-up-fast)
        start_local_docker_stack_fast
        ;;
    docker-local-down)
        stop_local_docker_stack
        ;;
    docker-local-logs)
        local_docker_stack_logs
        ;;
    docker-local-status)
        local_docker_stack_status
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
