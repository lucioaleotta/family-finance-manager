# Family Finance Manager

Backend application to manage household budget, including:
- Expenses and incomes
- Monthly balances
- Assets and net worth
- Financial reporting

## Tech Stack
- Java 21
- Spring Boot
- Spring Modulith
- Hexagonal Architecture
- PostgreSQL
- Docker

## Architecture
This project follows a modular monolith approach using Spring Modulith and Domain-Driven Design principles.

More details in `docs/architecture.md`.

## Getting Started

### Prerequisites
- Java 21+
- Maven 3.8+
- Node.js 18+ & npm
- Docker & Docker Compose

### Development Script

The project includes a development script (`scripts/dev.sh`) to manage all services (Database, Backend, Frontend).

**Start all services:**
```bash
./scripts/dev.sh up
```

**Services will be available at:**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080

**Individual service management:**

```bash
# Database (PostgreSQL)
./scripts/dev.sh db-start      # Start database
./scripts/dev.sh db-stop       # Stop database
./scripts/dev.sh db-restart    # Restart database
./scripts/dev.sh db-logs       # Show database logs

# Backend (Spring Boot)
./scripts/dev.sh backend-start    # Start backend
./scripts/dev.sh backend-stop     # Stop backend
./scripts/dev.sh backend-restart  # Restart backend
./scripts/dev.sh backend-build    # Build backend (compile + skip tests)
./scripts/dev.sh backend-logs     # Show backend logs

# Frontend (Next.js)
./scripts/dev.sh frontend-start    # Start frontend
./scripts/dev.sh frontend-stop     # Stop frontend
./scripts/dev.sh frontend-restart  # Restart frontend
./scripts/dev.sh frontend-logs     # Show frontend logs
```

**Utility commands:**
```bash
./scripts/dev.sh status   # Show status of all services
./scripts/dev.sh down     # Stop all services
./scripts/dev.sh restart  # Restart all services
./scripts/dev.sh help     # Show help
```

## Comandi utili
- Backend cheat sheet: [backend/docs/commands-cheatsheet.md](backend/docs/commands-cheatsheet.md)
- Frontend development script: [frontend/scripts/dev.sh](frontend/scripts/dev.sh)
