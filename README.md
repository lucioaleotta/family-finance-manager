# Family Finance Manager

Backend application to manage household budget, including:
- Expenses and incomes
- Monthly balances
- Assets and net worth
- Financial reporting

## Tech Stack
- Backend: Java 21, Spring Boot, Spring Security, Spring Data JPA
- Architecture: Modular Monolith (Spring Modulith), Hexagonal Architecture
- Database: PostgreSQL + Flyway
- Auth: JWT + Password Reset flow (token-based)
- Frontend: Next.js (App Router), React, TypeScript
- Tooling: Maven, npm, Docker (Compose v2/v1 fallback)

## Architecture
This project follows a modular monolith approach using Spring Modulith and Domain-Driven Design principles.

More details in `docs/architecture.md`.

Technical documentation:
- `docs/technical-documentation.md`
- `backend/docs/logging-standard.md`
- `backend/docs/db-backup-restore.md`

## Getting Started

### Prerequisites
- Java 21+
- Maven 3.8+
- Node.js 18+ & npm
- Docker (Compose v2 `docker compose`; supporto anche a `docker-compose` v1)

### Development Script

The project includes a development script (`scripts/dev.sh`) to manage all services (Database, Backend, Frontend).

**Start all services:**
```bash
./scripts/dev.sh up
```

**Start all services with backend profile `prod`:**
```bash
./scripts/dev.sh up-prod
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
./scripts/dev.sh db-export     # Export database backup (.dump)
./scripts/dev.sh db-import <file.dump> # Import database backup
./scripts/dev.sh db-smoke-test # Smoke test backup/restore + cleanup automatico DB test

# Backend (Spring Boot)
./scripts/dev.sh backend-start    # Start backend
./scripts/dev.sh backend-start-prod # Start backend with profile prod
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

**Backup/Restore DB rapido (da root):**
```bash
./scripts/dev.sh db-export
./scripts/dev.sh db-import ./backend/backups/<file>.dump
./scripts/dev.sh db-smoke-test
```

Lo smoke test usa un database temporaneo (`finance_smoke`) e lo rimuove automaticamente al termine.

## Comandi utili
- Backend cheat sheet: [backend/docs/commands-cheatsheet.md](backend/docs/commands-cheatsheet.md)
- Git branching convention: [backend/docs/git-branching-convention.md](backend/docs/git-branching-convention.md)
- Frontend development script: [frontend/scripts/dev.sh](frontend/scripts/dev.sh)

## Avvio completo in Docker (locale)

Se vuoi eseguire tutto in container (PostgreSQL + Backend + Frontend):

```bash
docker compose -f docker-compose.local.yml up --build
```

Servizi esposti:
- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- PostgreSQL: localhost:5432

Nota persistenza DB:
- I compose locali usano un volume condiviso con nome fisso `family-finance-pgdata`.
- `down` non cancella i dati.
- `down -v` rimuove il volume e quindi i dati persistiti.

Stop stack:

```bash
docker compose -f docker-compose.local.yml down
```

Stop + rimozione volume DB:

```bash
docker compose -f docker-compose.local.yml down -v
```

## Deploy su Google Cloud

- Guida passo-passo: [docs/deploy/google-cloud-step-by-step.md](docs/deploy/google-cloud-step-by-step.md)
- Report attività deploy setup: [docs/deploy/deployment-report-2026-02-26.md](docs/deploy/deployment-report-2026-02-26.md)
- Template secrets: [docs/deploy/secrets-template.env](docs/deploy/secrets-template.env)
- Checklist pre-demo clienti: [docs/deploy/pre-demo-checklist.md](docs/deploy/pre-demo-checklist.md)

### CI/CD Workflows
- Backend Cloud Run: [.github/workflows/deploy-backend-cloud-run.yml](.github/workflows/deploy-backend-cloud-run.yml)
- Frontend Cloud Run: [.github/workflows/deploy-frontend-cloud-run.yml](.github/workflows/deploy-frontend-cloud-run.yml)

## Password Reset (Forgot Password)

La funzionalità di reset password è implementata end-to-end (frontend + backend + DB).

- Endpoint backend:
	- `POST /api/auth/forgot-password`
	- `GET /api/auth/reset-password/validate?token=...`
	- `POST /api/auth/reset-password`
- Frontend:
	- `/forgot-password`
	- `/reset-password?token=...`
- Migrazione DB:
	- `backend/src/main/resources/db/migration/V10__password_reset_tokens.sql`

### Configurazione

Le proprietà del reset password sono esternalizzate (no hardcoded a runtime):

- `security.password-reset.token-minutes`
- `security.password-reset.reset-url`
- `security.password-reset.from-email`

Profili:

- `application.yml`: profilo di default `dev`
- `application-dev.yml`: valori locali di sviluppo
- `application-prod.yml`: valori da variabili ambiente

Variabili richieste in `prod`:

- `JWT_ISSUER`
- `JWT_SECRET`
- `JWT_ACCESS_TOKEN_MINUTES`
- `PASSWORD_RESET_TOKEN_MINUTES`
- `PASSWORD_RESET_URL`
- `PASSWORD_RESET_FROM_EMAIL`

Esempio avvio backend con profilo prod:

```bash
cd backend
SPRING_PROFILES_ACTIVE=prod \
JWT_ISSUER=<JWT_ISSUER> \
JWT_SECRET=<JWT_SECRET_FROM_SECRET_MANAGER> \
JWT_ACCESS_TOKEN_MINUTES=60 \
PASSWORD_RESET_TOKEN_MINUTES=30 \
PASSWORD_RESET_URL=https://app.example.com/reset-password \
PASSWORD_RESET_FROM_EMAIL=no-reply@example.com \
mvn spring-boot:run
```

Nota email: per invio reale va configurato SMTP (`spring.mail.*`).
Se `JavaMailSender` non è disponibile, il link di reset viene loggato (fallback dev).
